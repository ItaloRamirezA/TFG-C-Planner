package com.app.cplanner.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.cplanner.R
import com.app.cplanner.model.viewModel.TareaViewModel
import com.app.cplanner.ui.TareaAdapter

class TrashActivity : AppCompatActivity() {

    private val tareaVM by viewModels<TareaViewModel>()
    private lateinit var rvTrash: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        rvTrash = findViewById(R.id.rvTrash)
        rvTrash.layoutManager = LinearLayoutManager(this)

        tareaVM.listaTareas.observe(this) { deletedTasks ->
            rvTrash.adapter = TareaAdapter(deletedTasks, emptyMap()) { task ->
                // Handle task restoration or permanent deletion
            }
        }

        tareaVM.getDeletedTasks()
    }
}