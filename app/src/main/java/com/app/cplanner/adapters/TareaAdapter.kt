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

    override fun getItemCount(): Int = tasks.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Fondo
        holder.card.setCardBackgroundColor(Color.parseColor(task.colorHex))

        // Nombre de la tarea (antes usabas task.titulo, ahora task.nombre)
        holder.name.text = task.titulo

        // Categoría
        // Mostrar nombre de categoría (o "-" si no lo encuentra)
        holder.category.text = categoryNames[task.categoryId] ?: "-"

        // Fecha solo si existe
        if (task.date.isBlank()) {
            holder.date.visibility = View.GONE
        } else {
            holder.date.visibility = View.VISIBLE
            holder.date.text = task.date
        }

        // Imagen si existe
//        if (task.imageUri.isNotBlank()) {
//            holder.thumbnail.visibility = View.VISIBLE
//            Glide.with(holder.thumbnail.context)
//                .load(task.imageUri)
//                .centerCrop()
//                .into(holder.thumbnail)
//        } else {
//            holder.thumbnail.visibility = View.GONE
//        }

        holder.itemView.setOnClickListener { onClick(task) }
    }
}
