package com.app.cplanner.activities

import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import com.app.cplanner.R
import com.app.cplanner.model.entity.Categoria
import com.app.cplanner.model.entity.Tarea
import com.app.cplanner.model.viewModel.CategoriaViewModel
import com.app.cplanner.model.viewModel.TareaViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import yuku.ambilwarna.AmbilWarnaDialog
import java.text.SimpleDateFormat
import java.util.*

class CreateTareaActivity : AppCompatActivity() {

    // Vistas principales
    private lateinit var etTitle          : TextInputEditText
    private lateinit var switchReminder   : SwitchMaterial
    private lateinit var spinnerCategory  : Spinner
    private lateinit var switchMultiDay   : SwitchMaterial
    private lateinit var datePicker       : DatePicker
    private lateinit var btnAttachFile    : Button
    private lateinit var btnSave          : Button
    private lateinit var viewColorPreview : View

    // Para compartir con usuarios
    private lateinit var actvSharedEmail  : AutoCompleteTextView
    private lateinit var chipGroupShared  : ChipGroup
    private val sharedWithIds = mutableListOf<String>()

    // Estados internos
    private var selectedColor   = 0xFF0000FF.toInt()
    private var fileUriToAttach : Uri? = null

    // ViewModels
    private val categoriaVM by viewModels<CategoriaViewModel>()
    private val tareaVM     by viewModels<TareaViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_tarea)

        // 1) Bind de vistas
        etTitle          = findViewById(R.id.etTitle)
        switchReminder   = findViewById(R.id.switchReminder)
        spinnerCategory  = findViewById(R.id.spinnerCategory)
        switchMultiDay   = findViewById(R.id.switchMultiDay)
        datePicker       = findViewById(R.id.datePicker)
        btnAttachFile    = findViewById(R.id.btnAttachFile)
        btnSave          = findViewById(R.id.btnSave)
        viewColorPreview = findViewById(R.id.viewColorPreview)
        actvSharedEmail  = findViewById(R.id.actvSharedEmail)
        chipGroupShared  = findViewById(R.id.chipGroupShared)

        // 2) Spinner de categorías
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

        // 3) Selector de color por AmbilWarnaDialog
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

        // 4) Guardar tarea con todos los campos
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Título vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Determinar ID de categoría (vacío si no hay selección)
            val catPos = spinnerCategory.selectedItemPosition
            val categoryId = if (catPos >= 0 && spinnerCategory.count > 0) {
                (spinnerCategory.selectedItem as Categoria).id
            } else {
                ""  // sin categoría
            }

            // Obtener fecha seleccionada del DatePicker
            val day = datePicker.dayOfMonth
            val month = datePicker.month
            val year = datePicker.year
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)

            val tarea = Tarea().apply {
                titulo       = title
                reminder     = switchReminder.isChecked
                this.categoryId = categoryId
                colorHex     = String.format("#%06X", 0xFFFFFF and selectedColor)
                multiDay     = switchMultiDay.isChecked
                date         = selectedDate
                attachmentUri = fileUriToAttach?.toString().orEmpty()
                sharedWith    = sharedWithIds.toList()
            }

            tareaVM.addTarea(tarea)
            Toast.makeText(this, "Tarea guardada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}