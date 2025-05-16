package com.app.cplanner.activities

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.cplanner.R
import com.app.cplanner.fragments.CategoriaDialogFragment
import com.app.cplanner.model.entity.Categoria
import com.app.cplanner.model.entity.Tarea
import com.app.cplanner.model.viewModel.CategoriaViewModel
import com.app.cplanner.model.viewModel.TareaViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import yuku.ambilwarna.AmbilWarnaDialog

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

        // Inicializamos todas caracteristicas para la creación de tareas
        initializeTitle()
        initializeReminder()
        initializeCategory(btnAddCategory)
        initializeRepeat()
        initializeMultiDay()
        initializeSharedUsers()
        initializeFileAttachment()
        initializeSaveButton()
    }


    companion object {
        private const val REQUEST_CODE_PICK_FILES = 1002
    }


    private fun initializeTitle() {
        // Logic for title initialization
    }

    /** TODO
     * Si esta activado y tiene una fecha, creará una notificación
     * push para la tarea a la fecha de inicio.
     */
    private fun initializeReminder() {
        // Logic for reminder initialization
    }

    /**
     * Inicializa el selector de categoría para la tarea.
     *
     * Este método configura un "Spinner" para mostrar una lista de categorías disponibles.
     * Observa los cambios en la lista de categorías desde el "CategoriaViewModel" y actualiza
     * el adaptador del "Spinner" en consecuencia. También permite al usuario agregar una nueva
     * categoría mediante un botón que abre un diálogo de creación de categoría.
     *
     * @param btnAddCategory Botón que permite agregar una nueva categoría.
     */
    private fun initializeCategory(btnAddCategory: Button) {
        // Configura el adaptador para el Spinner de categorías
        val catAdapter = ArrayAdapter<Categoria>(
            this,
            android.R.layout.simple_spinner_item,
            mutableListOf()
        )
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = catAdapter

        // Observa los cambios en la lista de categorías y actualiza el Spinner
        categoriaVM.listaCategorias.observe(this) { cats ->
            val categoriasConOpciones = mutableListOf(
                Categoria(id = "none", nombre = "Sin categoría", color = "#FFFFFF")
            ).apply {
                addAll(cats) // Agrega las categorías obtenidas del ViewModel
            }
            catAdapter.clear()
            catAdapter.addAll(categoriasConOpciones)
            catAdapter.notifyDataSetChanged()
            spinnerCategory.setSelection(0) // Selecciona la opción predeterminada
        }

        // Configura el botón para agregar una nueva categoría
        btnAddCategory.setOnClickListener {
            CategoriaDialogFragment { nuevaCat ->
                categoriaVM.addCategoria(nuevaCat)
                Toast.makeText(this, "Categoría creada: ${nuevaCat.nombre}", Toast.LENGTH_SHORT).show()
            }.show(supportFragmentManager, "CreateCategoryFragment")
        }
    }

    /**
     * Inicializa la funcionalidad de repetición de la tarea.
     *
     * Este método configura un "Spinner" que permite seleccionar una opción de repetición
     * (Diario, Semanal o Mensual). El "Spinner" está contenido en un "LinearLayout" que
     * inicialmente está oculto. Cuando el usuario activa el interruptor de repetición
     * ("switchRepeat"), el contenedor del "Spinner" se hace visible, y cuando lo desactiva,
     * el contenedor se oculta nuevamente.
     */
    private fun initializeRepeat() {
        // Obtiene el contenedor del Spinner y lo oculta inicialmente
        val spinnerRepeatContainer = findViewById<LinearLayout>(R.id.spinnerRepeatContainer)
        spinnerRepeat = Spinner(this)
        spinnerRepeatContainer.addView(spinnerRepeat)
        spinnerRepeatContainer.visibility = View.GONE

        // Configura las opciones de repetición para el Spinner
        val repeatOptions = listOf("Diario", "Semanal", "Mensual")
        val repeatAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, repeatOptions)
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRepeat.adapter = repeatAdapter

        // Muestra u oculta el contenedor del Spinner según el estado del interruptor
        switchRepeat.setOnCheckedChangeListener { _, isChecked ->
            spinnerRepeatContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    /** TODO
     * Inicializa la funcionalidad de selección de múltiples días para la tarea.
     *
     * Este método controla la visibilidad del selector de fecha de finalización ("datePickerEnd").
     * Si el usuario activa el interruptor de múltiples días ("switchMultiDay"), el selector de
     * fecha de finalización se muestra, permitiendo elegir una fecha de fin. Si el interruptor
     * está desactivado, el selector de fecha de finalización se oculta.
     */
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

    private fun initializeSaveButton() {
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Título vacío", Toast.LENGTH_SHORT).show()
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

    /**
     * Busca un usuario por su email en la base de datos
     * y si existe lo agrega a la lista de usuarios compartidos.
     */
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

    /**
     * Agrega un chip(pestañita) para los usuarios
     * a los que se compartira la tarea.
     */
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

    /**
     * Agrega los archivos adjuntos a la vista.
     */
    private fun addFileToView(uri: Uri) {
        val textView = TextView(this).apply {
            text = uri.lastPathSegment
            textSize = 16f
            setPadding(8, 8, 8, 8)
        }
        llAttachedFiles.addView(textView)
    }
}