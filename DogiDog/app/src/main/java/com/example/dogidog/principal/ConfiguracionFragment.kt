package com.example.dogidog.principal

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dogidog.R
import com.example.dogidog.adapters.LogrosAdapter
import com.example.dogidog.adapters.LogrosAdapterImage
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Logro
import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.dataModels.UsuariosLogro
import com.example.dogidog.databinding.FragmentConfiguracionBinding
import com.example.dogidog.inicioSesion.InicioSesionActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.MessageDigest

class ConfiguracionFragment : Fragment() {

    private lateinit var binding: FragmentConfiguracionBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val PICK_IMAGEN = 100
    lateinit var logrosAdapter: LogrosAdapterImage
    val logros = mutableListOf<Logro>()
    val logrosDesbloqueados = mutableListOf<UsuariosLogro>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConfiguracionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        configurarToolbar()
        // Inicializar el adaptador antes de usarlo
        logrosAdapter = LogrosAdapterImage()

        binding.tvNombreUsuario.text = obtenerUsuarioLocal()!!.usuario

        sharedPreferences.getString("imagenBot", null)?.let {
            val uri = Uri.parse(it)
            binding.imgDogiBot.setImageURI(uri)
            binding.imgFotoUsuario.setImageURI(uri)
        }

        // Cargar switches
        binding.switchNotificaciones.isChecked = sharedPreferences.getBoolean("notificaciones", true)
        binding.switchModoNoche.isChecked = sharedPreferences.getBoolean("modoNoche", false)
        binding.switchSilenciarDogiBot.isChecked = sharedPreferences.getBoolean("silenciarBot", false)


        binding.btnEditarNombre.setOnClickListener {
            mostrarDialogoNombre()
        }

        binding.btnEditarDogiBot.setOnClickListener {
            cargarYMostrarDialogoLogros()
        }

        binding.imgFotoUsuario.setOnClickListener {
            cargarYMostrarDialogoLogros()
        }

        binding.switchModoNoche.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("modoNoche", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("notificaciones", isChecked).apply()
        }

        binding.switchSilenciarDogiBot.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("silenciarBot", isChecked).apply()
        }

        binding.btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }

        binding.btnEliminarCuenta.setOnClickListener {
            mostrarDialogoEliminarCuenta()
        }

        // Referencias a las vistas
        val layoutCambiarContrasena = binding.layoutCambiarContrasena
        val layoutDetallesContrasena = binding.layoutDetallesContrasena
        val btnExpandirContrasena = binding.btnExpandirContrasena

// Mostrar y ocultar la sección de cambiar contraseña
        layoutCambiarContrasena.setOnClickListener {
            if (layoutDetallesContrasena.visibility == View.GONE) {
                // Mostrar detalles de cambiar contraseña
                layoutDetallesContrasena.visibility = View.VISIBLE
                btnExpandirContrasena.setImageResource(R.drawable.baseline_expand_less_24) // Icono de menos
            } else {
                // Ocultar detalles de cambiar contraseña
                layoutDetallesContrasena.visibility = View.GONE
                btnExpandirContrasena.setImageResource(R.drawable.baseline_expand_more_24) // Icono de más
            }
        }

