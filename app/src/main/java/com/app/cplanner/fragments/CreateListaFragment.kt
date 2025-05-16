package com.app.cplanner.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.app.cplanner.R
import com.app.cplanner.model.entity.Lista
import com.google.android.material.textfield.TextInputEditText
import androidx.fragment.app.DialogFragment
import java.util.UUID

class CreateListaFragment(private val onSave: (Lista) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflate the dialog layout
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_create_lista, null)

        // Reference to the input field
        val etNombre = view.findViewById<TextInputEditText>(R.id.etListaNombre)

        // Build the AlertDialog
        return AlertDialog.Builder(requireContext())
            .setTitle("Nueva Lista")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombre.text.toString().trim().ifEmpty { "Sin nombre" }
                onSave(
                    Lista(
                        id = UUID.randomUUID().toString(),
                        nombre = nombre
                    )
                )
            }
            .setNegativeButton("Cancelar", null)
            .create()
    }
}