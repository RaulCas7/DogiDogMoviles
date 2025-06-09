package com.example.dogidog.principal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.speech.tts.TextToSpeech
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogidog.R
import com.example.dogidog.adapters.DogibotAdapter
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Mensaje
import com.example.dogidog.dataModels.Pregunta
import com.example.dogidog.dataModels.Tarea
import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.databinding.FragmentDogibotBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class DogibotFragment : Fragment(), TextToSpeech.OnInitListener {

    private lateinit var binding: FragmentDogibotBinding
    private lateinit var adapter: DogibotAdapter
    private val mensajes = mutableListOf<Mensaje>()
    private lateinit var usuarioNombre : String
    private lateinit var tts: TextToSpeech
    private var numeroSecreto: Int? = null // Número secreto elegido por el bot
    private var jugando: Boolean = false // Estado del juego
    private var enviandoMensaje = false
    private var bienvenidaEnviada = false  // Variable para controlar si el mensaje de bienvenida ya ha sido enviado

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDogibotBinding.inflate(inflater, container, false)
        // Mensaje inicial del bot
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        usuarioNombre = obtenerUsuarioLocal()!!.usuario
        configurarToolbar()
        // Inicializar TTS
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Configurar idioma
                val result = tts.setLanguage(Locale("es", "ES"))

                // Verificar si el idioma está disponible
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(context, "Idioma no soportado", Toast.LENGTH_SHORT).show()
                }

                // Configurar la voz
                configurarVozAvanzada()
            }
        }

        // Configurar RecyclerView
        adapter = DogibotAdapter(mensajes, usuarioNombre)
        binding.recyclerMensajes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMensajes.adapter = adapter

        // Verificar si el mensaje de bienvenida ya fue enviado
        if (!bienvenidaEnviada) {
            mostrarMensajeBienvenida()
        }

        // Enviar mensaje con botón
        binding.enviarMensaje.setOnClickListener {
            enviarMensaje()
        }

        // Configurar RecyclerView para que los mensajes más nuevos aparezcan abajo
        binding.recyclerMensajes.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true  // Para que los mensajes más nuevos aparezcan abajo
        }

        // Enviar con Enter
        binding.edtEscribir.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                enviarMensaje()
                true
            } else {
                false
            }
        }
    }

    // Método para enviar el mensaje
    private fun enviarMensaje() {
        if (enviandoMensaje) return
        val mensajeTexto = binding.edtEscribir.text.toString().trim()
        if (mensajeTexto.isNotEmpty()) {
            enviandoMensaje = true

            val usuario = obtenerUsuarioLocal()
            val idLogro = usuario?.foto

            if (idLogro != null) {
                obtenerEmblemaLogro(idLogro) { bitmap ->
                    val mensajeUsuario = Mensaje(mensajeTexto, true, bitmap)
                    adapter.agregarMensaje(mensajeUsuario)
                    binding.recyclerMensajes.scrollToPosition(mensajes.size - 1)

                    binding.edtEscribir.text.clear()

                    // Aquí haces la llamada a la API después de agregar el mensaje
                    buscarPreguntaEnApi(mensajeTexto)
                }
            } else {
                adapter.agregarMensaje(Mensaje(mensajeTexto, true, null))
                binding.recyclerMensajes.scrollToPosition(mensajes.size - 1)
                binding.edtEscribir.text.clear()

                buscarPreguntaEnApi(mensajeTexto)
            }
        }
    }



    private fun mostrarMensajeBienvenida() {
        if (!bienvenidaEnviada) {
            val usuario = obtenerUsuarioLocal()
            val idLogroStr = usuario?.foto
            val mensajeTexto = "¡Hola! Soy Dogibot, ¿en qué puedo ayudarte?"

            if (idLogroStr != null) {
                val idLogro = idLogroStr
                if (idLogro != null) {
                    obtenerEmblemaLogro(idLogro) { bitmap ->
                        val mensajeBot = Mensaje(mensajeTexto, false, bitmap)
                        adapter.agregarMensaje(mensajeBot)
                        binding.recyclerMensajes.scrollToPosition(mensajes.size - 1)
                        bienvenidaEnviada = true
                    }
                } else {
                    adapter.agregarMensaje(Mensaje(mensajeTexto, false, null))
                    binding.recyclerMensajes.scrollToPosition(mensajes.size - 1)
                    bienvenidaEnviada = true
                }
            } else {
                adapter.agregarMensaje(Mensaje(mensajeTexto, false, null))
                binding.recyclerMensajes.scrollToPosition(mensajes.size - 1)
                bienvenidaEnviada = true
            }
        }
    }

    private fun obtenerEmblemaLogro(idLogro: Int, onBitmapLoaded: (Bitmap?) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.170.200:8080/dogidog/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        service.obtenerEmblemaLogro(idLogro).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val inputStream = response.body()?.byteStream()
                    val bitmap = inputStream?.let { BitmapFactory.decodeStream(it) }
                    onBitmapLoaded(bitmap)
                } else {
                    Log.e("EmblemaLogro", "Error al obtener el emblema: ${response.code()}")
                    onBitmapLoaded(null)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("EmblemaLogro", "Error de conexión: ${t.message}")
                onBitmapLoaded(null)
            }
        })
    }


    // Método para hablar usando Text-to-Speech
    private fun hablar(texto: String) {
        tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // Implementar el método de inicialización de TextToSpeech
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Configuramos el idioma a Español
            val langResult = tts.setLanguage(Locale("es", "ES"))
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(requireContext(), "Idioma no soportado", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Error en la inicialización de TTS", Toast.LENGTH_SHORT).show()
        }
    }

    // Liberar el recurso de TTS cuando el fragmento es destruido
    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }



    // Configurar una voz de alta calidad
    private fun configurarVozAvanzada() {
        val voices = tts.voices // Lista de voces disponibles
        // Establecer la voz preferida (puedes elegir una voz masculina, femenina, etc.)
        for (voice in voices) {
            if (voice.name.contains("es_ES", ignoreCase = true) && voice.name.contains("male", ignoreCase = true)) {
                tts.voice = voice
                break
            }
        }
    }

    private fun configurarToolbar() {
        (activity as AppCompatActivity).supportActionBar?.apply {
            // Crear el LinearLayout para contener el TextView y la ImageView
            val linearLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL // Centrar los elementos verticalmente
            }

            // Crear el TextView con el nombre "Dogibot"
            val titleTextView = TextView(requireContext()).apply {
                text = "Dogibot"
                setTextColor(Color.WHITE) // Establecer el color blanco
                setTypeface(null, Typeface.BOLD) // Poner el texto en negrita

                // Obtener la altura de la ActionBar (Toolbar)
                val actionBarHeight = resources.getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_bar_default_height_material)

                // Ajustar el tamaño del texto proporcionalmente
                val textSize = (actionBarHeight * 0.5f).toFloat() // El tamaño del texto será el 50% de la altura
                this.textSize = textSize / resources.displayMetrics.density // Convertir a SP
            }

            // Crear la ImageView con la imagen de Dogibot
            val dogibotImageView = ImageView(requireContext()).apply {
                setImageResource(R.drawable.bordercollie)
                setColorFilter(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_bar_default_height_material),
                    resources.getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_bar_default_height_material)
                ).apply {
                    marginEnd = 8 // Ajustar el espacio entre la imagen y el texto
                }
            }

            // Agregar la imagen y el texto al LinearLayout
            linearLayout.addView(dogibotImageView)
            linearLayout.addView(titleTextView)

            // Establecer el LinearLayout como la vista personalizada de la ActionBar
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            customView = linearLayout

            // Cambiar el fondo de la ActionBar (Toolbar)
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.primario)))
        }
    }

    private fun responderBot(): String {
        val mensajeTexto = mensajes.lastOrNull()?.texto?.lowercase() ?: ""

        // Simular respuesta del bot según el texto
        return when {
            mensajeTexto.contains("hola") -> {
                "¡Hola! ¿Cómo puedo ayudarte?"
            }
            mensajeTexto.contains("adiós") || mensajeTexto.contains("chao") -> {
                "¡Adiós! ¡Que tengas un buen día!"
            }
            mensajeTexto.contains("hora") -> {
                val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                "La hora actual es $hora"
            }
            mensajeTexto.contains("temperatura") || mensajeTexto.contains("clima") -> {
                // Aquí puedes usar una API para obtener el clima si lo deseas. Por ahora, usaré una respuesta estática.
                "La temperatura actual es 22°C."
            }
            mensajeTexto.contains("quien eres") -> {
                "Soy Dogibot, un asistente virtual creado para ayudarte."
            }
            mensajeTexto.contains("como estas") -> {
                "¡Estoy genial, gracias por preguntar!"
            }
            mensajeTexto.contains("jugar") -> {
                // Comenzar el juego de adivinanza
                if (!jugando) {
                    jugando = true
                    numeroSecreto = (1..100).random() // El bot elige un número aleatorio entre 1 y 100
                    "¡Genial! Empecemos a jugar. Adivina un número entre 1 y 100."
                } else {
                    "Ya estamos jugando, ¡adelante! Adivina un número entre 1 y 100."
                }
            }
            mensajeTexto.toIntOrNull() != null && jugando -> {
                // El usuario envía un número para adivinar
                val numeroUsuario = mensajeTexto.toInt()
                when {
                    numeroUsuario < numeroSecreto!! -> {
                        "El número que buscas es mayor. ¡Sigue intentándolo!"
                    }
                    numeroUsuario > numeroSecreto!! -> {
                        "El número que buscas es menor. ¡Sigue intentándolo!"
                    }
                    else -> {
                        jugando = false
                        "¡Felicidades! Has adivinado el número correcto. El número secreto era $numeroSecreto. ¡Bien hecho!"
                    }
                }
            }
            else -> {
                "Lo siento, no entiendo eso. ¿Puedes preguntarme otra cosa?"
            }
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



    private fun buscarPreguntaEnApi(texto: String) {
        // Eliminar los signos repetidos y recortar espacios extra
        val textoLimpio = texto
            .replace("¿¿", "¿")
            .replace("??", "?")
            .replace("¡¡", "¡")
            .replace("!!", "!")
            .trim()

        // Si el texto está vacío, no agregar nada
        if (textoLimpio.isEmpty()) {
            // Aquí se podría hacer algo como retornar o simplemente no hacer nada
            return
        }

        // Agregar los signos de apertura y cierre solo si es necesario
        val textoConSignos = when {
            textoLimpio.startsWith("¿") && textoLimpio.endsWith("?") -> textoLimpio  // Ya tiene los signos correctos
            textoLimpio.startsWith("¿") -> "$textoLimpio?"  // Agregar solo el signo de cierre
            textoLimpio.endsWith("?") -> "¿$textoLimpio"  // Agregar solo el signo de apertura
            else -> "¿$textoLimpio?"  // Agregar ambos signos
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.170.200:8080/dogidog/") // ✅ Esto está correcto
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        val call = service.buscarPregunta(textoConSignos)
        call.enqueue(object : Callback<Pregunta> {
            override fun onResponse(call: Call<Pregunta?>, response: Response<Pregunta?>) {
                Log.d("API", "Código respuesta: ${response.code()}")
                Log.d("API", "Cuerpo: ${response.body()?.respuesta}")
                val preguntaResponse = response.body()

                if (preguntaResponse != null && !preguntaResponse.respuesta.isNullOrBlank()) {
                    mostrarRespuestaBot(preguntaResponse.respuesta!!)
                } else {
                    buscarRespuestaLocal(texto)
                }
            }

            override fun onFailure(call: Call<Pregunta>, t: Throwable) {
                val error = "Ocurrió un error al buscar la respuesta: ${t.message}"
                val usuario = obtenerUsuarioLocal()
                val idLogroStr = usuario?.foto

                if (idLogroStr != null) {
                    val idLogro = idLogroStr
                    if (idLogro != null) {
                        obtenerEmblemaLogro(idLogro) { bitmap ->
                            val mensajeError = Mensaje(error, false, bitmap)
                            adapter.agregarMensaje(mensajeError)
                            hablar(error)
                            binding.recyclerMensajes.scrollToPosition(mensajes.size - 1)
                        }
                    } else {
                        val mensajeError = Mensaje(error, false, null)
                        adapter.agregarMensaje(mensajeError)
                        hablar(error)
                        binding.recyclerMensajes.scrollToPosition(mensajes.size - 1)
                    }
                } else {
                    val mensajeError = Mensaje(error, false, null)
                    adapter.agregarMensaje(mensajeError)
                    hablar(error)
                    binding.recyclerMensajes.scrollToPosition(mensajes.size - 1)
                }
            }
        })
    }


    private fun buscarRespuestaLocal(pregunta: String) {
        val respuesta = responderBot()

        if (respuesta == "Lo siento, no entiendo eso. ¿Puedes preguntarme otra cosa?") {
            ofrecerEnviarAlSoporte(pregunta)
        } else {
            mostrarRespuestaBot(respuesta)
        }
    }
    private fun mostrarRespuestaBot(texto: String) {
        val usuario = obtenerUsuarioLocal()
        val idLogro = usuario?.foto

        if (idLogro != null) {
            obtenerEmblemaLogro(idLogro) { bitmap ->
                val mensajeBot = Mensaje(texto, false, bitmap)
                adapter.agregarMensaje(mensajeBot)
                hablar(texto)
                binding.recyclerMensajes.scrollToPosition(mensajes.size - 1)
                enviandoMensaje = false
            }
        } else {
            val mensajeBot = Mensaje(texto, false, null)
            adapter.agregarMensaje(mensajeBot)
            hablar(texto)
            binding.recyclerMensajes.scrollToPosition(mensajes.size - 1)
            enviandoMensaje = false
        }
    }


    private fun ofrecerEnviarAlSoporte(pregunta: String) {
        val mensaje = "No tengo respuesta para eso. ¿Quieres que envíe tu pregunta al servicio de atención al cliente?"
        mostrarRespuestaBot(mensaje)

        AlertDialog.Builder(requireContext())
            .setTitle("¿Enviar pregunta?")
            .setMessage("¿Deseas que enviemos tu pregunta al equipo de soporte?")
            .setPositiveButton("Sí") { _, _ ->
                enviarPreguntaAlSoporte(pregunta, obtenerUsuarioLocal()!!.usuario)
            }
            .setNegativeButton("No") { _, _ ->
                noEnviarPreguntaAlSoporte(pregunta)
            }
            .show()
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

    private fun enviarPreguntaAlSoporte(pregunta: String, usuarioNombre: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.170.200:8080/dogidog/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        // Obtener fecha actual en formato ISO o compatible con datetime en MySQL
        val fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        val tarea = Tarea(
            titulo = "Pregunta de usuario",
            descripcion = "$pregunta\n\nSolicitada por: $usuarioNombre",
            fecha_creacion = fechaActual,
            prioridad = "Media",
            estado = "Pendiente",
            id_empleado = null
        )

        val call = service.guardarTarea(tarea)

        call.enqueue(object : Callback<Tarea> {
            override fun onResponse(call: Call<Tarea>, response: Response<Tarea>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Pregunta enviada al soporte", Toast.LENGTH_SHORT).show()
                    mostrarRespuestaBot("Tu pregunta ha sido enviada al servicio de atención al cliente. Te responderemos pronto.")
                } else {
                    Toast.makeText(requireContext(), "Error al enviar la pregunta", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Tarea>, t: Throwable) {
                Log.e("API", "Error al enviar pregunta: ${t.message}")
                Toast.makeText(requireContext(), "Error de conexión. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun noEnviarPreguntaAlSoporte(pregunta: String) {
        val mensaje = "Tu pregunta no ha sido enviada. Espero serle de ayuda pronto, poco a poco aprendo"
        mostrarRespuestaBot(mensaje)
    }
}
