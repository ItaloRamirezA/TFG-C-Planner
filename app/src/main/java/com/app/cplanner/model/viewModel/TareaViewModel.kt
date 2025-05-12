package com.app.cplanner.model.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.cplanner.model.entity.Tarea
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.UUID

class TareaViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private var _listaTareas: MutableLiveData<List<Tarea>> = MutableLiveData(emptyList())
    val listaTareas: LiveData<List<Tarea>> = _listaTareas

    init {
        getTareas()
    }

    /**
     * Obtiene la lista de tareas de la base de datos.
     */
    private fun getTareas() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resultado = db.collection("tareas").get().await()

                val tareas = resultado.documents.mapNotNull { document -> document.toObject(Tarea::class.java) }
                _listaTareas.postValue(tareas)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Obtiene una tarea por su ID.
     */
    private fun getTareaById(id: String): Tarea? {
        return _listaTareas.value?.find { it.id == id }
    }

    /**
     * Agrega una nueva tarea a la base de datos.
     */
    fun addTarea(tarea: Tarea) {
        tarea.id = UUID.randomUUID().toString()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("tareas").document(tarea.id).set(tarea).await()
                _listaTareas.postValue(_listaTareas.value?.plus(tarea))

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Actualiza una tarea existente en la base de datos.
     */
    fun updateTarea(tarea: Tarea) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("tareas").document(tarea.id).update(tarea.toMap()).await()
                _listaTareas.postValue(_listaTareas.value?.map {
                    if (it.id == tarea.id) {
                        tarea
                    } else {
                        it
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Elimina una tarea definitivamente de la base de datos.
     */
    fun deleteTarea(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("tareas").document(id).delete().await()
                _listaTareas.postValue(_listaTareas.value?.filter { it.id != id })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}