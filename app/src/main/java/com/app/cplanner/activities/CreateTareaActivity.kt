package com.app.cplanner.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import com.app.cplanner.R
import com.app.cplanner.model.entity.Categoria
import com.app.cplanner.model.entity.Tarea
import com.app.cplanner.model.viewModel.CategoriaViewModel
import com.app.cplanner.model.viewModel.TareaViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class CreateTareaActivity : AppCompatActivity() {

    private lateinit var etTitle        : TextInputEditText
    private lateinit var switchReminder : SwitchMaterial
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnColorPicker : Button
    private lateinit var switchMultiDay : SwitchMaterial
    private lateinit var btnPickDate    : Button
    private lateinit var btnSave        : Button

    private var selectedColor = 0xFF0000FF.toInt()
    private var pickedDate    = ""

    private val categoriaVM by viewModels<CategoriaViewModel>()
    private val tareaVM     by viewModels<TareaViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_tarea)

        // Bind views
        etTitle         = findViewById(R.id.etTitle)
        switchReminder  = findViewById(R.id.switchReminder)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        btnColorPicker  = findViewById(R.id.btnColorPicker)
        switchMultiDay  = findViewById(R.id.switchMultiDay)
        btnPickDate     = findViewById(R.id.btnPickDate)
        btnSave         = findViewById(R.id.btnSave)

        // Carga categorías
        val catAdapter = ArrayAdapter<Categoria>(
            this, android.R.layout.simple_spinner_item, mutableListOf()
        )
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = catAdapter
        categoriaVM.listaCategorias.observe(this) { cats ->
            catAdapter.clear()
            catAdapter.addAll(cats)
            catAdapter.notifyDataSetChanged()
        }

        // Color picker (usa AmbilWarna o simplemente un Toast)
        btnColorPicker.setOnClickListener {
            // Para esta prueba, rota entre rojo/verde/azul
            selectedColor = when (selectedColor) {
                0xFFFF0000.toInt() -> 0xFF00FF00.toInt()
                0xFF00FF00.toInt() -> 0xFF0000FF.toInt()
                else                -> 0xFFFF0000.toInt()
            }
            btnColorPicker.setBackgroundColor(selectedColor)
        }

        // Fecha
        btnPickDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this,
                { _, y, m, d ->
                    c.set(y, m, d)
                    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    pickedDate = fmt.format(c.time)
                    btnPickDate.text = pickedDate
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Guardar
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Título vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val tarea = Tarea().apply {
                titulo     = title
                reminder   = switchReminder.isChecked
                categoryId = (spinnerCategory.selectedItem as Categoria).id
                colorHex   = String.format("#%06X", 0xFFFFFF and selectedColor)
                multiDay   = switchMultiDay.isChecked
                date       = pickedDate
            }
            tareaVM.addTarea(tarea)
            Toast.makeText(this, "Tarea sin imagen guardada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
