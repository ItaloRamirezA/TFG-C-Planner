package com.app.cplanner.session

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.cplanner.BaseActivity
import com.app.cplanner.R

class SignInActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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