package com.app.cplanner.activities

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.cplanner.R
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
    private lateinit var switchMultiDay: SwitchMaterial
    private lateinit var datePickerStart: DatePicker
    private lateinit var datePickerEnd: DatePicker
    private lateinit var btnAttachFile: Button
    private lateinit var btnSave: Button
    private lateinit var viewColorPreview: View
    private lateinit var actvSharedEmail: AutoCompleteTextView
    private lateinit var chipGroupShared: ChipGroup
    private lateinit var llAttachedFiles: LinearLayout
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

        etTitle = findViewById(R.id.etTitle)
        switchReminder = findViewById(R.id.switchReminder)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        switchMultiDay = findViewById(R.id.switchMultiDay)
        datePickerStart = findViewById(R.id.datePicker)
        datePickerEnd = findViewById(R.id.datePickerEnd)
        btnAttachFile = findViewById(R.id.btnAttachFile)
        btnSave = findViewById(R.id.btnSave)
        viewColorPreview = findViewById(R.id.viewColorPreview)
        actvSharedEmail = findViewById(R.id.actvSharedEmail)
        chipGroupShared = findViewById(R.id.chipGroupShared)
        llAttachedFiles = findViewById(R.id.llAttachedFiles)

        val catAdapter = ArrayAdapter<Categoria>(
            this,
            android.R.layout.simple_spinner_item,
            mutableListOf()
        )
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = catAdapter
        categoriaVM.listaCategorias.observe(this) { cats ->
            catAdapter.clear()
            catAdapter.addAll(cats)
            catAdapter.notifyDataSetChanged()
        }

        (viewColorPreview.background as GradientDrawable).setColor(selectedColor)
        viewColorPreview.setOnClickListener {
            AmbilWarnaDialog(this, selectedColor,
                object : AmbilWarnaDialog.OnAmbilWarnaListener {
                    override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                        selectedColor = color
                        (viewColorPreview.background as GradientDrawable).setColor(color)
                    }

                    override fun onCancel(dialog: AmbilWarnaDialog) {}
                }
            ).show()
        }

        switchMultiDay.setOnCheckedChangeListener { _, isChecked ->
            datePickerEnd.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        actvSharedEmail.setOnEditorActionListener { _, _, _ ->
            val email = actvSharedEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                buscarUsuarioPorEmail(email)
            }
            true
        }

        btnAttachFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*" // Allow all file types
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Allow multiple selection
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(Intent.createChooser(intent, "Selecciona archivos"), REQUEST_CODE_PICK_FILES)
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Título vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val catPos = spinnerCategory.selectedItemPosition
            val categoryId = if (catPos >= 0 && spinnerCategory.count > 0) {
                (spinnerCategory.selectedItem as Categoria).id
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

            val tarea = Tarea(
                titulo = title,
                reminder = switchReminder.isChecked,
                categoryId = categoryId,
                colorHex = String.format("#%06X", 0xFFFFFF and selectedColor),
                multiDay = switchMultiDay.isChecked,
                startDate = startDate,
                endDate = endDate,
                attachments = fileUrisToAttach.map { it.toString() },
                sharedWith = sharedWithIds.toList()
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
                // Multiple files selected
                for (i in 0 until data.clipData!!.itemCount) {
                    val uri = data.clipData!!.getItemAt(i).uri
                    fileUrisToAttach.add(uri)
                    addFileToView(uri)
                }
            } else if (data?.data != null) {
                // Single file selected
                val uri = data.data!!
                fileUrisToAttach.add(uri)
                addFileToView(uri)
            }
        }
    }

    private fun buscarUsuarioPorEmail(email: String) {
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

    companion object {
        private const val REQUEST_CODE_PICK_FILES = 1002
    }
}