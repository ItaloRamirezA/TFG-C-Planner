package com.app.cplanner.session

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.app.cplanner.BaseActivity
import com.app.cplanner.MainActivity
import com.app.cplanner.R
import com.app.cplanner.model.entity.Usuario
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Clase para el registro de nuevos usuarios,
 * que unifica email/password y Google si ya existe la cuenta.
 */
class SignUpActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        // Configurar GoogleSignInClient por si hay que vincular
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)
    }

    /** Llamado desde el botón “Registrarse” */
    fun botonSignUp(view: View) {
        val name     = findViewById<EditText>(R.id.etSignUpName).text.toString().trim()
        val email    = findViewById<EditText>(R.id.etSignUpEmail).text.toString().trim()
        val password = findViewById<EditText>(R.id.etSignUpPassword).text.toString()

        if (!validateFormSignUp(name, email, password)) return

        showProgressBar()
        // Comprueba si el email ya existe y con qué método
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                hideProgressBar()
                if (!task.isSuccessful) {
                    showToast(this, "Error comprobando métodos: ${task.exception?.message}")
                    return@addOnCompleteListener
                }
                val methods = task.result?.signInMethods.orEmpty()
                if (methods.contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD)) {
                    // Ya existe con Google → vincular email/password
                    linkEmailToGoogle(email, password, name)
                } else {
                    // No existe → crear cuenta nueva
                    createEmailUser(email, password, name)
                }
            }
    }

    /** Registro estándar email/password */
    private fun createEmailUser(email: String, password: String, name: String) {
        showProgressBar()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                hideProgressBar()
                if (task.isSuccessful) {
                    // Guardar perfil en Firestore
                    val user = auth.currentUser!!
                    val perfil = Usuario(
                        id         = user.uid,
                        nombre     = name,
                        email      = email,
                        contrasena = password,
                        fotoUrl    = ""
                    )
                    db.collection("usuarios")
                        .document(user.uid)
                        .set(perfil.toMap())
                        .addOnSuccessListener {
                            showToast(this, "Usuario creado exitosamente")
                            goToMain()
                        }
                        .addOnFailureListener { e ->
                            showToast(this, "Error guardando perfil: ${e.message}")
                        }
                } else {
                    val ex = task.exception
                    if (ex is FirebaseAuthUserCollisionException) {
                        // Collision: email ya en uso, vincular credenciales
                        linkEmailToGoogle(email, password, name)
                    } else {
                        showToast(this, "Error al registrar: ${ex?.message}")
                    }
                }
            }
    }

    /**
     * Vincula email/password a la cuenta Google existente.
     * Requiere haber iniciado sesión con Google previamente.
     */
    private fun linkEmailToGoogle(email: String, password: String, name: String) {
        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct == null) {
            showToast(this, "Inicia sesión primero con Google para vincular.")
            return
        }
        val googleCred = GoogleAuthProvider.getCredential(acct.idToken, null)
        showProgressBar()
        auth.signInWithCredential(googleCred).addOnCompleteListener(this) { signInTask ->
            if (!signInTask.isSuccessful) {
                hideProgressBar()
                showToast(this, "Error iniciando Google: ${signInTask.exception?.message}")
                return@addOnCompleteListener
            }
            // Ahora enlazamos la credencial email/password
            val emailCred = EmailAuthProvider.getCredential(email, password)
            auth.currentUser?.linkWithCredential(emailCred)
                ?.addOnCompleteListener { linkTask ->
                    hideProgressBar()
                    if (linkTask.isSuccessful) {
                        // Actualizar Firestore con contraseña
                        db.collection("usuarios")
                            .document(auth.currentUser!!.uid)
                            .update("contrasena", password)
                        showToast(this, "Email vinculado exitosamente")
                        goToMain()
                    } else {
                        showToast(this, "Error vinculando email: ${linkTask.exception?.message}")
                    }
                }
        }
    }

    /** Navega a MainActivity y cierra esta pantalla */
    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /** Valida formulario de registro */
    private fun validateFormSignUp(name: String, email: String, password: String): Boolean {
        return when {
            name.isEmpty() -> {
                showToast(this, "El nombre no puede estar vacío"); false
            }
            email.isEmpty() -> {
                showToast(this, "El correo no puede estar vacío"); false
            }
            password.isEmpty() -> {
                showToast(this, "La contraseña no puede estar vacía"); false
            }
            else -> true
        }
    }
}
