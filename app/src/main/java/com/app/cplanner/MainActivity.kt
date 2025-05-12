package com.app.cplanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.cplanner.activities.CreateActivityActivity
import com.app.cplanner.activities.UserProfileActivity
import com.google.firebase.auth.FirebaseAuth


class MainActivity : BaseActivity() {
    private lateinit var auth:FirebaseAuth

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
        val intent = Intent(this, CreateActivityActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun goToProfile(view: View) {
        val intent = Intent(this, UserProfileActivity::class.java)
        startActivity(intent)
        finish()
    }


}