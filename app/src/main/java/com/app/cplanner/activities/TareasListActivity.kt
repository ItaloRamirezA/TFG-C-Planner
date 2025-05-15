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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.app.cplanner.R
import com.app.cplanner.model.entity.Tarea
import com.app.cplanner.model.viewModel.CategoriaViewModel
import com.app.cplanner.model.viewModel.TareaViewModel
import com.app.cplanner.ui.TareaAdapter

class TareasListActivity : AppCompatActivity() {

    // ViewModels
    private val tareaVM      by viewModels<TareaViewModel>()
    private val categoriaVM  by viewModels<CategoriaViewModel>()

    // RecyclerView
    private lateinit var rvTasks: RecyclerView

    // SwipeRefreshLayout para refrescar la vista
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tareas_list)

        // Ajuste de padding para barras de sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Configurar SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            // Recargar datos
            categoriaVM.cargarCategorias()
            tareaVM.cargarTareas()

            // Detener el indicador de carga después de actualizar
            swipeRefreshLayout.isRefreshing = false
        }

        // 1) Configurar RecyclerView y LayoutManager
        rvTasks = findViewById(R.id.rvTasks)
        rvTasks.layoutManager = LinearLayoutManager(this)

        // Variables locales para cachear emisiones
        var latestTasks  = emptyList<Tarea>()
        var latestCatMap = emptyMap<String,String>()

        // Helper para inicializar Adapter cuando tengamos BOTH listas
        fun trySetAdapter() {
            if (latestTasks.isNotEmpty() || latestCatMap.isNotEmpty()) {
                rvTasks.adapter = TareaAdapter(latestTasks, latestCatMap) { task ->
                    // Al pulsar un ítem, vamos a la pantalla de edición/visualización
                    val intent = Intent(this, CreateTareaActivity::class.java).apply {
                        putExtra("taskId", task.id)
                    }
                    startActivity(intent)
                }
            }
        }

        // 2) Observar lista de tareas
        tareaVM.listaTareas.observe(this) { list ->
            latestTasks = list
            trySetAdapter()
        }

        // 3) Observar mapa id→nombre de categorías
        categoriaVM.categoriesMap.observe(this) { map ->
            latestCatMap = map
            trySetAdapter()
        }

        // 4) Disparar lectura inicial
        categoriaVM.cargarCategorias()
        tareaVM.cargarTareas()
    }
}
