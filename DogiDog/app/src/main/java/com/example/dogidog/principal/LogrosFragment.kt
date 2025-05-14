package com.example.dogidog.principal

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogidog.R
import com.example.dogidog.adapters.LogrosAdapter
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Logro
import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.dataModels.UsuariosLogro
import com.example.dogidog.databinding.FragmentLogrosBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LogrosFragment : Fragment() {

    lateinit var binding: FragmentLogrosBinding
    lateinit var logrosAdapter: LogrosAdapter
    val logros = mutableListOf<Logro>()
    val logrosDesbloqueados = mutableListOf<UsuariosLogro>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflamos el layout y vinculamos el binding
        binding = FragmentLogrosBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarToolbar()
        // Inicializar el adaptador antes de usarlo
        logrosAdapter = LogrosAdapter()

        // Configurar RecyclerView
        binding.listaLogros.layoutManager = LinearLayoutManager(requireContext())
        binding.listaLogros.adapter = logrosAdapter

        // Llamar a los métodos para obtener logros
        obtenerLogrosDisponibles()
        obtenerLogrosDesbloqueados()
    }

    private fun obtenerLogrosDisponibles() {
        // Crear cliente Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/") // Dirección del servidor
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        service.obtenerLogros().enqueue(object : Callback<List<Logro>> {
            override fun onResponse(call: Call<List<Logro>>, response: Response<List<Logro>>) {
                if (response.isSuccessful) {
                    val logros = response.body() ?: emptyList()
                    this@LogrosFragment.logros.clear()  // Limpiar la lista antes de añadir los nuevos
                    this@LogrosFragment.logros.addAll(logros)
                    logrosAdapter.setLogros(logros)
                }
            }

            override fun onFailure(call: Call<List<Logro>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error al obtener logros", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun obtenerLogrosDesbloqueados() {
        val usuarioId = obtenerUsuarioLocal()?.id ?: return // Obtener el ID del usuario logueado

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/") // Dirección del servidor
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        service.buscarLogrosDeUsuario(usuarioId).enqueue(object : Callback<List<UsuariosLogro>> {
            override fun onResponse(call: Call<List<UsuariosLogro>>, response: Response<List<UsuariosLogro>>) {
                if (response.isSuccessful) {
                    val logrosDesbloqueados = response.body() ?: emptyList()

                    // Verifica si la respuesta contiene logros desbloqueados
                    if (logrosDesbloqueados.isNotEmpty()) {
                        // Actualiza la lista de logros desbloqueados en el adaptador
                        this@LogrosFragment.logrosDesbloqueados.clear()
                        this@LogrosFragment.logrosDesbloqueados.addAll(logrosDesbloqueados)
                        logrosAdapter.setLogrosDesbloqueados(this@LogrosFragment.logrosDesbloqueados)
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al obtener logros desbloqueados", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<UsuariosLogro>>, t: Throwable) {

            }
        })
    }

    private fun obtenerUsuarioLocal(): Usuario? {
        // Obtener usuario del almacenamiento local (SharedPreferences o base de datos local)
        val prefs = requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val id = prefs.getInt("usuario_id", -1)
        val usuario = prefs.getString("usuario", null)
        val email = prefs.getString("usuario_email", null)
        val password = prefs.getString("usuario_password", null)

        return if (id != -1 && usuario != null && email != null && password != null) {
            Usuario(id, usuario, email, password, 0,null,null)
        } else {
            null
        }
    }

    private fun configurarToolbar() {
        // Acceder al ActionBar de la Activity que contiene este Fragment
        (activity as AppCompatActivity).supportActionBar?.apply {
            // Crear el TextView personalizado
            val titleTextView = TextView(requireContext()).apply {
                text = "Logros"
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_options -> { // El ítem de configuración
                // Aquí navegas a tu fragmento de configuración
                (activity as AppCompatActivity).supportFragmentManager.beginTransaction().apply {
                    replace(R.id.containerView, ConfiguracionFragment()) // Asegúrate de tener el container correcto
                    addToBackStack(null) // Si quieres que el fragmento de configuración se agregue a la pila de retroceso
                    commit()
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_menu, menu) // Inflar el menú
        val menuItem = menu.findItem(R.id.action_delete)
        val menuItemOptions = menu.findItem(R.id.action_options)
        menuItem.isVisible = false
        menuItemOptions.isVisible = true
        setHasOptionsMenu(true) // Permitir que el fragmento maneje los ítems del menú
    }


}

