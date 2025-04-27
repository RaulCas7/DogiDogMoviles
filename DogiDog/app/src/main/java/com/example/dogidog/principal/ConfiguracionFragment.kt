package com.example.dogidog.principal

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.dogidog.R
import com.example.dogidog.databinding.FragmentConfiguracionBinding

class ConfiguracionFragment : Fragment() {

    private lateinit var binding: FragmentConfiguracionBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val PICK_FOTO_USUARIO = 100
    private val PICK_IMAGEN_BOT = 200

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

        // Cargar nombre y foto guardada
        binding.tvNombreUsuario.text = sharedPreferences.getString("usuario", "Nombre de usuario")
        sharedPreferences.getString("fotoUsuario", null)?.let {
            binding.imgFotoUsuario.setImageURI(Uri.parse(it))
        }
        sharedPreferences.getString("imagenBot", null)?.let {
            binding.imgDogiBot.setImageURI(Uri.parse(it))
        }

        // Cargar switches
        binding.switchNotificaciones.isChecked = sharedPreferences.getBoolean("notificaciones", true)
        binding.switchModoNoche.isChecked = sharedPreferences.getBoolean("modoNoche", false)
        binding.switchSilenciarDogiBot.isChecked = sharedPreferences.getBoolean("silenciarBot", false)

        // Listeners
        binding.tvCambiarFoto.setOnClickListener {
            seleccionarImagen(PICK_FOTO_USUARIO)
        }

        binding.btnEditarNombre.setOnClickListener {
            mostrarDialogoNombre()
        }

        binding.btnEditarDogiBot.setOnClickListener {
            seleccionarImagen(PICK_IMAGEN_BOT)
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

            // Verificar si la contraseña actual es correcta
            val storedPassword = sharedPreferences.getString("password", null)

            if (storedPassword == contrasenaActual) {
                if (nuevaContrasena == confirmarContrasena) {
                    // Guardar la nueva contraseña (por ejemplo, en SharedPreferences)
                    sharedPreferences.edit().putString("password", nuevaContrasena).apply()
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

    private fun seleccionarImagen(codigo: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, codigo)
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
                binding.tvNombreUsuario.text = nuevoNombre
                //sharedPreferences.edit().putString("username", nuevoNombre).apply()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEliminarCuenta() {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar cuenta")
            .setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                // Aquí deberías manejar la lógica real de eliminar cuenta
                Toast.makeText(requireContext(), "Cuenta eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cerrarSesion() {
        // Aquí deberías cerrar sesión de tu sistema de autenticación
        Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data ?: return
            when (requestCode) {
                PICK_FOTO_USUARIO -> {
                    binding.imgFotoUsuario.setImageURI(uri)
                    sharedPreferences.edit().putString("fotoUsuario", uri.toString()).apply()
                }
                PICK_IMAGEN_BOT -> {
                    binding.imgDogiBot.setImageURI(uri)
                    sharedPreferences.edit().putString("imagenBot", uri.toString()).apply()
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
}

