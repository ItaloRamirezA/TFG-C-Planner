package com.app.cplanner.model.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cplanner.model.entity.Lista
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ListaViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _listas = MutableLiveData<List<Lista>>(emptyList())
    val listas: LiveData<List<Lista>> get() = _listas

    init {
        cargarListas()
    }

    fun cargarListas() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("usuarios")
                    .document(userId)
                    .collection("listas")
                    .get()
                    .await()
                val listas = snapshot.documents.mapNotNull { it.toObject(Lista::class.java) }
                _listas.postValue(listas)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addLista(lista: Lista) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val listaId = db.collection("usuarios")
                    .document(userId)
                    .collection("listas")
                    .document().id
                val nuevaLista = lista.copy(id = listaId)

                db.collection("usuarios")
                    .document(userId)
                    .collection("listas")
                    .document(listaId)
                    .set(nuevaLista)
                    .await()

                _listas.postValue(_listas.value.orEmpty() + nuevaLista)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateLista(updatedLista: Lista) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("usuarios")
                    .document(userId)
                    .collection("listas")
                    .document(updatedLista.id)
                    .set(updatedLista)
                    .await()

                _listas.postValue(
                    _listas.value.orEmpty().map { if (it.id == updatedLista.id) updatedLista else it }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteLista(listaId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("usuarios")
                    .document(userId)
                    .collection("listas")
                    .document(listaId)
                    .delete()
                    .await()

                _listas.postValue(_listas.value.orEmpty().filter { it.id != listaId })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}