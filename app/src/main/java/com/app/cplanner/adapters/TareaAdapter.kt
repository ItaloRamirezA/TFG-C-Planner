package com.app.cplanner.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.cplanner.R
import com.app.cplanner.model.entity.Tarea
import com.bumptech.glide.Glide

/**
 * Adapter para mostrar una lista de tareas en un RecyclerView.
 *
 * @param tasks Lista de tareas a mostrar.
 * @param onClick Función a ejecutar al hacer clic en una tarea.
 */
class TareaAdapter(
    private val tasks: List<Tarea>,
    private val categoryNames: Map<String,String>,
    private val onClick: (Tarea) -> Unit
) : RecyclerView.Adapter<TareaAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val card     : CardView   = item.findViewById(R.id.cardView)
        val name     : TextView  = item.findViewById(R.id.tvTaskName)
        val category : TextView  = item.findViewById(R.id.tvTaskCategory)
        val date     : TextView  = item.findViewById(R.id.tvTaskDate)
        val thumbnail: ImageView = item.findViewById(R.id.ivTaskThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarea, parent, false)
        return TaskViewHolder(view)
    }

    // Cantidad de items será la cantidad de tareas que tiene el usuario
    override fun getItemCount(): Int = tasks.size

    /**
     * Asigna los valores a cada vista de la tarjeta de tarea.
     *
     * @param holder ViewHolder que contiene las vistas de la tarjeta.
     * @param position Posición de la tarea en la lista.
     */
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        // Variable que almacena la tarea en la posición actual
        val task = tasks[position]

        // Campos obligatorios
        colorFondo(holder, task)
        mostrarNombre(holder, task)

        // Campos opcionales
        mostrarCategoria(holder, task)
        mostrarFecha(holder, task)
        edicionTarea()
        mostrarArchivos(holder, task)
    }

    /** TODO
     * Muestra la vista de edición de la tarea al hacer clic en ella.
     */
    fun edicionTarea() {

    }

    /**
     * Cambia el color de fondo de la tarjeta según el color elegido por el usuario.
     */
    fun colorFondo(holder: TaskViewHolder, task: Tarea) {
        holder.card.setCardBackgroundColor(Color.parseColor(task.colorHex))
    }

    /**
     * Muestra el nombre de la tarea.
     */
    fun mostrarNombre(holder: TaskViewHolder, task: Tarea) {
        holder.name.text = "Título: \"${task.titulo}\""
    }

    /**
     * Muestra la categoría de una tarea solo si existe.
     */
    fun mostrarCategoria(holder: TaskViewHolder, task: Tarea) {
        if (task.categoryId.isBlank()) {
            holder.category.visibility = View.GONE
        } else {
            holder.category.visibility = View.VISIBLE
            holder.category.text = "Categoría: \"${categoryNames[task.categoryId]}\""
        }
    }

    /**
     * Muestra la fecha de una tarea solo si existe.
     */
    fun mostrarFecha(holder: TaskViewHolder, task: Tarea) {
        if (task.startDate.isBlank()) {
            holder.date.visibility = View.GONE
        } else {
            holder.date.visibility = View.VISIBLE
            holder.date.text = if (task.multiDay) {
                "Inicio: ${task.startDate}\nFin: ${task.endDate}"
            } else {
                "Fecha: ${task.startDate}"
            }
        }
    }

    /**
     * Muestra los archivos adjuntos de una tarea solo si existen.
     */
    fun mostrarArchivos(holder: TaskViewHolder, task: Tarea) {
        if (task.attachments.isNotEmpty()) {
            holder.thumbnail.visibility = View.VISIBLE
            Glide.with(holder.thumbnail.context)
                .load(task.attachments.first()) // Muestra el primer archivo como ejemplo
                .into(holder.thumbnail)
        } else {
            holder.thumbnail.visibility = View.GONE
        }
    }
}