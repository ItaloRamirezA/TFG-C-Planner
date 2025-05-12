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
import com.bumptech.glide.Glide


class UserProfileActivity : AppCompatActivity() {

    // 1) Obtén tu ViewModel
    private val usuarioViewModel by viewModels<UsuarioViewModel>()

    // 2) Declara las vistas
    private lateinit var imgFoto: ImageView
    private lateinit var tvNombre: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnEditar: Button

    override fun onResume() {
        super.onResume()
        // Recarga al volver a primer plano para que siempre traiga la versión más reciente :contentReference[oaicite:1]{index=1}
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

        // 3) Vincula las vistas
        imgFoto   = findViewById(R.id.ivProfilePhoto)
        tvNombre  = findViewById(R.id.tvProfileName)
        tvEmail   = findViewById(R.id.tvProfileEmail)
        btnEditar = findViewById(R.id.btnEditProfile)

        // 4) Observa el LiveData<Usuario> y actualiza la UI cuando llegue
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
                // Aquí podrías navegar al login si no hay usuario
                tvNombre.text = getString(R.string.no_sesion_iniciada)
                tvEmail.text  = ""
                imgFoto.setImageResource(R.drawable.person_48px)
            }
        }

        // 5) Botón para ir a editar
        btnEditar.setOnClickListener { goToEditProfile(it) }
    }

    fun goToEditProfile(view: View) {
        startActivity(Intent(this, EditProfileActivity::class.java))
        // no hacemos finish() si quieres volver aquí luego
    }
}
