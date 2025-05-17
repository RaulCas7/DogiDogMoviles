package com.example.dogidog.inicioSesion

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.databinding.ActivityRegistroBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.MessageDigest

class RegistroActivity : AppCompatActivity() {

        lateinit var binding: ActivityRegistroBinding

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityRegistroBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.btnRegistrarse.setOnClickListener {
                registrarUsuario()
            }

            binding.btnInicioSesion.setOnClickListener {
                startActivity(Intent(this, InicioSesionActivity::class.java))
                finish()
            }
        }

        private fun registrarUsuario() {
            val email = binding.edtEmail.text.toString().trim()
            val usuario = binding.edtUsuario.text.toString().trim()
            val contra = binding.edtContra.text.toString().trim()
            val contraRepetida = binding.edtContraRepetir.text.toString().trim()

            if (email.isEmpty() || usuario.isEmpty() || contra.isEmpty() || contraRepetida.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return
            }

            if (contra != contraRepetida) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return
            }

            val contraMD5 = convertirAMD5(contra)

            val nuevoUsuario = Usuario(-1,usuario, email, contraMD5, 0, null, null,0,0) // contadorPreguntas = 0 por defecto

            val retrofit = Retrofit.Builder()
                .baseUrl("http://192.168.0.26:8080/dogidog/") // Dirección del backend
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(ApiService::class.java)
            val call = service.registrarUsuario(nuevoUsuario)

            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegistroActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        redirigirAInicioSesion()
                    } else {
                        Toast.makeText(this@RegistroActivity, "Error en el registro", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@RegistroActivity, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        private fun redirigirAInicioSesion() {
            startActivity(Intent(this, InicioSesionActivity::class.java))
            finish()
        }

    fun convertirAMD5(cadena: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val bytes = digest.digest(cadena.toByteArray())
        val sb = StringBuilder()
        for (byte in bytes) {
            sb.append(String.format("%02x", byte))
        }
        return sb.toString()
    }
}