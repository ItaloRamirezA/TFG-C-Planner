package com.app.cplanner

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.cplanner.activities.CreateTareaActivity

open class BaseActivity : AppCompatActivity() {
    private lateinit var progbar: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }


    override fun onDestroy() {
        if (::progbar.isInitialized && progbar.isShowing) {
            progbar.dismiss()
        }
        super.onDestroy()
    }

    /**
     * Método para mostrar el ProgressBar.
     */
    fun showProgressBar() {
        progbar = Dialog(this)
        progbar.setContentView(R.layout.progress_bar)
        progbar.setCancelable(false)
        progbar.show()
    }

    /**
     * Método para ocultar el ProgressBar.
     */
    fun hideProgressBar() {
        if (::progbar.isInitialized && progbar.isShowing) {
            progbar.dismiss()
        }
    }


    /**
     * Método para mostrar un Toast.
     * @param activity Activity
     * @param msg Mensaje a mostrar
     */
    public fun showToast(activity: Activity, msg:String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }

    companion object {
        public fun showToast(activity: Activity, msg:String) {
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        }
    }


}