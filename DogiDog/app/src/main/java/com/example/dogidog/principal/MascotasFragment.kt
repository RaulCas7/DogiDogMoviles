package com.example.dogidog.principal

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.example.dogidog.R
import com.example.dogidog.adapters.MascotaAdapter
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Mascota
import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.databinding.FragmentMascotasListBinding
import com.example.dogidog.placeholder.PlaceholderContent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * A fragment representing a list of Items.
 */
class MascotasFragment : Fragment() {

    lateinit var binding: FragmentMascotasListBinding
    lateinit var mascotaAdapter: MascotaAdapter  // Adaptador para la lista de mascotas

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el diseño del fragmento y almacena el binding
        binding = FragmentMascotasListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Inicializar adaptador vacío
        mascotaAdapter = MascotaAdapter(emptyList()) { mascota ->
            val action = MascotasFragmentDirections
                .actionMascotasFragmentToDetalleMascotaFragment(mascota)
            findNavController().navigate(action)
        }

        binding.listaMascotas.layoutManager = LinearLayoutManager(requireContext())
        binding.listaMascotas.adapter = mascotaAdapter

        // Carga la lista de mascotas desde la API
        cargarMascotas()
    }

    private fun cargarMascotas() {
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
            val call = service.obtenerMascotas(it.id)  // Ahora pasamos el ID del usuario
            call.enqueue(object : Callback<List<Mascota>> {
                override fun onResponse(call: Call<List<Mascota>>, response: Response<List<Mascota>>) {
                    if (response.isSuccessful) {
                        val listaMascotas = response.body() ?: emptyList()

                        // Actualizar los datos en lugar de crear un nuevo adaptador
                        mascotaAdapter.actualizarLista(listaMascotas)
                    } else {
                        Toast.makeText(requireContext(), "Error al obtener mascotas", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Mascota>>, t: Throwable) {
                    Log.e("API", "Error: ${t.message}")
                }
            })
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