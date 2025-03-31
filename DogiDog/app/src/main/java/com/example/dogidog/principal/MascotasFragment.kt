package com.example.dogidog.principal

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.example.dogidog.R
import com.example.dogidog.adapters.MascotaAdapter
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Mascota
import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.databinding.FragmentMascotasListBinding
import com.example.dogidog.mascotas.AnadirMascotaFragment
import com.example.dogidog.mascotas.MascotaPrincipalFragment
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
    var botonesVisibles : Boolean = false

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

        configurarToolbar()
        // Inicializar adaptador vacío
        mascotaAdapter = MascotaAdapter(emptyList()) { mascota ->
            irAMascotaPrincipalFragment(mascota)
        }

        binding.listaMascotas.layoutManager = LinearLayoutManager(requireContext())
        binding.listaMascotas.adapter = mascotaAdapter

        // Carga la lista de mascotas desde la API
        cargarMascotas()

        binding.botonPrincipal.setOnClickListener {
            alternarBotones()
        }

        binding.fondoOscuro.setOnClickListener {
            if (botonesVisibles) {
                ocultarBotones()
                botonesVisibles = false
            }
        }
        binding.botonAniadir.setOnClickListener {
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.containerView, AnadirMascotaFragment()) // Reemplaza el fragmento actual
            fragmentTransaction.addToBackStack(null) // Permite volver atrás con el botón de retroceso
            fragmentTransaction.commit()
        }
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
    private fun alternarBotones() {
        if (botonesVisibles) {
            ocultarBotones()
        } else {
            mostrarBotones()
        }
        botonesVisibles = !botonesVisibles
    }

    private fun mostrarBotones() {
        // Mostrar el fondo oscuro con animación
        binding.fondoOscuro.apply {
            visibility = View.VISIBLE
            animate().alpha(1.0f).setDuration(300).start()
        }

        // Asegurar que los contenedores sean visibles antes de animarlos
        binding.contenedorAniadir.visibility = View.VISIBLE
        binding.contenedorBorrar.visibility = View.VISIBLE

        // Asegurar que los botones sean visibles antes de animarlos
        binding.botonAniadir.visibility = View.VISIBLE
        binding.botonBorrar.visibility = View.VISIBLE

        // Mostrar botones con animación (cambiar a translationX en vez de translationY)
        binding.botonAniadir.animate().translationX(-0f).alpha(1.0f).setDuration(300).start()
        binding.botonBorrar.animate().translationX(-0f).alpha(1.0f).setDuration(300).start()

        // Asegurar que los textos sean visibles
        binding.textoAniadir.visibility = View.VISIBLE
        binding.textoBorrar.visibility = View.VISIBLE

        // Hacer el botón principal completamente visible
        binding.botonPrincipal.animate().alpha(1.0f).setDuration(300).start()
    }

    private fun ocultarBotones() {
        // Ocultar fondo oscuro con animación
        binding.fondoOscuro.animate().alpha(0.0f).setDuration(300).withEndAction {
            binding.fondoOscuro.visibility = View.GONE
        }.start()

        // Ocultar botones con animación
        binding.botonAniadir.animate().translationX(0f).alpha(0.0f).setDuration(200).withEndAction {
            binding.botonAniadir.visibility = View.GONE
        }.start()

        binding.botonBorrar.animate().translationX(0f).alpha(0.0f).setDuration(200).withEndAction {
            binding.botonBorrar.visibility = View.GONE
        }.start()

        // Ocultar los textos
        binding.textoAniadir.visibility = View.INVISIBLE
        binding.textoBorrar.visibility = View.INVISIBLE

        // Ocultar los contenedores al final
        binding.contenedorAniadir.visibility = View.GONE
        binding.contenedorBorrar.visibility = View.GONE

        // Hacer el botón principal semi-transparente de nuevo
        binding.botonPrincipal.animate().alpha(0.5f).setDuration(300).start()
    }

    private fun configurarToolbar() {
        // Acceder al ActionBar de la Activity que contiene este Fragment
        (activity as AppCompatActivity).supportActionBar?.apply {
            // Crear el TextView personalizado
            val titleTextView = TextView(requireContext()).apply {
                text = "Mascotas"
                setTextColor(Color.WHITE) // Establecer el color blanco
                setTypeface(null, Typeface.BOLD) // Poner el texto en negrita

                // Obtener la altura de la ActionBar (Toolbar)
                val actionBarHeight = resources.getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_bar_default_height_material)

                // Ajustar el tamaño del texto proporcionalmente
                val textSize = (actionBarHeight * 0.5f).toFloat() // El tamaño del texto será el 50% de la altura
                this.textSize = textSize / resources.displayMetrics.density // Convertir a SP
            }

            // Establecer el título con el TextView personalizado
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            customView = titleTextView

            // Cambiar el fondo de la ActionBar (Toolbar)
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.primario)))
        }
    }
    override fun onResume() {
        super.onResume()
        cargarMascotas()
    }

    private fun irAMascotaPrincipalFragment(mascota: Mascota) {
        val fragment = MascotaPrincipalFragment().apply {
            arguments = Bundle().apply {
                putParcelable("mascota", mascota) // Pasar la mascota seleccionada
            }
        }

        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.containerView, fragment)
        fragmentTransaction.addToBackStack(null) // Para poder volver atrás
        fragmentTransaction.commit()
    }
}