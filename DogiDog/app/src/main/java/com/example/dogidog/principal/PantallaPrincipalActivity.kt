package com.example.dogidog.principal

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.dogidog.R
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Notificacion
import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.databinding.ActivityPantallaPrincipalBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class PantallaPrincipalActivity : AppCompatActivity() {
    lateinit var binding: ActivityPantallaPrincipalBinding
    lateinit var mapsFragment: MapsFragment
    lateinit var mascotasFragment: MascotasFragment
    lateinit var notificacionesFragment: NotificacionesFragment
    lateinit var logrosFragment: LogrosFragment
    lateinit var dogibotFragment: DogibotFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPantallaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Define el índice del elemento que deseas seleccionar por defecto (en este ejemplo, el segundo elemento)
        val defaultItemIndex = 2
        // Selecciona el elemento por defecto
        binding.navegacion.menu.getItem(defaultItemIndex).isChecked = true
        initComponents()
        seleccionarToolbar()
        // Cargar las notificaciones
        cargarNotificaciones()
        
    }

    private fun initComponents() {
        mapsFragment = MapsFragment()
        mascotasFragment = MascotasFragment()
        notificacionesFragment = NotificacionesFragment()
        logrosFragment = LogrosFragment()
        dogibotFragment = DogibotFragment()
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primario)))
        // Cambiar el título de la ActionBar
        supportActionBar?.title = "Mi Título"

        // Cambiar el estilo del título a blanco y en negrita
        val titleTextView = TextView(this)
        titleTextView.text = "Bienvenido..."
        titleTextView.setTextColor(Color.WHITE)  // Establecer el color a blanco
        titleTextView.setTypeface(null, Typeface.BOLD)  // Poner el texto en negrita
        // Ajustar el tamaño de la fuente proporcionalmente a la altura de la ActionBar
        val actionBarHeight = resources.getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_bar_default_height_material) // Altura de la ActionBar
        val textSize = (actionBarHeight * 0.5f).toFloat() // Ajustar el tamaño del texto como porcentaje de la altura

        // Establecer el tamaño del texto
        titleTextView.textSize = textSize / resources.displayMetrics.density // Convertir a SP

        // Aplicar el TextView personalizado a la ActionBar
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.customView = titleTextView
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.containerView, fragment)
            commit()
        }
    }

    private fun seleccionarToolbar() {
        binding.navegacion.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_mascotas -> {
                    setCurrentFragment(mascotasFragment)
                    true
                }

                R.id.navigation_mapa -> {

                    setCurrentFragment(mapsFragment)
                    true
                }

                R.id.navigation_notificaciones -> {

                    setCurrentFragment(notificacionesFragment)
                    true
                }

                R.id.navigation_logros -> {

                    setCurrentFragment(logrosFragment)
                    true
                }

                R.id.navigation_dogibot -> {
                    setCurrentFragment(dogibotFragment)
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    // Cargar las notificaciones desde la API
    private fun cargarNotificaciones() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/dogidog/") // Dirección del servidor
            .addConverterFactory(GsonConverterFactory.create()) // Convierte JSON en objetos
            .build()

        val service = retrofit.create(ApiService::class.java)
        val usuario = obtenerUsuarioLocal()

        if (usuario == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        usuario?.let {
            val call = service.obtenerNotificacionesDeUsuario(it.id)  // Pasamos el ID del usuario
            call.enqueue(object : Callback<MutableList<Notificacion>> {
                override fun onResponse(call: Call<MutableList<Notificacion>>, response: Response<MutableList<Notificacion>>) {
                    if (response.isSuccessful) {
                        val listaNotificaciones = response.body() ?: mutableListOf()
                        // Actualizar el fragmento con las notificaciones
                        notificacionesFragment.actualizarNotificaciones(listaNotificaciones)

                        val notificacionesNoLeidas = listaNotificaciones.filter { !it.leida }
                        val cantidadNotificacionesNoLeidas = notificacionesNoLeidas.size

                        actualizarBadgeNotificaciones(notificacionesNoLeidas.isNotEmpty(), cantidadNotificacionesNoLeidas)
                    } else {
                        Toast.makeText(this@PantallaPrincipalActivity, "Error al obtener notificaciones", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MutableList<Notificacion>>, t: Throwable) {
                    Log.e("API", "Error: ${t.message}")
                }
            })
        }
    }

    // Método para actualizar el badge de notificaciones
    fun actualizarBadgeNotificaciones(tieneNotificacionesNoLeidas: Boolean, cantidadNotificacionesNoLeidas: Int) {
        // Accede al BottomNavigationView a través de binding
        val bottomNavView = binding.navegacion // Aquí usamos el binding para acceder al BottomNavigationView

        // Obtener el badge del ítem correspondiente
        val badge = bottomNavView.getOrCreateBadge(R.id.navigation_notificaciones)

        if (tieneNotificacionesNoLeidas) {
            // Mostrar el badge (bolita roja)
            badge.isVisible = true
            badge.backgroundColor = Color.RED
            badge.badgeTextColor = Color.WHITE
            
        } else {
            // Eliminar el badge si no hay notificaciones no leídas
            badge.isVisible = false
        }
    }

    // Obtener el usuario local desde SharedPreferences

    private fun obtenerUsuarioLocal(): Usuario? {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val id = prefs.getInt("usuario_id", -1)
        val usuario = prefs.getString("usuario", null)
        val email = prefs.getString("usuario_email", null)
        val password = prefs.getString("usuario_password", null)
        val latitud = prefs.getFloat("usuario_latitud", Float.MIN_VALUE)
        val longitud = prefs.getFloat("usuario_longitud", Float.MIN_VALUE)

        Log.d("SharedPreferences", "Recuperando usuario: ID=$id, Usuario=$usuario, Email=$email, Lat=$latitud, Lng=$longitud")

        return if (id != -1 && usuario != null && email != null && password != null) {
            val latitudDouble = if (latitud != Float.MIN_VALUE) latitud.toDouble() else null
            val longitudDouble = if (longitud != Float.MIN_VALUE) longitud.toDouble() else null
            Usuario(id, usuario, email, password, 0, latitudDouble, longitudDouble,0,0)
        } else {
            Log.w("SharedPreferences", "No se encontró un usuario válido en SharedPreferences")
            null
        }
    }
}