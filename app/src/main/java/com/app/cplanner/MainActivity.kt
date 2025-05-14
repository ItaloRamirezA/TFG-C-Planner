package com.app.cplanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.app.cplanner.activities.CreateTareaActivity
import com.app.cplanner.activities.UserProfileActivity
import com.app.cplanner.fragments.CategoriaDialogFragment
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.viewModels
import com.app.cplanner.activities.TareasListActivity
import com.app.cplanner.model.viewModel.CategoriaViewModel

class MainActivity : BaseActivity() {
    private lateinit var auth:FirebaseAuth

    // Para probar crear categoria
    private val categoriaViewModel by viewModels<CategoriaViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
    }

    fun signOutBoton(view: View) {
        if (auth.currentUser != null) {
            auth.signOut()
            showToast(this, "Sesión cerrada")
            startActivity(Intent(this, GetStartedActivity::class.java))
            finish()
        } else {
            showToast(this, "No hay sesión activa")
        }
    }

    fun createActivityBoton(view: View) {
        val intent = Intent(this, CreateTareaActivity::class.java)
        startActivity(intent)
    }

    fun goToProfile(view: View) {
        val intent = Intent(this, UserProfileActivity::class.java)
        startActivity(intent)
    }

    /** Botón para probar el diálogo de categoría */
    fun goToCreateCategory(view: View) {
        CategoriaDialogFragment { nuevaCat ->
            // 1) Guarda en Firestore y en la lista local
            categoriaViewModel.addCategoria(nuevaCat)

            // 2) Muestra un mensaje de confirmación
            showToast(this, "Categoría «${nuevaCat.nombre}» creada")

            // 3) (Opcional) Observa LiveData para actualizar UI si tuvieras un Recycler/Spinner
            // categoriaViewModel.listaCategorias.observe(this) { lista ->
            //     miAdapter.submitList(lista)
            // }
        }.show(supportFragmentManager, "CreateCategoryFragment")
    }

    fun goToTareasList(view: View) {
        val intent = Intent(this, TareasListActivity::class.java)
        startActivity(intent)
    }


}