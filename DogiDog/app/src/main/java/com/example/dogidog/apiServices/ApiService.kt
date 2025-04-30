package com.example.dogidog.apiServices

import com.example.dogidog.dataModels.Documentacion
import com.example.dogidog.dataModels.DocumentacionCrear
import com.example.dogidog.dataModels.Logro
import com.example.dogidog.dataModels.Mascota
import com.example.dogidog.dataModels.MascotaCrear
import com.example.dogidog.dataModels.Notificacion
import com.example.dogidog.dataModels.Raza
import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.dataModels.UsuariosLogro
import com.example.dogidog.dataModels.Valoracion
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("usuarios") // Cambia por el endpoint correcto
    fun obtenerTodosLosUsuarios(): Call<List<Usuario>>

    @GET("razas")
    fun obtenerTodasLasRazas(): Call<List<Raza>>
    @DELETE("mascotas/{id}")
    fun eliminarMascota(@Path("id") id: Int): Call<Void> // Método para eliminar la mascota

    @POST("mascotas")
    fun guardarMascota(@Body mascota: MascotaCrear): Call<Void>
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

    @Multipart
    @POST("documentacion/guardar")
    fun guardarDocumentacion(
        @Part("idmascota") idmascota: Int,
        @Part("documentacion") documentacionJson: RequestBody,
        @Part archivo: MultipartBody.Part
    ): Call<DocumentacionCrear>

    @POST("documentacion")
    fun guardarDocumentacionSinArchivo(
        @Body documentacion: DocumentacionCrear
    ): Call<DocumentacionCrear>

    @GET("documentacion/mascota")
    fun obtenerDocumentacionPorMascota(
        @Query("mascotaId") mascotaId: Int
    ): Call<List<Documentacion>>

    @DELETE("documentacion/{id}")
    fun eliminarDocumentacion(@Path("id") id: Int): Call<Void>

    @PUT("mascotas/{id}")
    fun actualizarMascota(@Path("id") id: Int, @Body mascota: Mascota): Call<Mascota>
}