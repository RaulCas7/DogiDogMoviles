package com.example.dogidog.apiServices

import com.example.dogidog.dataModels.Documentacion
import com.example.dogidog.dataModels.DocumentacionCrear
import com.example.dogidog.dataModels.Logro
import com.example.dogidog.dataModels.Mascota
import com.example.dogidog.dataModels.MascotaCrear
import com.example.dogidog.dataModels.Notificacion
import com.example.dogidog.dataModels.PesoMascota
import com.example.dogidog.dataModels.Pregunta
import com.example.dogidog.dataModels.Raza
import com.example.dogidog.dataModels.Recorrido
import com.example.dogidog.dataModels.Tarea
import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.dataModels.UsuariosLogro
import com.example.dogidog.dataModels.Valoracion
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ApiService {

    @GET("usuarios") // Cambia por el endpoint correcto
    fun obtenerTodosLosUsuarios(): Call<List<Usuario>>

    @GET("razas")
    fun obtenerTodasLasRazas(): Call<List<Raza>>
    @DELETE("mascotas/{id}")
    fun eliminarMascota(@Path("id") id: Int): Call<Void> // Método para eliminar la mascota

    @POST("mascotas")
    fun guardarMascota(@Body mascota: MascotaCrear): Call<Mascota>
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
    @PUT("documentacion/{id}/archivo")
    fun actualizarArchivoDocumentacion(
        @Path("id") id: Int,  // id como parámetro de la URL
        @Part fichero: MultipartBody.Part  // El archivo se pasa como MultipartBody.Part
    ): Call<String>

    @POST("documentacion")
    fun guardarDocumentacionSinArchivo(
        @Body documentacion: DocumentacionCrear
    ): Call<DocumentacionCrear>

    @GET("documentacion/{id}/archivo")
    @Streaming
    fun descargarArchivoDocumentacion(@Path("id") id: Int): Call<ResponseBody>

    @GET("documentacion/mascota")
    fun obtenerDocumentacionPorMascota(
        @Query("mascotaId") mascotaId: Int
    ): Call<List<Documentacion>>

    @DELETE("documentacion/{id}")
    fun eliminarDocumentacion(@Path("id") id: Int): Call<Void>

    @PUT("mascotas/{id}")
    fun actualizarMascota(@Path("id") id: Int, @Body mascota: Mascota): Call<Mascota>

    @GET("pesosmascota/mascota")
    fun obtenerPesosMascota(@Query("mascotaId") mascotaId: Int): Call<List<PesoMascota>>

    @Multipart
    @PUT("mascotas/{id}/foto")
    fun subirFotoMascota(
        @Path("id") idMascota: Int,
        @Part fichero: MultipartBody.Part
    ): Call<String>

    @GET("mascotas/{id}/foto")
    fun getFotoMascota(@Path("id") id: Int): Call<ResponseBody>

    @GET("preguntas/buscar")
    fun buscarPregunta(@Query("texto") texto: String): Call<Pregunta>

    @PUT("usuarios/{id}")
    fun actualizarUsuario(@Path("id") id: Int, @Body usuario: Usuario): Call<Usuario>

    @GET("logros/{id}/emblema")
    fun obtenerEmblemaLogro(@Path("id") id: Int): Call<ResponseBody>

    @DELETE("usuarios/{id}")
    fun eliminarUsuario(@Path("id") id: Int): Call<Void>

    @POST("recorridos")
    fun guardarRecorrido(@Body recorrido: Recorrido): Call<Int>

    @PUT("notificaciones/{id}")
    fun actualizarNotificacion(
        @Path("id") id: Int,
        @Body notificacion: Notificacion
    ): Call<Notificacion>

    @PUT("recorridos/{id}")
    fun actualizarRecorrido(
        @Path("id") id: Int,
        @Body recorrido: Recorrido
    ): Call<Recorrido>

    @GET("recorridos/activos") // Asegúrate de que esta URL sea correcta en tu servidor
    fun obtenerRecorridosActivos(): Call<List<Recorrido>>

    @POST("valoraciones")
    fun enviarValoracion(@Body valoracion: Valoracion): Call<Valoracion>

    @DELETE("notificaciones/{id}")
    fun eliminarNotificacion(@Path("id") id: Int): Call<Void>

    @POST("tareas")
    fun guardarTarea(@Body tarea: Tarea): Call<Tarea>
}