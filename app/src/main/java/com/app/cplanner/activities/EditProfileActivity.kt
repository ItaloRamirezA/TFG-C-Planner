package com.app.cplanner.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.app.cplanner.R
import com.app.cplanner.model.viewModel.UsuarioViewModel
import com.bumptech.glide.Glide

class EditProfileActivity : AppCompatActivity() {

    private val usuarioViewModel by viewModels<UsuarioViewModel>()

    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var imgFoto: ImageButton
    private lateinit var btnCambiarFoto: ImageButton
    private lateinit var btnGuardar: Button

    private var uriFoto: Uri? = null

    companion object {
        // Se usa para lanzar la galería y tambien para saber que el resultado viene de la galería.
        private const val PICK_IMAGE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // 1) Vinculamos las vistas
        etNombre = findViewById(R.id.etEditName)
        etEmail = findViewById(R.id.etEditEmail)
        imgFoto = findViewById(R.id.ivEditPhoto)
        btnCambiarFoto = findViewById(R.id.btnPickPhoto)
        btnGuardar = findViewById(R.id.btnSaveProfile)

        // 2) Observamos el LiveData<Usuario> para poblar UI
        usuarioViewModel.usuario.observe(this) { usuario ->
            usuario?.let {
                etNombre.setText(it.nombre)
                etEmail.setText(it.email)

                // Carga de la foto usando Glide
                if (it.fotoUrl.isNotBlank()) {
                    Glide.with(this)
                        .load(it.fotoUrl)
                        .placeholder(R.drawable.person_48px)
                        .circleCrop()
                        .into(imgFoto)
                } else {
                    imgFoto.setImageResource(R.drawable.person_48px)
                }
            }
        }

        // 3) Selector de foto desde galería
        btnCambiarFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            startActivityForResult(intent, PICK_IMAGE)
        }

        // 4) Guardar cambios (nombre, email y foto)
        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val email  = etEmail.text.toString().trim()

            // 4.1) Actualiza nombre y email
            usuarioViewModel.updateProfile(nombre, email)

            // 4.2) Si eligió nueva foto, la sube a Firebase Storage
            uriFoto?.let { usuarioViewModel.updatePhoto(it) }

            Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // 5) Recibimos la URI de la imagen seleccionada
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            uriFoto = data?.data
            uriFoto?.let { imgFoto.setImageURI(it) }
        }
    }

    /**
     * Despliega la galería para elegir una imagen de perfil
     */
    fun editPPButton(view: View) {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE)
    }
}
