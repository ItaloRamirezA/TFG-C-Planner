package com.app.cplanner.model.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cplanner.model.entity.Tarea
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TareaViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _listaTareas = MutableLiveData<List<Tarea>>(emptyList())
    val listaTareas: LiveData<List<Tarea>> = _listaTareas

    init {
        cargarTareas()
    }

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
            } catch(e: Exception) { e.printStackTrace() }
        }
    }

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
            } catch(e: Exception) { e.printStackTrace() }
        }
    }
}
