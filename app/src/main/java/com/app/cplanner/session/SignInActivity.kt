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

class SignInActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()
    }

    /**
     * Método para iniciar sesión al presionar el botón.
     */
    fun botonLogin(view: View) {
        signIn()
    }

    /**
     * Método para iniciar sesión.
     */
    private fun signIn() {
        val email = findViewById<EditText>(R.id.etSignInEmail).text.toString()
        val password = findViewById<EditText>(R.id.etSignInPassword).text.toString()
        if (validateFormSignIn(email, password)) {
            showProgressBar()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Inicio de sesión exitoso
                        hideProgressBar()
                        showToast(this, "Bienvenido de nuevo")
                        startActivity(Intent(this, MainActivity::class.java))
                        this.finish()
                    } else {
                        // Inicio de sesión fallido
                        hideProgressBar()
                        showToast(this, "Error al iniciar sesión: ${task.exception?.message}")
                    }
                }
        }
    }

    /**
     * Método para validar el formulario de registro de usuario.
     */
    private fun validateFormSignIn(email: String, password: String): Boolean {
        return when {
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
     * Botón register.
     */
    fun botonRegister(view: View) {
        startActivity(Intent(this, SignUpActivity::class.java))
        this.finish()
    }

    /**
     * Botón forget password.
     */
    fun botonForgetPassword(view: View) {
        startActivity(Intent(this, ForgetPasswordActivity::class.java))
    }


}