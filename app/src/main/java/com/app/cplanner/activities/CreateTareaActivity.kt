package com.app.cplanner.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import com.app.cplanner.R
import com.app.cplanner.model.entity.Categoria
import com.app.cplanner.model.entity.Tarea
import com.app.cplanner.model.viewModel.CategoriaViewModel
import com.app.cplanner.model.viewModel.TareaViewModel
import com.google.android.material.textfield.TextInputEditText
import yuku.ambilwarna.AmbilWarnaDialog
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.material.switchmaterial.SwitchMaterial

class CreateTareaActivity : AppCompatActivity() {

    companion object {
        private const val PICK_IMAGE = 202
        private const val PICK_FILE  = 203
    }

    private lateinit var ivTaskImage: ImageView
    private lateinit var etTitle      : TextInputEditText
    private lateinit var switchReminder: SwitchMaterial
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnColorPicker : Button
    private lateinit var switchMultiDay: SwitchMaterial
    private lateinit var btnPickDate    : Button
    private lateinit var btnAttach      : Button
    private lateinit var btnSave        : Button

    private var imageUri: Uri? = null
    private var fileUri : Uri? = null
    private var selectedColor = 0xFF0000FF.toInt() // color inicial
    private var pickedDateStr: String? = null

    private val categoriaVM by viewModels<CategoriaViewModel>()
    private val tareaVM     by viewModels<TareaViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_tarea)

        // 1) Vincular vistas
        ivTaskImage      = findViewById(R.id.ivTaskImage)
        etTitle          = findViewById(R.id.etTitle)
        switchReminder   = findViewById(R.id.switchReminder)
        spinnerCategory  = findViewById(R.id.spinnerCategory)
        btnColorPicker   = findViewById(R.id.btnColorPicker)
        switchMultiDay   = findViewById(R.id.switchMultiDay)
        btnPickDate      = findViewById(R.id.btnPickDate)
        btnAttach        = findViewById(R.id.btnAttach)
        btnSave          = findViewById(R.id.btnSave)

        // 2) Imagen cabecera
        ivTaskImage.setOnClickListener {
            startActivityForResult(
                Intent(Intent.ACTION_PICK).apply { type = "image/*" },
                PICK_IMAGE
            )
        }

        // 3) Cargar categorías en spinner
        val catAdapter = ArrayAdapter<Categoria>(
            this, android.R.layout.simple_spinner_item, mutableListOf()
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = it
        }
        categoriaVM.listaCategorias.observe(this) { lista ->
            catAdapter.clear()
            catAdapter.addAll(lista)
            catAdapter.notifyDataSetChanged()
        }
        categoriaVM.cargarCategorias()

        // 4) Color picker
        btnColorPicker.setBackgroundColor(selectedColor)
        btnColorPicker.setOnClickListener {
            AmbilWarnaDialog(this, selectedColor,
                object : AmbilWarnaDialog.OnAmbilWarnaListener {
                    override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                        selectedColor = color
                        btnColorPicker.setBackgroundColor(color)
                    }
                    override fun onCancel(dialog: AmbilWarnaDialog) {}
                }
            ).show()
        }

        // 5) Fecha
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this,
                { _, y, m, d ->
                    cal.set(y, m, d)
                    pickedDateStr = dateFormat.format(cal.time)
                    btnPickDate.text = pickedDateStr
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // 6) Archivos
        btnAttach.setOnClickListener {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }.also { startActivityForResult(it, PICK_FILE) }
        }

        // 7) Guardar tarea
        btnSave.setOnClickListener {
            val tarea = Tarea().apply {
                title        = etTitle.text.toString()
                reminder     = switchReminder.isChecked
                categoryId   = (spinnerCategory.selectedItem as Categoria).id
                colorHex     = String.format("#%06X", 0xFFFFFF and selectedColor)
                multiDay     = switchMultiDay.isChecked
                date         = pickedDateStr ?: ""
                imageUri     = this@CreateTareaActivity.imageUri?.toString() ?: ""
                attachmentUri= this@CreateTareaActivity.fileUri?.toString()  ?: ""
                // userId y demás los maneja tu ViewModel
            }
            tareaVM.addTarea(tarea)
            Toast.makeText(this, "Tarea creada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            PICK_IMAGE -> {
                imageUri = data?.data
                ivTaskImage.setImageURI(imageUri)
            }
            PICK_FILE  -> {
                fileUri = data?.data
                // opcional: mostrar nombre
                contentResolver.query(fileUri!!, null, null, null, null)?.use { c ->
                    if (c.moveToFirst()) {
                        val nameIdx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val name    = if (nameIdx>=0) c.getString(nameIdx) else "archivo"
                        btnAttach.text = name
                    }
                }
            }
        }
    }
}
