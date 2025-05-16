package com.app.cplanner.activities

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.app.cplanner.R
import com.app.cplanner.fragments.CreateListaFragment
import com.app.cplanner.model.entity.Lista
import com.app.cplanner.model.viewModel.ListaViewModel

class ListaListActivity : AppCompatActivity() {

    private val listaViewModel: ListaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_list)

        val btnAddLista = findViewById<Button>(R.id.btnAddLista)
        val linearLayoutListas = findViewById<LinearLayout>(R.id.linearLayoutListas)
        val linearLayoutElementos = findViewById<LinearLayout>(R.id.linearLayoutElementos)

        listaViewModel.listas.observe(this) { listas ->
            populateLinearLayoutListas(listas, linearLayoutListas, linearLayoutElementos)
        }

        // Agrega nueva Lista
        btnAddLista.setOnClickListener {
            CreateListaFragment { nuevaLista ->
                listaViewModel.addLista(nuevaLista)
            }.show(supportFragmentManager, "CreateListaFragment")
        }
    }

    private fun populateLinearLayoutListas(
        listas: List<Lista>,
        linearLayoutListas: LinearLayout,
        linearLayoutElementos: LinearLayout
    ) {
        linearLayoutListas.removeAllViews() // Limpia vistas existentes
        for (lista in listas) {
            val button = Button(this).apply {
                text = lista.nombre
                setOnClickListener {
                    linearLayoutElementos.removeAllViews()
                    for (elemento in lista.elementos) {
                        val textView = TextView(this@ListaListActivity).apply {
                            text = elemento
                            textSize = 16f
                            setPadding(8, 8, 8, 8)
                        }
                        linearLayoutElementos.addView(textView)
                    }
                }
            }
            linearLayoutListas.addView(button)
        }
    }
}