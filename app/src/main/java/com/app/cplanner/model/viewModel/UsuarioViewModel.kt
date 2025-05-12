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

/**
 * ViewModel para manejar la lógica de negocio del usuario.
 * Contiene métodos para cargar, actualizar y eliminar el perfil del usuario.
 */
class UsuarioViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    private val _usuario = MutableLiveData<Usuario?>()
    val usuario: LiveData<Usuario?> = _usuario

    init {
        cargarUsuario()
    }

    /**
     * Carga el usuario actual desde Firestore y lo publica en el LiveData.
     * Se llama al iniciar la actividad o fragmento.
     */
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

    /**
     * Actualiza el nombre y email del usuario en Firestore y FirebaseAuth.
     */
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

    /**
     * Cambia la contraseña del usuario en FirebaseAuth.
     */
    fun changePassword(nueva: String) {
        auth.currentUser?.updatePassword(nueva)
            ?.addOnFailureListener { it.printStackTrace() }
    }

    /**
     * Actualiza la foto de perfil del usuario en Firestore y Firebase Storage.
     */
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
