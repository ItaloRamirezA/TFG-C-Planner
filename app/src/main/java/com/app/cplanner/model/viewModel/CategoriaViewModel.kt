package com.app.cplanner.model.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cplanner.model.entity.Categoria
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel para gestionar las categorías de cada usuario de forma aislada,
 * guardándolas en la subcolección /usuarios/{uid}/categorias.
 */
class CategoriaViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _listaCategorias = MutableLiveData<List<Categoria>>(emptyList())
    val listaCategorias: LiveData<List<Categoria>> = _listaCategorias

    init {
        cargarCategorias()
    }

    /**
     * Carga todas las categorías de la subcolección del usuario autenticado.
     */
    fun cargarCategorias() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snap = db.collection("usuarios")
                    .document(uid)
                    .collection("categorias")
                    .get()
                    .await()
                val cats = snap.documents
                    .mapNotNull { it.toObject(Categoria::class.java) }
                _listaCategorias.postValue(cats)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Refresca la lista (p. ej. en onResume de la UI) */
    fun refresh() {
        cargarCategorias()
    }

    /**
     * Agrega una nueva categoría en la subcolección del usuario actual.
     */
    fun addCategoria(categoria: Categoria) {
        val uid = auth.currentUser?.uid ?: return
        categoria.id = UUID.randomUUID().toString()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("usuarios")
                    .document(uid)
                    .collection("categorias")
                    .document(categoria.id)
                    .set(categoria.toMap())
                    .await()
                // Actualizar lista local
                _listaCategorias.postValue(_listaCategorias.value.orEmpty() + categoria)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Actualiza una categoría existente en la subcolección del usuario actual.
     */
    fun updateCategoria(categoria: Categoria) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("usuarios")
                    .document(uid)
                    .collection("categorias")
                    .document(categoria.id)
                    .update(categoria.toMap())
                    .await()
                // Reemplazar en la lista local
                val updated = _listaCategorias.value.orEmpty().map {
                    if (it.id == categoria.id) categoria else it
                }
                _listaCategorias.postValue(updated)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Elimina una categoría por su ID en la subcolección del usuario actual.
     */
    fun deleteCategoria(id: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("usuarios")
                    .document(uid)
                    .collection("categorias")
                    .document(id)
                    .delete()
                    .await()
                // Filtrar la lista local
                val filtered = _listaCategorias.value.orEmpty()
                    .filter { it.id != id }
                _listaCategorias.postValue(filtered)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
