package com.example.dogidog.apiServices

import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.responseModels.UsuarioResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("usuarios")
    fun registrarUsuario(@Body usuario: Usuario): Call<Void>
    @GET("usuarios/email/{email}")
    fun verificarUsuario(@Path("email") email: String): Call<Usuario>

    @GET("usuarios/inicio")
    fun iniciarSesion(@Query("email") email: String, @Query("password") password: String): Call<Usuario>
}