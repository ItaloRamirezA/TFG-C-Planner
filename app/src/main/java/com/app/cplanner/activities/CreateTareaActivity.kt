package com.app.cplanner.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.cplanner.R
import com.app.cplanner.fragments.CategoriaDialogFragment
import com.app.cplanner.model.entity.Categoria
import com.app.cplanner.model.entity.Tarea
import com.app.cplanner.model.viewModel.CategoriaViewModel
import com.app.cplanner.model.viewModel.TareaViewModel
import com.app.cplanner.session.LocalNotification
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import yuku.ambilwarna.AmbilWarnaDialog
import java.util.Calendar

class CreateTareaActivity : AppCompatActivity() {

    private lateinit var etTitle: TextInputEditText
    private lateinit var switchReminder: SwitchMaterial
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerRepeat: Spinner
    private lateinit var switchRepeat: SwitchMaterial
    private lateinit var switchMultiDay: SwitchMaterial
    private lateinit var datePickerStart: DatePicker
    private lateinit var datePickerEnd: DatePicker
    private lateinit var btnAttachFile: Button
    private lateinit var btnSave: Button
    private lateinit var viewColorPreview: View
    private lateinit var actvSharedEmail: AutoCompleteTextView
    private lateinit var chipGroupShared: ChipGroup
    private lateinit var llAttachedFiles: LinearLayout
    private lateinit var btnAddCategory: Button
    private lateinit var timePicker: TimePicker

    private val sharedWithIds = mutableListOf<String>()

    private var selectedColor = 0xFF0000FF.toInt()
    private val fileUrisToAttach = mutableListOf<Uri>()

