package com.app.cplanner.model.viewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cplanner.model.entity.Categoria
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import java.util.UUID

// TODO
class CategoriaViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // LiveData interna y externa
    private val _listaCategorias = MutableLiveData<List<Categoria>>(emptyList())
    val listaCategorias: LiveData<List<Categoria>> = _listaCategorias

    init {
        cargarCategorias()
    }

    /** Carga todas las categorías de Firestore */
    private fun cargarCategorias() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snap = db.collection("categorias").get().await()
                val cats = snap.documents
                    .mapNotNull { it.toObject(Categoria::class.java) }
                _listaCategorias.postValue(cats)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Refresca manualmente la lista (p. ej. en onResume) */
    fun refresh() {
        cargarCategorias()
    }

    /** Agrega una nueva categoría */
    fun addCategoria(categoria: Categoria) {
        // Generar ID único
        categoria.id = UUID.randomUUID().toString()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("categorias")
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

    /** Actualiza una categoría existente */
    fun updateCategoria(categoria: Categoria) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("categorias")
                    .document(categoria.id)
                    .update(categoria.toMap())
                    .await()
                // Reemplazar en la lista local
                _listaCategorias.postValue(
                    _listaCategorias.value.orEmpty().map {
                        if (it.id == categoria.id) categoria else it
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Elimina una categoría por ID */
    fun deleteCategoria(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("categorias")
                    .document(id)
                    .delete()
                    .await()
                // Filtrar la lista local
                _listaCategorias.postValue(
                    _listaCategorias.value.orEmpty().filter { it.id != id }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
