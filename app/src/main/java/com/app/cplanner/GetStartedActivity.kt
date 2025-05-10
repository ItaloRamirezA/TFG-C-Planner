package com.app.cplanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.cplanner.session.SignInActivity
import com.google.firebase.auth.FirebaseAuth

class GetStartedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_get_started)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser!= null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    /**
     * Botón getStarted.
     */
    fun getStarted(view: View) {
        startActivity(Intent(this, SignInActivity::class.java))
        this.finish()
    }
}