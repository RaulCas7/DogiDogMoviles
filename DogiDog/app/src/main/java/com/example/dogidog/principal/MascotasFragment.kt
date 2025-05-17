package com.example.dogidog.principal

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dogidog.R
import com.example.dogidog.adapters.MascotaAdapter
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Mascota
import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.databinding.FragmentMascotasListBinding
import com.example.dogidog.mascotas.AnadirMascotaFragment
import com.example.dogidog.mascotas.MascotaPrincipalFragment
import okhttp3.ResponseBody
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
    var modoEliminar = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el diseño del fragmento y almacena el binding
        binding = FragmentMascotasListBinding.inflate(inflater, container, false)
        // Configura el menú para que el fragmento lo maneje
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarToolbar()

        mascotaAdapter = MascotaAdapter(emptyList()) { mascota ->
            if (modoEliminar) {
                confirmarEliminacionMascota(mascota)
            } else {
                irAMascotaPrincipalFragment(mascota)
            }
        }

        binding.listaMascotas.layoutManager = LinearLayoutManager(requireContext())
        binding.listaMascotas.adapter = mascotaAdapter

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

        binding.botonBorrar.setOnClickListener {
            modoEliminar = !modoEliminar
            actualizarModoEliminar()
        }


    }

    private fun salirModoEliminar() {
        modoEliminar = false
        actualizarModoEliminar()
    }

    private fun eliminarMascota(mascota: Mascota) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/") // Dirección del servidor
            .addConverterFactory(GsonConverterFactory.create()) // Convierte JSON en objetos
            .build()

        val service = retrofit.create(ApiService::class.java)

        val call = service.eliminarMascota(mascota.id) // Llamada a la API DELETE

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Mascota ${mascota.nombre} eliminada", Toast.LENGTH_SHORT).show()

                    // Actualizamos la lista localmente
                    val listaActualizada = mascotaAdapter.listaMascotas.toMutableList()
                    listaActualizada.remove(mascota)
                    mascotaAdapter.actualizarLista(listaActualizada)
                } else {
                    Toast.makeText(requireContext(), "Error al eliminar mascota", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("API", "Error al eliminar: ${t.message}")
                Toast.makeText(requireContext(), "Error al eliminar mascota", Toast.LENGTH_SHORT).show()
            }
        })

        // Aquí deberías llamar a tu API para eliminar la mascota
        Toast.makeText(requireContext(), "Mascota ${mascota.nombre} eliminada", Toast.LENGTH_SHORT).show()

        // Actualizamos la lista localmente
        val listaActualizada = mascotaAdapter.listaMascotas.toMutableList()
        listaActualizada.remove(mascota)
        mascotaAdapter.actualizarLista(listaActualizada)
    }
    private fun confirmarEliminacionMascota(mascota: Mascota) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Mascota")
            .setMessage("¿Seguro que quieres eliminar a ${mascota.nombre}?")
            .setPositiveButton("Eliminar") { dialog, _ ->
                eliminarMascota(mascota) // Llamada para eliminar la mascota
                salirModoEliminar() // Salir del modo de eliminación
                dialog.dismiss() // Cerrar el diálogo
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss() // Solo cerrar el diálogo sin hacer nada
            }
            .show()
    }

    private fun actualizarModoEliminar() {
        if (modoEliminar) {
            binding.textoBorrar.text = "Cancelar eliminación"
            binding.botonBorrar.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            binding.fondoOscuro.visibility = View.VISIBLE
            configurarToolbarEliminar()
        } else {
            binding.textoBorrar.text = "Eliminar Mascota"
            binding.botonBorrar.setImageResource(android.R.drawable.ic_menu_delete)
            binding.fondoOscuro.visibility = View.GONE
            configurarToolbar()
        }
       // mascotaAdapter.modoEliminar = modoEliminar
        mascotaAdapter.notifyDataSetChanged()
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

                        // Cargar las fotos después de obtener las mascotas
                        listaMascotas.forEach { mascota ->
                            if (!mascota.foto.isNullOrEmpty()) {
                                cargarFotoMascota(mascota)  // Obtener la foto de cada mascota
                            }
                        }

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

    private fun cargarFotoMascota(mascota: Mascota) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/") // Dirección del servidor
            .addConverterFactory(GsonConverterFactory.create()) // Convierte JSON en objetos
            .build()

        val service = retrofit.create(ApiService::class.java)

        val call = service.getFotoMascota(mascota.id)  // Llamar a la API para obtener la foto de la mascota
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val inputStream = response.body()?.byteStream()

                    // Asegúrate de que el inputStream no sea nulo
                    if (inputStream != null) {
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        mascota.fotoBitmap = bitmap  // Guardar la foto como un bitmap en la mascota
                        mascotaAdapter.notifyDataSetChanged()  // Notificar al adaptador que los datos han cambiado
                    }
                } else {
                    Log.e("CargarFoto", "Error al obtener la foto")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("CargarFoto", "Error en la conexión: ${t.message}")
            }
        })
    }


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
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar ?: return

        // Limpiar configuración previa
        actionBar.displayOptions = 0
        actionBar.customView?.let {
            (it.parent as? ViewGroup)?.removeView(it)
        }
        actionBar.setDisplayShowTitleEnabled(false) // Para evitar mostrar título normal junto al customView

        // Crear nuevo TextView personalizado
        val titleTextView = TextView(requireContext()).apply {
            text = "Mascotas"
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            val actionBarHeight = resources.getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_bar_default_height_material)
            val textSize = (actionBarHeight * 0.5f).toFloat()
            this.textSize = textSize / resources.displayMetrics.density
        }

        actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        actionBar.customView = titleTextView

        actionBar.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.primario)))
    }



    private fun configurarToolbarEliminar() {
        // Acceder al ActionBar de la Activity que contiene este Fragment
        (activity as AppCompatActivity).supportActionBar?.apply {
            // Crear el TextView personalizado
            val titleTextView = TextView(requireContext()).apply {
                text = "Eliminar mascotas"
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
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.rojo)))

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
        val menuItemDoc = menu.findItem(R.id.action_nuevo_documento)
        menuItem.isVisible = false
        menuItemOptions.isVisible = true
        menuItemDoc.isVisible = false
        setHasOptionsMenu(true) // Permitir que el fragmento maneje los ítems del menú
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