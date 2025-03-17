package com.example.dogidog

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.databinding.ActivityMainBinding
import com.example.dogidog.responseModels.UsuarioResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.logging.Logger
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.log

private const val GOOGLE_SIGN_IN = 100 // Asegúrate de que este valor sea único
class InicioSesionActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        if (email != null && provider != null) {
            redirigirAPantallaPrincipal()
        } else {
            Log.d("PantallaPrincipal", "No hay sesión activa")
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
                    // Guardar datos de usuario localmente
                    guardarUsuarioLocal(email)
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
                    guardarUsuarioLocal(email)
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

    private fun guardarUsuarioLocal(email: String) {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", "LOCAL")
        prefs.apply()
    }

    private fun redirigirAPantallaPrincipal() {
        startActivity(Intent(this, PantallaPrincipalActivity::class.java))
        finish()
    }
}


