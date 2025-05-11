package com.app.cplanner.session

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.cplanner.BaseActivity
import com.app.cplanner.R
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forget_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        auth = FirebaseAuth.getInstance()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    /**
     * Método para enviar el correo de restablecimiento de contraseña.
     */
    fun submitBoton(view: View) {
        resetPassword()
    }

    /**
     * Método para restablecer la contraseña.
     */
    private fun resetPassword() {
        val email = findViewById<android.widget.EditText>(R.id.etForgetPasswordEmail).text.toString()
        if (validateFormForgetPassword(email)) {
            showProgressBar()
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Envío exitoso
                        hideProgressBar()
                        findViewById<android.widget.EditText>(R.id.tilEmailForgetPassword)?.visibility = View.GONE
                        findViewById<TextView>(R.id.tvSubmitMsg).visibility = View.VISIBLE
                        findViewById<Button>(R.id.btnForgotPasswordSubmit).visibility = View.GONE
                        showToast(this, "Correo de restablecimiento enviado a $email")
                        finish()
                    } else {
                        // Error al enviar el correo
                        hideProgressBar()
                        showToast(this, "Error: ${task.exception?.message}")
                    }
                }
        }
    }

    /**
     * Método para validar el formulario de restablecimiento de contraseña.
     */
    private fun validateFormForgetPassword(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                showToast(this, "Por favor ingrese su correo electrónico")
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showToast(this, "Por favor ingrese un correo electrónico válido")
                false
            }
            else -> true
        }
    }


}