    private val categoriaVM by viewModels<CategoriaViewModel>()
    private val tareaVM by viewModels<TareaViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_tarea)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Obtener todos los elementos de la vista
        etTitle = findViewById(R.id.etTitle)
        switchReminder = findViewById(R.id.switchReminder)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        switchRepeat = findViewById(R.id.switchRepeat)
        btnAddCategory = findViewById(R.id.btnAddCategory)
        switchMultiDay = findViewById(R.id.switchMultiDay)
        datePickerStart = findViewById(R.id.datePicker)
        datePickerEnd = findViewById(R.id.datePickerEnd)
        btnAttachFile = findViewById(R.id.btnAttachFile)
        btnSave = findViewById(R.id.btnSave)
        viewColorPreview = findViewById(R.id.viewColorPreview)
        actvSharedEmail = findViewById(R.id.actvSharedEmail)
        chipGroupShared = findViewById(R.id.chipGroupShared)
        llAttachedFiles = findViewById(R.id.llAttachedFiles)
        timePicker = findViewById(R.id.timePicker)
        timePicker.setIs24HourView(true)

        // Inicializamos todas características para la creación de tareas
        initializeCategory(btnAddCategory)
        initializeRepeat()
        initializeMultiDay()
        initializeSharedUsers()
        initializeColorPicker()
        initializeFileAttachment()
        initializeSaveButton()

        checkPermission()
    }

    companion object {
        private const val REQUEST_CODE_PICK_FILES = 1002
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun checkPermission() {
        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            NOTIFICATION_PERMISSION_REQUEST_CODE
                        )
                    }
                }
            }
        }
    }

    private fun initializeSaveButton() {
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "El título no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val catPos = spinnerCategory.selectedItemPosition
            val categoryId = if (catPos >= 0 && spinnerCategory.count > 0) {
                val selectedCategory = spinnerCategory.selectedItem as Categoria
                if (selectedCategory.id == "none") "" else selectedCategory.id
            } else {
                ""
            }

            val startDay = datePickerStart.dayOfMonth
            val startMonth = datePickerStart.month
            val startYear = datePickerStart.year
            val startDate = String.format("%04d-%02d-%02d", startYear, startMonth + 1, startDay)

            val endDate = if (switchMultiDay.isChecked) {
                val endDay = datePickerEnd.dayOfMonth
                val endMonth = datePickerEnd.month
                val endYear = datePickerEnd.year
                String.format("%04d-%02d-%02d", endYear, endMonth + 1, endDay)
            } else {
                startDate
            }

            if (switchMultiDay.isChecked && endDate < startDate) {
                Toast.makeText(this, "La fecha de finalización no puede ser anterior a la de inicio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val repeatOption = if (switchRepeat.isChecked) spinnerRepeat.selectedItem.toString() else "Ninguno"

            // Validar y crear la notificación si el switch está activado
            if (switchReminder.isChecked) {
                val currentDate = Calendar.getInstance()
                val selectedDate = Calendar.getInstance().apply {
                    set(
                        datePickerStart.year,
                        datePickerStart.month,
                        datePickerStart.dayOfMonth,
                        timePicker.hour,
                        timePicker.minute,
                        0
                    )
                }

                if (selectedDate.timeInMillis <= currentDate.timeInMillis) {
                    Toast.makeText(this, "La fecha y hora deben ser futuras", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Crear la notificación
                val localNotification = LocalNotification(this)
                localNotification.programarNotificacion(
                    titulo = title,
                    texto = "Recordatorio de la tarea: $title",
                    imagen = 0,
                    anio = datePickerStart.year,
                    mes = datePickerStart.month + 1,
                    dia = datePickerStart.dayOfMonth,
                    hora = timePicker.hour,
                    minuto = timePicker.minute
                )
            }

            val tarea = Tarea(
                titulo = title,
                reminder = switchReminder.isChecked,
                categoryId = categoryId,
                colorHex = String.format("#%06X", 0xFFFFFF and selectedColor),
                multiDay = switchMultiDay.isChecked,
                startDate = startDate,
                endDate = endDate,
                attachments = fileUrisToAttach.map { it.toString() },
                sharedWith = sharedWithIds.toList(),
                repeat = repeatOption
            )

            tareaVM.addTarea(tarea)
            Toast.makeText(this, "Tarea guardada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initializeColorPicker() {
        viewColorPreview.setOnClickListener {
            val colorPicker = AmbilWarnaDialog(this, selectedColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    selectedColor = color
                    viewColorPreview.setBackgroundColor(selectedColor)
                }

                override fun onCancel(dialog: AmbilWarnaDialog?) {
                    // No hacer nada si se cancela
                }
            })
            colorPicker.show()
        }
    }

    private fun initializeCategory(btnAddCategory: Button) {
        val catAdapter = ArrayAdapter<Categoria>(
            this,
            android.R.layout.simple_spinner_item,
            mutableListOf()
        )
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = catAdapter

        categoriaVM.listaCategorias.observe(this) { cats ->
            val categoriasConOpciones = mutableListOf(
                Categoria(id = "none", nombre = "Sin categoría", color = "#FFFFFF")
            ).apply {
                addAll(cats)
            }
            catAdapter.clear()
            catAdapter.addAll(categoriasConOpciones)
            catAdapter.notifyDataSetChanged()
            spinnerCategory.setSelection(0)
        }

        btnAddCategory.setOnClickListener {
            CategoriaDialogFragment { nuevaCat ->
                categoriaVM.addCategoria(nuevaCat)
                Toast.makeText(this, "Categoría creada: ${nuevaCat.nombre}", Toast.LENGTH_SHORT).show()
            }.show(supportFragmentManager, "CreateCategoryFragment")
        }
    }

    private fun initializeRepeat() {
        val spinnerRepeatContainer = findViewById<LinearLayout>(R.id.spinnerRepeatContainer)
        spinnerRepeat = Spinner(this)
        spinnerRepeatContainer.addView(spinnerRepeat)
        spinnerRepeatContainer.visibility = View.GONE

        val repeatOptions = listOf("Diario", "Semanal", "Mensual")
        val repeatAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, repeatOptions)
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRepeat.adapter = repeatAdapter

        switchRepeat.setOnCheckedChangeListener { _, isChecked ->
            spinnerRepeatContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun initializeMultiDay() {
        switchMultiDay.setOnCheckedChangeListener { _, isChecked ->
            datePickerEnd.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun initializeSharedUsers() {
        actvSharedEmail.setOnEditorActionListener { _, _, _ ->
            val email = actvSharedEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                addUserByEmail(email)
            }
            true
        }
    }

    private fun initializeFileAttachment() {
        btnAttachFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(Intent.createChooser(intent, "Selecciona archivos"), REQUEST_CODE_PICK_FILES)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_FILES && resultCode == RESULT_OK) {
            if (data?.clipData != null) {
                for (i in 0 until data.clipData!!.itemCount) {
                    val uri = data.clipData!!.getItemAt(i).uri
                    fileUrisToAttach.add(uri)
                    addFileToView(uri)
                }
            } else if (data?.data != null) {
                val uri = data.data!!
                fileUrisToAttach.add(uri)
                addFileToView(uri)
            }
        }
    }

    private fun addUserByEmail(email: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.first()
                    val userId = user.id
                    val userName = user.getString("nombre") ?: email

                    if (!sharedWithIds.contains(userId)) {
                        sharedWithIds.add(userId)
                        addUserChip(userName, userId)
                    }
                    actvSharedEmail.text?.clear()
                } else {
                    Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al buscar usuario", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addUserChip(userName: String, userId: String) {
        val chip = Chip(this).apply {
            text = userName
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                sharedWithIds.remove(userId)
                chipGroupShared.removeView(this)
            }
        }
        chipGroupShared.addView(chip)
    }

    private fun addFileToView(uri: Uri) {
        val textView = TextView(this).apply {
            text = uri.lastPathSegment
            textSize = 16f
            setPadding(8, 8, 8, 8)
        }
        llAttachedFiles.addView(textView)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
                switchReminder.isChecked = false // Desactiva el switch si se deniega el permiso
            }
        }
    }
}