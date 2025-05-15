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

/**
 * ViewModel para manejar la lógica de negocio relacionada con las tareas.
 *
 * @property auth FirebaseAuth para autenticación de usuarios.
 * @property db FirebaseFirestore para acceso a la base de datos Firestore.
 */
class TareaViewModel : ViewModel() {
    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    // LiveData para almacenar la lista de tareas
    private val _listaTareas = MutableLiveData<List<Tarea>>(emptyList())
    val listaTareas: LiveData<List<Tarea>> = _listaTareas

    init {
        cargarTareas()
    }

    /**
     * Carga las tareas del usuario actual desde Firestore.
     */
    fun cargarTareas() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snap = db.collection("usuarios")
                    .document(uid)
                    .collection("tareas")
                    .whereEqualTo("deleted", false) // Exclude deleted tasks
                    .get()
                    .await()
                val tareas = snap.documents.mapNotNull { it.toObject(Tarea::class.java) }
                _listaTareas.postValue(tareas)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Agrega una nueva tarea a la base de datos.
     *
     * @param tarea La tarea a agregar.
     */
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

    /**
     * ACtualiza una tarea de la base de datos solo
     * si hay un usuario autenticado.
     *
     * @param tarea La tarea a eliminar.
     */
    fun updateTarea(tarea: Tarea) {
        // Si no hay usuario autenticado, no se puede actualizar la tarea
        val uid = auth.currentUser?.uid ?: return

        // Actualiza la tarea en Firestore
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

    fun moveToTrash(tarea: Tarea) {
        val uid = auth.currentUser?.uid ?: return
        tarea.deleted = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("usuarios")
                    .document(uid)
                    .collection("tareas")
                    .document(tarea.id)
                    .update("deleted", true)
                    .await()

                _listaTareas.postValue(
                    _listaTareas.value.orEmpty().filter { it.id != tarea.id }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getDeletedTasks() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snap = db.collection("usuarios")
                    .document(uid)
                    .collection("tareas")
                    .whereEqualTo("deleted", true)
                    .get()
                    .await()
                val deletedTasks = snap.documents.mapNotNull { it.toObject(Tarea::class.java) }
                _listaTareas.postValue(deletedTasks)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
