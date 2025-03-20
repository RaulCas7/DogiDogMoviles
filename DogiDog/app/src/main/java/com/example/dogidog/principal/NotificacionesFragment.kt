package com.example.dogidog.principal
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dogidog.R
import com.example.dogidog.adapters.NotificacionesAdapter
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Notificacion
import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.databinding.FragmentNotificacionesGeneralBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NotificacionesFragment : Fragment() {

    lateinit var binding: FragmentNotificacionesGeneralBinding
    lateinit var notificacionesAdapter: NotificacionesAdapter
    val notificacionesList = mutableListOf<Notificacion>()

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            // Infla el diseño del fragmento y almacena el binding
            binding = FragmentNotificacionesGeneralBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // Inicializar adaptador vacío
            notificacionesAdapter = NotificacionesAdapter(emptyList()) { notificacion ->
                // Acciones al hacer clic en la notificación (puedes personalizar esto)
                // Por ejemplo, se podría abrir un detalle de la notificación.
                cambiarEstadoNotificacion(notificacion)
            }

            binding.listaNotificaciones.layoutManager = LinearLayoutManager(requireContext())
            binding.listaNotificaciones.adapter = notificacionesAdapter

            // Cargar las notificaciones desde la API
            cargarNotificaciones()

        }



        private fun cargarNotificaciones() {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://192.168.0.26:8080/dogidog/") // Dirección del servidor
                .addConverterFactory(GsonConverterFactory.create()) // Convierte JSON en objetos
                .build()

            val service = retrofit.create(ApiService::class.java)

            val usuario = obtenerUsuarioLocal()

            if (usuario == null) {
                Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return
            }

            usuario?.let {
                val call = service.obtenerNotificacionesDeUsuario(it.id)  // Ahora pasamos el ID del usuario
                call.enqueue(object : Callback<List<Notificacion>> {
                    override fun onResponse(call: Call<List<Notificacion>>, response: Response<List<Notificacion>>) {
                        if (response.isSuccessful) {
                            val listaNotificaciones = response.body() ?: emptyList()

                            // Actualizar los datos en lugar de crear un nuevo adaptador
                            notificacionesAdapter.actualizarLista(listaNotificaciones)
                        } else {
                            Toast.makeText(requireContext(), "Error al obtener notificaciones", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<List<Notificacion>>, t: Throwable) {
                        Log.e("API", "Error: ${t.message}")
                    }
                })
            }
        }

    private fun cambiarEstadoNotificacion(notificacion: Notificacion) {
        // Cambiar el estado de la notificación (por ejemplo, "leída")
        notificacion.leida = !notificacion.leida

        // Actualizamos la vista de la notificación clickeada
        val position = notificacionesAdapter.listaNotificaciones.indexOf(notificacion)
        if (position != -1) {
            notificacionesAdapter.notifyItemChanged(position)
        }
    }
        private fun obtenerUsuarioLocal(): Usuario? {
            val prefs = requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
            val id = prefs.getInt("usuario_id", -1)
            val usuario = prefs.getString("usuario", null)
            val email = prefs.getString("usuario_email", null)
            val password = prefs.getString("usuario_password", null)

            Log.d("SharedPreferences", "Recuperando usuario: ID=$id, Usuario=$usuario, Email=$email")

            return if (id != -1 && usuario != null && email != null && password != null) {
                Usuario(id, usuario, email, password)
            } else {
                Log.w("SharedPreferences", "No se encontró un usuario válido en SharedPreferences")
                null
            }
        }
}
