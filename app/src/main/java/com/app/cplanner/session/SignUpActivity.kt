package com.app.cplanner.session

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.cplanner.BaseActivity
import com.app.cplanner.MainActivity
import com.app.cplanner.R
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : BaseActivity() {
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initializar Firebase Auth
        auth = FirebaseAuth.getInstance()
    }

    fun botonSignUp(view: View) {
        registerUser()
    }

    /**
     * Método para registrar un nuevo usuario
     * verificando nombre, email y contraseña.
     */
    private fun registerUser() {
        val name = findViewById<EditText>(R.id.etSignUpName).text.toString()
        val email = findViewById<EditText>(R.id.etSignUpEmail).text.toString()
        val password = findViewById<EditText>(R.id.etSignUpPassword).text.toString()
        if (validateForm(name, email, password)) {
            showProgressBar()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Registro exitoso
                        hideProgressBar()
                        showToast(this, "Usuario $name creado con éxitoso")
                        startActivity(Intent(this, MainActivity::class.java))
                        this.finish()
                    } else {
                        // Registro fallido
                        hideProgressBar()
                        showToast(this, "Error al registrar: ${task.exception?.message}")
                    }
                }
        }
    }

    /**
     * Método para validar el formulario de registro de usuario.
     */
    private fun validateForm(name: String, email: String, password: String): Boolean {
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

    /**
     * Botón login.
     */
    fun botonLogin(view: View) {
        startActivity(Intent(this, SignInActivity::class.java))
        this.finish()
    }

}