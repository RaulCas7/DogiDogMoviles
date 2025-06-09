package com.example.dogidog.principal
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
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

        configurarToolbar()
        // Inicializar adaptador con una lista vacía
        notificacionesAdapter = NotificacionesAdapter(
            mutableListOf(),
            { enModoSeleccion -> manejarModoSeleccion(enModoSeleccion) }
        ) { notificacion ->
            cambiarEstadoNotificacion(notificacion)
        }

        binding.listaNotificaciones.layoutManager = LinearLayoutManager(requireContext())
        binding.listaNotificaciones.adapter = notificacionesAdapter

        // Inflar el menú de la Toolbar
        setHasOptionsMenu(true) // Asegura que el fragmento pueda tener su propio menú

        // Cargar las notificaciones desde la API
        cargarNotificaciones()
    }

    // Inflar el menú con los elementos definidos en el archivo de recursos
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_menu, menu)
        val menuItem = menu.findItem(R.id.action_delete)
        val menuItemOptions = menu.findItem(R.id.action_options)
        menuItem.isVisible = notificacionesAdapter.enModoSeleccion
        menuItemOptions.isVisible = notificacionesAdapter.enModoSeleccion
        menuItemOptions.isVisible = !notificacionesAdapter.enModoSeleccion
    }

    // Manejar la acción de los ítems del menú (como la papelera)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                eliminarSeleccionadas()
                true
            }
            R.id.action_options -> { // El ítem de configuración
                // Aquí navegas a tu fragmento de configuración
                (activity as AppCompatActivity).supportFragmentManager.beginTransaction().apply {
                    replace(R.id.containerView, ConfiguracionFragment()) // Asegúrate de tener el container correcto
                    addToBackStack(null) // Si quieres que el fragmento de configuración se agregue a la pila de retroceso
                    commit()

                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Manejar el cambio del modo de selección
    private fun manejarModoSeleccion(enModoSeleccion: Boolean) {
        notificacionesAdapter.enModoSeleccion = enModoSeleccion
        requireActivity().invalidateOptionsMenu()
    }

    // Cargar las notificaciones desde la API
    private fun cargarNotificaciones() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.170.200:8080/dogidog/") // Dirección del servidor
            .addConverterFactory(GsonConverterFactory.create()) // Convierte JSON en objetos
            .build()

        val service = retrofit.create(ApiService::class.java)
        val usuario = obtenerUsuarioLocal()

        if (usuario == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        usuario?.let {
            val call = service.obtenerNotificacionesDeUsuario(it.id)  // Pasamos el ID del usuario
            call.enqueue(object : Callback<MutableList<Notificacion>> {
                override fun onResponse(call: Call<MutableList<Notificacion>>, response: Response<MutableList<Notificacion>>) {
                    if (response.isSuccessful) {
                        val listaNotificaciones = response.body() ?: mutableListOf()
                        notificacionesAdapter.actualizarLista(listaNotificaciones)
                        mostrarEstadoVacio(listaNotificaciones.isEmpty()) // ⬅️ Añadido
                    } else {
                        Toast.makeText(requireContext(), "Error al obtener notificaciones", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MutableList<Notificacion>>, t: Throwable) {
                    Log.e("API", "Error: ${t.message}")
                }
            })
        }
    }

    // Cambiar el estado de la notificación (por ejemplo, marcar como leída)
    private fun cambiarEstadoNotificacion(notificacion: Notificacion) {
        notificacion.leida = !notificacion.leida

        actualizarNotificacionEnApi(notificacion.id, notificacion) { success ->
            if (success) {
                Toast.makeText(context, "Notificación actualizada correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Error al actualizar la notificación", Toast.LENGTH_SHORT).show()
            }
        }
        val position = notificacionesAdapter.listaNotificaciones.indexOf(notificacion)
        if (position != -1) {
            notificacionesAdapter.notifyItemChanged(position)
        }
    }

    fun actualizarNotificacionEnApi(id: Int, notificacion: Notificacion, onResult: (Boolean) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.170.200:8080/dogidog/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        service.actualizarNotificacion(id, notificacion).enqueue(object : Callback<Notificacion> {
            override fun onResponse(call: Call<Notificacion>, response: Response<Notificacion>) {
                onResult(response.isSuccessful)
            }

            override fun onFailure(call: Call<Notificacion>, t: Throwable) {
                onResult(false)
            }
        })
    }

    // Obtener el usuario local desde SharedPreferences
    private fun obtenerUsuarioLocal(): Usuario? {
        val prefs = requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val id = prefs.getInt("usuario_id", -1)
        val usuario = prefs.getString("usuario", null)
        val email = prefs.getString("usuario_email", null)
        val password = prefs.getString("usuario_password", null)
        val contadorPreguntas = prefs.getInt("usuario_preguntas", 0)
        val latitud = prefs.getFloat("usuario_latitud", Float.MIN_VALUE)
        val longitud = prefs.getFloat("usuario_longitud", Float.MIN_VALUE)
        val valoracion = prefs.getInt("usuario_valoracion", 0)
        val foto = prefs.getInt("usuario_foto", 0) // 🆕 Añadimos la foto del usuario

        return if (id != -1 && usuario != null && email != null && password != null) {
            val latitudDouble = if (latitud != Float.MIN_VALUE) latitud.toDouble() else null
            val longitudDouble = if (longitud != Float.MIN_VALUE) longitud.toDouble() else null

            Usuario(
                id = id,
                usuario = usuario,
                email = email,
                password = password,
                contadorPreguntas = contadorPreguntas,
                latitud = latitudDouble,
                longitud = longitudDouble,
                valoracion = valoracion,
                foto = foto // 🆕 Añadimos la foto al objeto Usuario
            )
        } else {
            null
        }
    }

    // Eliminar las notificaciones seleccionadas
    private fun eliminarSeleccionadas() {
        notificacionesAdapter.eliminarSeleccionadas()
        manejarModoSeleccion(false)
    }

    private fun configurarToolbar() {
        // Acceder al ActionBar de la Activity que contiene este Fragment
        (activity as AppCompatActivity).supportActionBar?.apply {
            // Crear el TextView personalizado
            val titleTextView = TextView(requireContext()).apply {
                text = "Notificaciones"
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

    // Método para actualizar las notificaciones en el adaptador
    fun actualizarNotificaciones(notificaciones: MutableList<Notificacion>) {
        if (::notificacionesAdapter.isInitialized) {
            notificacionesAdapter.actualizarLista(notificaciones)
        } else {
            Log.e("NotificacionesFragment", "El adaptador no ha sido inicializado correctamente.")
        }

        // Verificar si hay notificaciones no leídas
        val notificacionesNoLeidas = notificaciones.filter { !it.leida }

        // Llamar al método de la actividad para actualizar el badge
        (activity as? PantallaPrincipalActivity)?.actualizarBadgeNotificaciones(
            notificacionesNoLeidas.isNotEmpty(),
            notificacionesNoLeidas.size // Pasamos el número de notificaciones no leídas
        )
    }
    private fun mostrarEstadoVacio(listaVacia: Boolean) {
        binding.textoSinNotificaciones.visibility = if (listaVacia) View.VISIBLE else View.GONE
    }
}