// Lógica de guardar contraseña (se muestra en el XML anterior)
        binding.btnGuardarContrasena.setOnClickListener {
            val contrasenaActual = binding.etContrasenaActual.text.toString()
            val nuevaContrasena = binding.etNuevaContrasena.text.toString()
            val confirmarContrasena = binding.etConfirmarContrasena.text.toString()

            // Obtener la contraseña actual almacenada (en MD5) desde SharedPreferences
            val storedPasswordMD5 = sharedPreferences.getString("usuario_password", null)

            if (storedPasswordMD5 != null && verificarContraseñaActual(contrasenaActual, storedPasswordMD5)) {
                if (nuevaContrasena == confirmarContrasena) {
                    // Convertir la nueva contraseña a MD5
                    val nuevaContrasenaMD5 = generarMD5(nuevaContrasena)

                    // Guardar la nueva contraseña en SharedPreferences (en MD5)
                    sharedPreferences.edit().putString("usuario_password", nuevaContrasenaMD5).apply()

                    // Actualizar los demás campos del usuario, incluidos los cambios realizados
                    val usuarioActualizado = obtenerUsuarioLocal()  // Obtener el usuario local actualizado
                    usuarioActualizado?.let {
                        // Si se cambia la contraseña, la actualizamos en el objeto Usuario
                        it.password = nuevaContrasenaMD5

                        // Llamamos a la API para actualizar el usuario en el servidor
                        actualizarUsuarioAPI(it)
                    }

                    Toast.makeText(requireContext(), "Contraseña cambiada exitosamente", Toast.LENGTH_SHORT).show()

                    // Replegar la sección
                    layoutDetallesContrasena.visibility = View.GONE
                    btnExpandirContrasena.setImageResource(R.drawable.baseline_expand_more_24) // Cambiar icono
                } else {
                    Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun mostrarDialogoNombre() {
        val editText = EditText(requireContext()).apply {
            setText(binding.tvNombreUsuario.text)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Cambiar nombre")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = editText.text.toString()

                // Actualizamos la UI localmente
                binding.tvNombreUsuario.text = nuevoNombre

                // Actualizamos el usuario local (SharedPreferences)
                val usuarioEdit = obtenerUsuarioLocal()
                usuarioEdit?.let {
                    it.usuario = nuevoNombre // Actualizamos el nombre
                    // Luego actualizamos los datos en SharedPreferences
                    guardarUsuarioLocal(it)

                    // Finalmente, hacemos la llamada a la API para guardar los cambios en el servidor
                    actualizarUsuarioAPI(it)  // Llamada a la API para actualizar
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEliminarCuenta() {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar cuenta")
            .setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                val usuario = obtenerUsuarioLocal()
                if (usuario != null) {
                    eliminarUsuarioAPI(usuario.id)
                } else {
                    Toast.makeText(requireContext(), "No se pudo obtener el usuario", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cerrarSesion() {
        // Limpiar SharedPreferences
        val prefs = requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Mostrar mensaje
        Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()

        // Ir a la pantalla de inicio de sesión y limpiar el stack de actividades
        val intent = Intent(requireContext(), InicioSesionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data ?: return
            if (requestCode == PICK_IMAGEN) {
                // Guardamos la misma imagen para usuario y DogiBot
                binding.imgFotoUsuario.setImageURI(uri)
                binding.imgDogiBot.setImageURI(uri)

                // Actualizamos los SharedPreferences con la nueva foto
                sharedPreferences.edit().putString("imagenBot", uri.toString()).apply()

                // Actualizamos el usuario local (SharedPreferences)
                val usuario = obtenerUsuarioLocal()
                usuario?.let {
                    it.foto = uri.toString() // Actualizamos la foto
                    guardarUsuarioLocal(it)  // Guardamos el usuario actualizado

                    // Finalmente, hacemos la llamada a la API para actualizar la foto del usuario en el servidor
                    actualizarUsuarioAPI(it)  // Llamada a la API para actualizar
                }
            }
        }
    }

    private fun configurarToolbar() {
        // Si usas una Toolbar en lugar de ActionBar, accedes a ella directamente
        (activity as AppCompatActivity).apply {

            supportActionBar?.apply {
                // Título personalizado de la Toolbar
                val titleTextView = TextView(requireContext()).apply {
                    text = "Configuración"
                    setTextColor(Color.WHITE)
                    setTypeface(null, Typeface.BOLD)

                    // Obtener la altura de la ActionBar (Toolbar)
                    val actionBarHeight = resources.getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_bar_default_height_material)
                    val textSize = (actionBarHeight * 0.5f).toFloat()
                    this.textSize = textSize / resources.displayMetrics.density // Convertir a SP
                }

                displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
                customView = titleTextView

                setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.primario)))
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
            }

            setHasOptionsMenu(true) // Habilitar el menú
        }
    }

    // Manejar el botón de retroceso
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed() // Retroceder
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? PantallaPrincipalActivity)?.binding?.navegacion?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        (activity as? PantallaPrincipalActivity)?.binding?.navegacion?.visibility = View.VISIBLE
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
        val valoracion = prefs.getInt("usuario_valoracion", 0)  // Nuevo campo de valoración
        val foto = prefs.getString("usuario_foto", null) // Aquí recogemos la foto del usuario

        Log.d("SharedPreferences", "Recuperando usuario: ID=$id, Usuario=$usuario, Email=$email, Preguntas=$contadorPreguntas, Valoración=$valoracion, Foto=$foto")

        return if (id != -1 && usuario != null && email != null && password != null) {
            val latitudDouble = if (latitud != Float.MIN_VALUE) latitud.toDouble() else null
            val longitudDouble = if (longitud != Float.MIN_VALUE) longitud.toDouble() else null

            Usuario(
                id = id,
                usuario = usuario,
                email = email,
                password = password,  // Asegúrate que la contraseña esté en MD5
                contadorPreguntas = contadorPreguntas,
                latitud = latitudDouble,
                longitud = longitudDouble,
                valoracion = valoracion, // Agregar la valoración al objeto Usuario
                foto = foto // Agregar la foto al objeto Usuario
            )
        } else {
            Log.w("SharedPreferences", "No se encontró un usuario válido en SharedPreferences")
            null
        }
    }
    private fun guardarUsuarioLocal(usuario: Usuario) {
        val prefs = requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        with(prefs.edit()) {
            // Guardamos los campos no nulos
            putInt("usuario_id", usuario.id)
            putString("usuario", usuario.usuario)
            putString("usuario_email", usuario.email)
            putString("usuario_password", usuario.password)
            putInt("usuario_preguntas", usuario.contadorPreguntas)

            // Guardamos latitud y longitud solo si no son nulos
            usuario.latitud?.let {
                putFloat("usuario_latitud", it.toFloat())
            }
            usuario.longitud?.let {
                putFloat("usuario_longitud", it.toFloat())
            }

            // Guardamos el campo de valoración solo si no es nulo
            usuario.valoracion?.let {
                putInt("usuario_valoracion", it)
            }

            // Guardamos foto solo si no es nulo
            usuario.foto?.let {
                putString("usuario_foto", it)
            }

            apply()  // Guarda los cambios de manera asíncrona
        }
    }
    private fun actualizarUI(usuario: Usuario) {

        // Actualizar los valores en los campos de la UI
        binding.tvNombreUsuario.setText(usuario.usuario)

        // Si tienes una imagen de perfil que se puede actualizar
        usuario.fotoBitmap?.let {
            binding.imgFotoUsuario.setImageBitmap(it)
        }

        // Mostrar un mensaje indicando que la actualización fue exitosa
        Toast.makeText(requireContext(), "Datos del usuario actualizados", Toast.LENGTH_SHORT).show()
    }

    private fun actualizarUsuarioAPI(usuarioEditar: Usuario) {
        // Configurar Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/") // Cambia esta URL por la de tu servidor
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        // Comprobamos si la contraseña fue modificada antes de aplicarle MD5
        if (usuarioEditar.password != obtenerUsuarioLocal()?.password) {
            // Si la contraseña fue cambiada, la convertimos a MD5
            usuarioEditar.password = generarMD5(usuarioEditar.password)
        }

        // Hacemos la llamada PUT
        val call = service.actualizarUsuario(usuarioEditar.id, usuarioEditar)
        call.enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful) {
                    val usuarioActualizado = response.body()
                    if (usuarioActualizado != null) {
                        // Actualizamos el usuario local
                        guardarUsuarioLocal(usuarioActualizado)
                        actualizarUI(usuarioActualizado) // Actualizamos la UI con los datos del usuario
                        Toast.makeText(requireContext(), "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar el usuario", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                Toast.makeText(requireContext(), "Fallo en la conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun eliminarUsuarioAPI(idUsuario: Int) {
        // Configurar Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/") // Usa la IP de tu servidor local
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        // Hacemos la llamada DELETE
        val call = service.eliminarUsuario(idUsuario)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Cuenta eliminada correctamente", Toast.LENGTH_SHORT).show()

                    // Aquí puedes limpiar SharedPreferences y redirigir al login
                    val prefs = requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()

                    val intent = Intent(requireContext(), InicioSesionActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Fallo en la conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun generarMD5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val hashBytes = md.digest(input.toByteArray())
        val sb = StringBuilder()
        for (byte in hashBytes) {
            sb.append(String.format("%02x", byte))
        }
        return sb.toString()
    }

    // Método para comprobar si la contraseña proporcionada por el usuario es la misma que la almacenada en MD5
    fun verificarContraseñaActual(contraseñaIngresada: String, contraseñaGuardadaMD5: String): Boolean {
        val contraseñaIngresadaMD5 = generarMD5(contraseñaIngresada)
        return contraseñaIngresadaMD5 == contraseñaGuardadaMD5
    }

    private fun mostrarDialogoConLogros() {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_ver_logro, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerLogros)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Selecciona un logro")
            .setView(view)
            .setPositiveButton("Cerrar", null)
            .create()

        logrosAdapter = LogrosAdapterImage().apply {
            setLogros(logros)
            setLogrosDesbloqueados(logrosDesbloqueados)
            setOnEmblemaSeleccionadoListener(object : LogrosAdapterImage.OnEmblemaSeleccionadoListener {
                override fun onEmblemaSeleccionado(bitmap: Bitmap) {
                    binding.imgFotoUsuario.setImageBitmap(bitmap)
                    dialog.dismiss()
                }
            })
        }

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.adapter = logrosAdapter

        dialog.show()
    }



    private fun obtenerLogrosDisponibles(onComplete: () -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        service.obtenerLogros().enqueue(object : Callback<List<Logro>> {
            override fun onResponse(call: Call<List<Logro>>, response: Response<List<Logro>>) {
                if (response.isSuccessful) {
                    val nuevosLogros = response.body() ?: emptyList()
                    logros.clear()
                    logros.addAll(nuevosLogros)

                    nuevosLogros.forEach { logro ->
                        obtenerEmblemaLogro(logro.id)
                    }

                    logrosAdapter.setLogros(logros)
                    onComplete()
                } else {
                    Toast.makeText(requireContext(), "Error al obtener logros", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Logro>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de conexión al obtener logros", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun obtenerLogrosDesbloqueados(onComplete: () -> Unit) {
        val usuarioId = obtenerUsuarioLocal()?.id ?: return

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        service.buscarLogrosDeUsuario(usuarioId).enqueue(object : Callback<List<UsuariosLogro>> {
            override fun onResponse(call: Call<List<UsuariosLogro>>, response: Response<List<UsuariosLogro>>) {
                if (response.isSuccessful) {
                    val desbloqueados = response.body() ?: emptyList()
                    logrosDesbloqueados.clear()
                    logrosDesbloqueados.addAll(desbloqueados)

                    desbloqueados.forEach {
                        obtenerEmblemaLogro(it.logro.id)
                    }

                    logrosAdapter.setLogrosDesbloqueados(logrosDesbloqueados)
                    onComplete()
                } else {
                    Toast.makeText(requireContext(), "Error al obtener logros desbloqueados", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<UsuariosLogro>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de conexión al obtener logros desbloqueados", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun obtenerEmblemaLogro(id: Int) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        service.obtenerEmblemaLogro(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val inputStream = response.body()?.byteStream()
                    inputStream?.let {
                        val bitmap = BitmapFactory.decodeStream(it)
                        val logro = logros.find { it.id == id }
                        logro?.emblemaBitmap = bitmap
                        logrosAdapter.notifyItemChanged(logros.indexOf(logro))
                    }
                } else {
                    Log.e("CargarEmblema", "Error al obtener el emblema")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("CargarEmblema", "Error en la conexión: ${t.message}")
            }
        })
    }


    private fun cargarYMostrarDialogoLogros() {
        obtenerLogrosDisponibles {
            obtenerLogrosDesbloqueados {
                mostrarDialogoConLogros()
            }
        }
    }

}

