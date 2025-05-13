package com.app.cplanner.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import com.app.cplanner.R
import com.app.cplanner.model.entity.Categoria
import yuku.ambilwarna.AmbilWarnaDialog
import java.util.UUID
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText

class CategoriaDialogFragment( private val onSave: (Categoria) -> Unit) : DialogFragment() {

    private var selectedColor: Int = 0xFF0000FF.toInt() // color inicial (azul)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflamos el layout de diálogo
        val view = requireActivity().layoutInflater
            .inflate(R.layout.fragment_create_category, null)

        // Referencias a vistas
        val etNombre = view.findViewById<TextInputEditText>(R.id.etCatNombre)
        val btnColor = view.findViewById<Button>(R.id.btnColorPicker)

        // Botón para lanzar el color picker
        btnColor.setOnClickListener {
            AmbilWarnaDialog(
                requireContext(),
                selectedColor,
                object : AmbilWarnaDialog.OnAmbilWarnaListener {
                    override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                        selectedColor = color
                        btnColor.setBackgroundColor(color)
                    }
                    override fun onCancel(dialog: AmbilWarnaDialog) { /* no-op */ }
                }
            ).show()
        }

        // Construcción del AlertDialog
        return AlertDialog.Builder(requireContext())
            .setTitle("Nueva Categoría")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombre.text.toString().trim().ifEmpty { "Sin nombre" }
                val hexColor = String.format("#%06X", 0xFFFFFF and selectedColor)
                onSave(
                    Categoria(
                        id     = UUID.randomUUID().toString(),
                        nombre = nombre,
                        color  = hexColor
                    )
                )
            }
            .setNegativeButton("Cancelar", null)
            .create()
    }
}
