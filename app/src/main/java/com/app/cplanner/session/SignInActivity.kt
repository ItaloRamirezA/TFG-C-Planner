    package com.app.cplanner.session

    import android.content.Intent
    import android.os.Bundle
    import android.view.View
    import android.widget.EditText
    import androidx.activity.enableEdgeToEdge
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat
    import com.app.cplanner.BaseActivity
    import com.app.cplanner.MainActivity
    import com.app.cplanner.R
    import com.google.android.gms.auth.api.signin.GoogleSignIn
    import com.google.android.gms.auth.api.signin.GoogleSignInAccount
    import com.google.android.gms.auth.api.signin.GoogleSignInClient
    import com.google.android.gms.auth.api.signin.GoogleSignInOptions
    import com.google.android.gms.common.api.ApiException
    import com.google.android.gms.tasks.Task
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.auth.GoogleAuthProvider

    /**
     * Clase para el inicio de sesión de usuarios.
     * @author Italo
     */
    class SignInActivity : BaseActivity() {
        private lateinit var auth: FirebaseAuth

        private lateinit var googleSignInClient: GoogleSignInClient

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(R.layout.activity_sign_in)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            // Inicializar Firebase Auth
            auth = FirebaseAuth.getInstance()

            // Configura las opciones de inicio de sesión
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            // Obténer la instancia del cliente
            googleSignInClient = GoogleSignIn.getClient(this, gso)
        }

        /**
         * Método para iniciar sesión al presionar el botón.
         */
        fun botonLogin(view: View) {
            signIn()
        }

        fun BotonLoginGoogle(view: View) {
            signInWithGoogle()
        }

        /**
         * Método para iniciar sesión con Google.
         */
        private fun signInWithGoogle() {
            // Cerrar la sesión anterior para forzar selector de cuenta
            googleSignInClient.signOut()
                .addOnCompleteListener(this) {
                    // Lanzar el selector de cuentas
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                }
        }

        /**
         * Método para registrar el resultado de la actividad de inicio de sesión.
         */
        private val launcher = registerForActivityResult( ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            }
        }

        /**
         * Método para manejar el resultado de la autenticación de Google.
         */
        private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    updateUI(account)
                }
            } catch (e: ApiException) {
                showToast(this, "Error al iniciar sesión: ${e.message}")
            }
        }

        /**
         * Método para actualizar la interfaz de usuario después de iniciar sesión.
         */
        private fun updateUI(account: GoogleSignInAccount) {
            showProgressBar()
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        // Inicio de sesión exitoso
                        hideProgressBar()
                        showToast(this, "Bienvenido de nuevo")
                        startActivity(Intent(this, MainActivity::class.java))
                        this.finish()
                    } else {
                        // Inicio de sesión fallido
                        hideProgressBar()
                        showToast(this, "Error al iniciar sesión: ${it.exception?.message}")
                    }
                }
        }

        /**
         * Método para iniciar sesión.
         */
        private fun signIn() {
            val email = findViewById<EditText>(R.id.etSignInEmail).text.toString()
            val password = findViewById<EditText>(R.id.etSignInPassword).text.toString()
            if (validateFormSignIn(email, password)) {
                showProgressBar()
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Inicio de sesión exitoso
                            hideProgressBar()
                            showToast(this, "Bienvenido de nuevo")
                            startActivity(Intent(this, MainActivity::class.java))
                            this.finish()
                        } else {
                            // Inicio de sesión fallido
                            hideProgressBar()
                            showToast(this, "Error al iniciar sesión: ${task.exception?.message}")
                        }
                    }
            }
        }

        /**
         * Método para validar el formulario de registro de usuario.
         */
        private fun validateFormSignIn(email: String, password: String): Boolean {
            return when {
                email.isEmpty() -> {
                    showToast(this, "El correo no puede estar vacío")
                    false
                }
                password.isEmpty() -> {
                    showToast(this, "La contraseña no puede estar vacía")
                    false
                }
                else -> true
            }
        }

        /**
         * Botón register.
         */
        fun botonRegister(view: View) {
            startActivity(Intent(this, SignUpActivity::class.java))
            this.finish()
        }

        /**
         * Botón forget password.
         */
        fun botonForgetPassword(view: View) {
            startActivity(Intent(this, ForgetPasswordActivity::class.java))
        }

    }