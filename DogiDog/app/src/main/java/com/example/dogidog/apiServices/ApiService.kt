package com.example.dogidog.apiServices

import com.example.dogidog.dataModels.Logro
import com.example.dogidog.dataModels.Mascota
import com.example.dogidog.dataModels.Notificacion
import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.dataModels.UsuariosLogro
import com.example.dogidog.dataModels.Valoracion
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("usuarios") // Cambia por el endpoint correcto
    fun obtenerTodosLosUsuarios(): Call<List<Usuario>>
    @POST("usuarios")
    fun registrarUsuario(@Body usuario: Usuario): Call<Void>
    @GET("usuarios/email/{email}")
    fun verificarUsuario(@Path("email") email: String): Call<Usuario>

    @GET("usuarios/inicio")
    fun iniciarSesion(@Query("email") email: String, @Query("password") password: String): Call<Usuario>

    @GET("mascotas/usuario")
    fun obtenerMascotas(@Query("usuarioId") usuarioId: Int): Call<List<Mascota>>

    @GET("notificaciones/usuario")
    fun obtenerNotificacionesDeUsuario(@Query("usuarioId") usuarioId: Int): Call<MutableList<Notificacion>>

    @GET("logros")
    fun obtenerLogros(): Call<List<Logro>>

    @GET("usuarioLogros/usuario/{usuarioId}")
    fun buscarLogrosDeUsuario(@Path("usuarioId") usuarioId: Int): Call<List<UsuariosLogro>>

    @PUT("actualizar-coordenadas/{id}")
    fun actualizarCoordenadas(
        @Path("id") id: Int,
        @Query("latitud") latitud: Double,
        @Query("longitud") longitud: Double
    ): Call<Usuario>

    @PUT("usuarios/limpiar-coordenadas/{id}")
    fun limpiarCoordenadas(@Path("id") usuarioId: Int): Call<Usuario>

    @GET("valoraciones/usuario/{id}")
    fun obtenerValoracionesDeUsuario(@Path("id") id: Int): Call<List<Valoracion>>
}