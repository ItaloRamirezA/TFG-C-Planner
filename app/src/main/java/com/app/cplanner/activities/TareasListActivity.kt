package com.app.cplanner.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.cplanner.R
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.cplanner.model.entity.Tarea
import com.app.cplanner.model.viewModel.TareaViewModel
import com.app.cplanner.ui.TareaAdapter

class TareasListActivity : AppCompatActivity() {

    private val tareaVM by viewModels<TareaViewModel>()
    private lateinit var rvTasks: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tareas_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // 1) Configura RecyclerView y LayoutManager
        rvTasks = findViewById(R.id.rvTasks)
        rvTasks.layoutManager = LinearLayoutManager(this)

        // 2) Observa LiveData y asigna Adapter aquí
        tareaVM.listaTareas.observe(this) { list ->
            rvTasks.adapter = TareaAdapter(list) { task ->
                // Al pulsar, abre CreateTareaActivity pasándole el ID
                val intent = Intent(this, CreateTareaActivity::class.java)
                intent.putExtra("taskId", task.id)
                startActivity(intent)
            }
        }

        // 3) Dispara la carga de tareas
        tareaVM.cargarTareas()
    }
}
