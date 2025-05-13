package com.app.cplanner.model.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cplanner.model.entity.Tarea
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

class TareaViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _listaTareas = MutableLiveData<List<Tarea>>(emptyList())
    val listaTareas: LiveData<List<Tarea>> = _listaTareas

    init {
        cargarTareas()
    }

    /** Carga las tareas de la subcolección del usuario autenticado */
    fun cargarTareas() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snap = db.collection("usuarios")
                    .document(uid)
                    .collection("tareas")
                    .get()
                    .await()
                val tareas = snap.documents.mapNotNull { it.toObject(Tarea::class.java) }
                _listaTareas.postValue(tareas)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Agrega una nueva tarea a la subcolección del usuario */
    fun addTarea(tarea: Tarea) {
        val uid = auth.currentUser?.uid ?: return
        tarea.id = db.collection("usuarios")
            .document(uid)
            .collection("tareas")
            .document().id
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("usuarios")
                    .document(uid)
                    .collection("tareas")
                    .document(tarea.id)
                    .set(tarea.toMap())
                    .await()
                _listaTareas.postValue(_listaTareas.value.orEmpty() + tarea)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Actualiza una tarea existente en la subcolección del usuario */
    fun updateTarea(tarea: Tarea) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("usuarios")
                    .document(uid)
                    .collection("tareas")
                    .document(tarea.id)
                    .update(tarea.toMap())
                    .await()
                _listaTareas.postValue(
                    _listaTareas.value.orEmpty().map {
                        if (it.id == tarea.id) tarea else it
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Elimina una tarea de la subcolección del usuario */
    fun deleteTarea(id: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("usuarios")
                    .document(uid)
                    .collection("tareas")
                    .document(id)
                    .delete()
                    .await()
                _listaTareas.postValue(_listaTareas.value.orEmpty().filter { it.id != id })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
