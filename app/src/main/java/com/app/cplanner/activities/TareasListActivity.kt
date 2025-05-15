package com.app.cplanner.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.cplanner.R
import com.app.cplanner.model.entity.Tarea
import com.app.cplanner.model.viewModel.CategoriaViewModel
import com.app.cplanner.model.viewModel.TareaViewModel
import com.app.cplanner.ui.TareaAdapter

class TareasListActivity : AppCompatActivity() {

    private val tareaVM by viewModels<TareaViewModel>()
    private val categoriaVM by viewModels<CategoriaViewModel>()
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

        var latestTasks = emptyList<Tarea>()
        var latestCatMap = emptyMap<String, String>()

        // Configura RecyclerView y LayoutManager
        rvTasks = findViewById(R.id.rvTasks)
        rvTasks.layoutManager = LinearLayoutManager(this)

        // Observa las tareas
        tareaVM.listaTareas.observe(this) { list ->
            latestTasks = list
            trySetAdapter(latestTasks, latestCatMap)
        }

        // Observa el mapeo de categorías
        categoriaVM.categoriesMap.observe(this) { map ->
            latestCatMap = map
            trySetAdapter(latestTasks, latestCatMap)
        }

        // Cargar datos
        categoriaVM.cargarCategorias()
        tareaVM.cargarTareas()
    }

    private fun trySetAdapter(tasks: List<Tarea>, catMap: Map<String, String>) {
        if (tasks.isNotEmpty() && catMap.isNotEmpty()) {
            rvTasks.adapter = TareaAdapter(tasks, catMap) { task ->
                val intent = Intent(this, CreateTareaActivity::class.java)
                intent.putExtra("taskId", task.id)
                startActivity(intent)
            }
        }
    }
}