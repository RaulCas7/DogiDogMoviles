package com.example.dogidog.adapters
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogidog.R
import com.example.dogidog.dataModels.Logro
import com.example.dogidog.dataModels.UsuariosLogro
import com.example.dogidog.databinding.ItemLogroBinding
import java.text.SimpleDateFormat
import java.util.Locale

class LogrosAdapter : RecyclerView.Adapter<LogrosAdapter.LogroViewHolder>() {

    private var logros = listOf<Logro>()
    private var logrosDesbloqueados = listOf<UsuariosLogro>()

    fun setLogros(logros: List<Logro>) {
        this.logros = logros
        notifyDataSetChanged()  // Notificar que los datos han cambiado
    }

    fun setLogrosDesbloqueados(logrosDesbloqueados: List<UsuariosLogro>) {
        this.logrosDesbloqueados = logrosDesbloqueados
        notifyDataSetChanged()  // Notificar que los datos han cambiado
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogroViewHolder {
        val binding = ItemLogroBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogroViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogroViewHolder, position: Int) {
        // Verifica si hay logros disponibles antes de acceder
        if (logros.isNotEmpty()) {
            val logro = logros[position]

// Comprobar si la lista de logrosDesbloqueados contiene elementos antes de acceder
            val estaDesbloqueado = logrosDesbloqueados.isNotEmpty() && logrosDesbloqueados.any { it.logro.id == logro.id }

            // Formatear la fecha de desbloqueo
            val fechaDesbloqueoFormateada = if (estaDesbloqueado) {
                formatearFecha(logrosDesbloqueados.find { it.logro.id == logro.id }?.fechaDesbloqueo ?: "")
            } else {
                "Sin obtener"
            }

            // Pasar la fecha formateada al ViewHolder
            holder.bind(logro, estaDesbloqueado, fechaDesbloqueoFormateada)
        }
    }

    override fun getItemCount(): Int {
        return logros.size
    }

    inner class LogroViewHolder(private val binding: ItemLogroBinding) : RecyclerView.ViewHolder(binding.root) {


        fun bind(logro: Logro, estaDesbloqueado: Boolean, fechaDesbloqueo: String) {

            if (logro.emblemaBitmap != null) {
                binding.imgLogro.setImageBitmap(logro.emblemaBitmap)
            } else {
                binding.imgLogro.setImageResource(R.drawable.bordercollie) // imagen por defecto
            }
            if (estaDesbloqueado) {
                binding.txtTituloLogro.text = logro.titulo
                binding.txtDescripcionLogro.text = logro.descripcion
                binding.txtFechaObtencion.text = "Conseguido el día: " + fechaDesbloqueo  // Mostrar la fecha formateada
                binding.txtTituloLogro.setTextColor(Color.BLACK)
                binding.imgLogro.clearColorFilter()
            } else {
                binding.txtTituloLogro.text = "???"  // Si no está desbloqueado
                binding.txtDescripcionLogro.text = logro.descripcion
                binding.txtFechaObtencion.text = "Sin obtener"  // Mostrar texto para los logros no desbloqueados
                binding.txtTituloLogro.setTextColor(Color.GRAY)  // Texto gris
                binding.imgLogro.setColorFilter(Color.GRAY)
            }



            // Si no está desbloqueado, podemos poner un fondo gris o similar
            binding.root.setBackgroundColor(if (estaDesbloqueado) Color.WHITE else Color.LTGRAY)
        }
    }

    fun formatearFecha(fecha: String): String {
        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()) // Formato de la fecha que recibes
        val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())  // Formato deseado

        return try {
            val date = formatoEntrada.parse(fecha)
            formatoSalida.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            fecha  // Si la fecha no se puede formatear, devuelve la fecha original
        }
    }
}
