package com.app.cplanner.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
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
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import yuku.ambilwarna.AmbilWarnaDialog
import java.text.SimpleDateFormat
import java.util.*

class CreateTareaActivity : AppCompatActivity() {

    companion object {
        private const val PICK_FILE = 300
    }

    private lateinit var etTitle         : TextInputEditText
    private lateinit var switchReminder  : SwitchMaterial
    private lateinit var spinnerCategory : Spinner
    private lateinit var switchMultiDay  : SwitchMaterial
    private lateinit var btnPickDate     : Button
    private lateinit var btnAttachFile   : Button
    private lateinit var btnSave         : Button

    private var selectedColor   = 0xFF0000FF.toInt()
    private var pickedDate      = ""
    private var fileUriToAttach : Uri? = null

    private val categoriaVM by viewModels<CategoriaViewModel>()
    private val tareaVM     by viewModels<TareaViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_tarea)

        // 1) Bind de vistas
        etTitle         = findViewById(R.id.etTitle)
        switchReminder  = findViewById(R.id.switchReminder)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        switchMultiDay  = findViewById(R.id.switchMultiDay)
        btnPickDate     = findViewById(R.id.btnPickDate)
        btnAttachFile   = findViewById(R.id.btnAttachFile)
        btnSave         = findViewById(R.id.btnSave)

        // 2) Spinner categorías
        val catAdapter = ArrayAdapter<Categoria>( this,
            android.R.layout.simple_spinner_item, mutableListOf() )
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = catAdapter
        categoriaVM.listaCategorias.observe(this) { cats ->
            catAdapter.clear()
            catAdapter.addAll(cats)
            catAdapter.notifyDataSetChanged()
        }

        // 3) Círculo de color
        val viewColorPreview: View = findViewById(R.id.viewColorPreview)
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

        // 4) DatePicker
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

        // 5) Adjuntar archivo
        btnAttachFile.setOnClickListener {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }.also { startActivityForResult(it, PICK_FILE) }
        }

        // 6) Guardar tarea
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Título vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val tarea = Tarea().apply {
                titulo        = title
                reminder      = switchReminder.isChecked
                categoryId    = (spinnerCategory.selectedItem as Categoria).id
                colorHex      = String.format("#%06X", 0xFFFFFF and selectedColor)
                multiDay      = switchMultiDay.isChecked
                date          = pickedDate
                attachmentUri = fileUriToAttach?.toString().orEmpty()
            }
            tareaVM.addTarea(tarea)
            Toast.makeText(this, "Tarea guardada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                // Permiso persistente
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                fileUriToAttach = uri
                // Mostrar nombre en el botón
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val name = if (idx>=0) cursor.getString(idx) else "archivo"
                        btnAttachFile.text = name
                    }
                }
            }
        }
    }
}
