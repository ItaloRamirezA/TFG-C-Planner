package com.app.cplanner.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.cplanner.R
import com.app.cplanner.model.viewModel.UsuarioViewModel
import com.app.cplanner.session.SignInActivity
import com.bumptech.glide.Glide

/**
 * Pantalla de perfil de usuario.
 */
class UserProfileActivity : AppCompatActivity() {
    // ViewModel para manejar la lógica de negocio del usuario
    private val usuarioViewModel by viewModels<UsuarioViewModel>()

    private lateinit var imgFoto: ImageView
    private lateinit var tvNombre: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnEditar: Button

    override fun onResume() {
        super.onResume()
        usuarioViewModel.cargarUsuario()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Elementos de la vista
        imgFoto = findViewById(R.id.ivProfilePhoto)
        tvNombre = findViewById(R.id.tvProfileName)
        tvEmail = findViewById(R.id.tvProfileEmail)
        btnEditar = findViewById(R.id.btnEditProfile)

        // Observa cambios en el LiveData<Usuario> del ViewModel y actualiza la interfaz:
        // Cada vez que el perfil (usuario) cambie o se recargue, este bloque se ejecuta
        usuarioViewModel.usuario.observe(this) { usuario ->
            if (usuario != null) {
                // Nombre y correo
                tvNombre.text = usuario.nombre
                tvEmail.text  = usuario.email

                // Foto: placeholder mientras carga o si no tiene URL
                if (usuario.fotoUrl.isNotBlank()) {
                    Glide.with(this)
                        .load(usuario.fotoUrl)
                        .placeholder(R.drawable.person_48px)
                        .circleCrop()
                        .into(imgFoto)
                } else {
                    imgFoto.setImageResource(R.drawable.person_48px)
                }
            } else {
                // Si por algún motivo no hay sesión iniciada, te lleva al login
                tvNombre.text = getString(R.string.no_sesion_iniciada)
                tvEmail.text  = ""
                imgFoto.setImageResource(R.drawable.person_48px)
                startActivity(Intent(this, SignInActivity::class.java))
            }
        }
    }

    /**
     * Navega a la pantalla de edición de perfil.
     */
    fun goToEditProfile(view: View) {
        startActivity(Intent(this, EditProfileActivity::class.java))
    }
}
