package com.app.cplanner.session

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.app.cplanner.BaseActivity
import com.app.cplanner.MainActivity
import com.app.cplanner.R
import com.app.cplanner.model.entity.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignUpActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()
    }

    /** Llamado desde el botón "Registrarse" */
    fun botonSignUp(view: View) {
        registerUser()
    }

    /** Registra en Auth y guarda perfil en Firestore */
    private fun registerUser() {
        val name     = findViewById<EditText>(R.id.etSignUpName).text.toString().trim()
        val email    = findViewById<EditText>(R.id.etSignUpEmail).text.toString().trim()
        val password = findViewById<EditText>(R.id.etSignUpPassword).text.toString()

        if (!validateFormSignUp(name, email, password)) return

        showProgressBar()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                hideProgressBar()
                if (task.isSuccessful) {
                    // 1) UID del nuevo usuario
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // 2) Crear objeto Usuario
                    val usuario = Usuario(
                        id        = uid,
                        nombre    = name,
                        email     = email,
                        contrasena= password,
                        fotoUrl   = ""       // sin foto aún
                    )

                    // 3) Guardar en Firestore
                    db.collection("usuarios")
                        .document(uid)
                        .set(usuario.toMap())
                        .addOnSuccessListener {
                            showToast(this, "Usuario \"$name\" creado exitosamente")
                            // Navegar a MainActivity
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            showToast(this, "Error al guardar perfil: ${e.message}")
                        }

                } else {
                    // Error en Auth
                    showToast(this, "Error al registrar: ${task.exception?.message}")
                }
            }
    }

    /** Valida campos no vacíos */
    private fun validateFormSignUp(name: String, email: String, password: String): Boolean {
        return when {
            name.isEmpty() -> {
                showToast(this, "El nombre no puede estar vacío")
                false
            }
            email.isEmpty() -> {
                showToast(this, "El correo no puede estar vacío")
                false
            }
            password.isEmpty() -> {
                showToast(this, "La contraseña no puede estar vacía")
                false
            }
            else -> true
        }
    }

    /** Llamado desde el botón "¿Ya tienes cuenta? Iniciar Sesión" */
    fun botonLogin(view: View) {
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }
}
