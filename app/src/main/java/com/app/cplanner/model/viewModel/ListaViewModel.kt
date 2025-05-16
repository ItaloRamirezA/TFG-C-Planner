package com.app.cplanner.model.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.cplanner.model.entity.Lista

class ListaViewModel : ViewModel() {
    private val _listas = MutableLiveData<List<Lista>>(emptyList())
    val listas: LiveData<List<Lista>> get() = _listas

    fun addLista(lista: Lista) {
        val currentListas = _listas.value?.toMutableList() ?: mutableListOf()
        currentListas.add(lista)
        _listas.value = currentListas
    }

    fun updateLista(updatedLista: Lista) {
        val currentListas = _listas.value?.toMutableList() ?: mutableListOf()
        val index = currentListas.indexOfFirst { it.id == updatedLista.id }
        if (index != -1) {
            currentListas[index] = updatedLista
            _listas.value = currentListas
        }
    }

    fun deleteLista(listaId: String) {
        val currentListas = _listas.value?.toMutableList() ?: mutableListOf()
        currentListas.removeAll { it.id == listaId }
        _listas.value = currentListas
    }
}