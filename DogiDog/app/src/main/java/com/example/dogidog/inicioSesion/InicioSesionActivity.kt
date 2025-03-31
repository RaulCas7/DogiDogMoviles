package com.example.dogidog.inicioSesion

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import com.example.dogidog.principal.PantallaPrincipalActivity
import com.example.dogidog.R
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val GOOGLE_SIGN_IN = 100 // Asegúrate de que este valor sea único
class InicioSesionActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var usuario: Usuario? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)

        val id = prefs.getInt("usuario_id", -1) // ID del usuario
        val usuario = prefs.getString("usuario", null) // Nombre de usuario
        val email = prefs.getString("usuario_email", null) // Email del usuario
        val password = prefs.getString("usuario_password", null) // Contraseña del usuario
        val provider = prefs.getString("provider", null) // Proveedor de autenticación (Google, Local, etc.)

        if (id != -1 && usuario != null && email != null && password != null) {
            Log.d("SharedPreferences", "Usuario encontrado: ID=$id, Nombre=$usuario, Email=$email, Provider=$provider")
            redirigirAPantallaPrincipal()
        } else {
            Log.w("SharedPreferences", "No hay sesión activa o datos incompletos en SharedPreferences")
        }

        binding.btnInicioGoogle.setOnClickListener {
            iniciarGoogleSignIn()
        }

        binding.btnInicioSesion.setOnClickListener {
            val emailInput = binding.edtEmail.text.toString().trim()
            val passwordInput = binding.edtContra.text.toString().trim()

            if (emailInput.isEmpty() || passwordInput.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese ambos campos", Toast.LENGTH_SHORT).show()
            } else {
                iniciarSesionNormal(emailInput, passwordInput)
            }
        }

        binding.btnMostrarContra.setOnClickListener {
            if (binding.edtContra.transformationMethod == PasswordTransformationMethod.getInstance()) {
                binding.edtContra.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.btnMostrarContra.setImageResource(R.drawable.visibility_24)
            } else {
                binding.edtContra.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.btnMostrarContra.setImageResource(R.drawable.visibility_off_24)
            }
        }

        binding.btnRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun iniciarGoogleSignIn() {
        Log.d("GoogleSignIn", "Botón de Google clickeado")
        val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        val googleClient = GoogleSignIn.getClient(this, googleConf)

        // **Cerrar sesión para forzar el selector de cuentas**
        googleClient.signOut().addOnCompleteListener {
            Log.d("GoogleSignIn", "Cerrando sesión antes de iniciar selección")
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val email = account?.email ?: ""

                if (email.isNotEmpty()) {
                    verificarUsuarioEnAPI(email)
                } else {
                    Toast.makeText(this, "Error al obtener el correo", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Error: ${e.statusCode} - ${e.message}")
                Toast.makeText(this, "Error al seleccionar cuenta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun iniciarSesionNormal(email: String, password: String) {
        // Lógica para verificar las credenciales del usuario
        // En este caso, puedes verificar las credenciales contra una API o base de datos local.
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/") // Asegúrate de usar la URL correcta
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val call = service.iniciarSesion(email, password)

        call.enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful && response.body() != null) {
                    usuario = response.body()
                    // Guardar datos de usuario localmente
                    guardarUsuarioLocal(usuario)
                    redirigirAPantallaPrincipal()
                } else {
                    Log.w("API", "Usuario o contraseña incorrectos")
                    Toast.makeText(this@InicioSesionActivity, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                Log.e("API", "Error de conexión: ${t.message}")
                Toast.makeText(this@InicioSesionActivity, "Error de conexión con el servidor: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun verificarUsuarioEnAPI(email: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val call = service.verificarUsuario(email)

        call.enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful && response.body() != null) {
                    usuario = response.body()
                    guardarUsuarioLocal(usuario)
                    redirigirAPantallaPrincipal()
                } else {
                    Log.w("API", "Usuario no encontrado en la base de datos")
                    Toast.makeText(this@InicioSesionActivity, "El usuario no está registrado", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                Log.e("API", "Error de conexión: ${t.message}")
                t.printStackTrace()
                Toast.makeText(this@InicioSesionActivity, "Error de conexión con el servidor: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun guardarUsuarioLocal(usuario: Usuario?) {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        if (usuario != null) {
            prefs.putInt("usuario_id", usuario.id)
            prefs.putString("usuario", usuario.usuario)
            prefs.putString("usuario_email", usuario.email)
            prefs.putString("usuario_password", usuario.password)
            prefs.putString("provider", "LOCAL")
            prefs.commit()

            // Verificar si los datos se guardaron correctamente
            Log.d("SharedPreferences", "Usuario guardado: ${usuario.usuario}, ID: ${usuario.id}, Email: ${usuario.email}")
        } else {
            Log.w("SharedPreferences", "Intento de guardar un usuario nulo")
        }
    }

    private fun redirigirAPantallaPrincipal() {
        startActivity(Intent(this, PantallaPrincipalActivity::class.java))
        finish()
    }

}


