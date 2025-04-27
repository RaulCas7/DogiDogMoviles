package com.example.dogidog.principal

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.speech.tts.TextToSpeech
import android.os.Bundle
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogidog.R
import com.example.dogidog.adapters.DogibotAdapter
import com.example.dogidog.dataModels.Mensaje
import com.example.dogidog.databinding.FragmentDogibotBinding
import java.text.SimpleDateFormat
import java.util.*


class DogibotFragment : Fragment(), TextToSpeech.OnInitListener {

    private lateinit var binding: FragmentDogibotBinding
    private lateinit var adapter: DogibotAdapter
    private val mensajes = mutableListOf<Mensaje>()
    private val usuarioNombre = "Raúl" // Aquí podrías obtener el nombre del usuario logueado
    private lateinit var tts: TextToSpeech
    private var numeroSecreto: Int? = null // Número secreto elegido por el bot
    private var jugando: Boolean = false // Estado del juego

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
            // Mensaje inicial del bot
            adapter.agregarMensaje(Mensaje("¡Hola! Soy Dogibot, ¿en qué puedo ayudarte?", false, R.drawable.bordercollie))
            bienvenidaEnviada = true  // Establecer que el mensaje de bienvenida ya fue enviado
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
        val mensajeTexto = binding.edtEscribir.text.toString().trim()
        if (mensajeTexto.isNotEmpty()) {
            // Agregar mensaje del usuario
            adapter.agregarMensaje(Mensaje(mensajeTexto, true, R.drawable.bordercollie))
            binding.edtEscribir.text.clear()
            binding.recyclerMensajes.scrollToPosition(mensajes.size - 1)

            // Simular respuesta del bot
            val respuestaBot = responderBot()
            adapter.agregarMensaje(Mensaje(respuestaBot, false, R.drawable.bordercollie))
            hablar(respuestaBot) // Respuesta del bot hablada

            // Desplazar el RecyclerView hacia el mensaje del bot
            binding.recyclerMensajes.scrollToPosition(mensajes.size - 1)
        }
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


}
