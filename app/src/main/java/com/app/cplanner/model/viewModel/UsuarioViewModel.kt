package com.app.cplanner.model.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cplanner.model.entity.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

class UsuarioViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    private val _usuario = MutableLiveData<Usuario?>()
    val usuario: LiveData<Usuario?> = _usuario

    init {
        cargarUsuario()
    }

    /** 1) Carga el documento /usuarios/{uid} y lo publica. */
    private fun cargarUsuario() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snap = db.collection("usuarios").document(uid).get().await()
                val u = snap.toObject(Usuario::class.java)
                _usuario.postValue(u)
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** 2) Actualiza nombre y/o email en Firestore (y en Auth si cambias email). */
    fun updateProfile(nombre: String, email: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Si el email cambia, actualízalo también en FirebaseAuth
                if (email != auth.currentUser?.email) {
                    auth.currentUser?.updateEmail(email)?.await()
                }
                // Update en Firestore
                db.collection("usuarios").document(uid)
                    .update(mapOf("nombre" to nombre, "email" to email))
                    .await()

                // Refrescar LiveData local
                val updated = _usuario.value?.copy(nombre = nombre, email = email)
                _usuario.postValue(updated)
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** 3) Cambia la contraseña en FirebaseAuth. */
    fun changePassword(nueva: String) {
        auth.currentUser?.updatePassword(nueva)
            ?.addOnFailureListener { it.printStackTrace() }
    }

    /** 4) Sube foto al Storage y guarda la URL en Firestore + LiveData */
    fun updatePhoto(photoUri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ref = storage.child("usuarios/$uid/profile.jpg")
                ref.putFile(photoUri).await()
                val url = ref.downloadUrl.await().toString()

                db.collection("usuarios").document(uid)
                    .update("fotoUrl", url)
                    .await()

                val updated = _usuario.value?.copy(fotoUrl = url)
                _usuario.postValue(updated)
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
