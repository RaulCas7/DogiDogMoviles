package com.example.dogidog.adapters
import android.annotation.SuppressLint
import android.graphics.Bitmap
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
import com.example.dogidog.databinding.ItemEmblemaLogroBinding
import java.text.SimpleDateFormat
import java.util.Locale

class LogrosAdapterImage : RecyclerView.Adapter<LogrosAdapterImage.LogroViewHolder>() {

    private var logros = listOf<Logro>()
    private var logrosDesbloqueados = listOf<UsuariosLogro>()
    private var listener: OnEmblemaSeleccionadoListener? = null

    fun setLogros(logros: List<Logro>) {
        this.logros = logros
        notifyDataSetChanged()
    }

    fun setLogrosDesbloqueados(logrosDesbloqueados: List<UsuariosLogro>) {
        this.logrosDesbloqueados = logrosDesbloqueados
        notifyDataSetChanged()
    }

    fun setOnEmblemaSeleccionadoListener(listener: OnEmblemaSeleccionadoListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogroViewHolder {
        val binding = ItemEmblemaLogroBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogroViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogroViewHolder, position: Int) {
        if (logros.isNotEmpty()) {
            val logro = logros[position]
            val estaDesbloqueado = logrosDesbloqueados.any { it.logro.id == logro.id }
            holder.bind(logro, estaDesbloqueado)
        }
    }

    override fun getItemCount(): Int = logros.size

    inner class LogroViewHolder(private val binding: ItemEmblemaLogroBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(logro: Logro, estaDesbloqueado: Boolean) {
            // Mostrar el emblema si está disponible
            if (logro.emblemaBitmap != null) {
                binding.imgEmblema.setImageBitmap(logro.emblemaBitmap)
            } else {
                binding.imgEmblema.setImageResource(R.drawable.borderhembra)  // Imagen predeterminada
            }

            // Si no está desbloqueado, aplicamos filtros
            if (!estaDesbloqueado) {
                binding.imgEmblema.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
                binding.root.setBackgroundColor(Color.LTGRAY)
                binding.root.isClickable = false
            } else {
                binding.imgEmblema.clearColorFilter()
                binding.root.setBackgroundColor(Color.WHITE)
                binding.root.isClickable = true

                // Evento de clic para seleccionar la imagen
                binding.root.setOnClickListener {
                    logro.emblemaBitmap?.let { it1 -> listener?.onEmblemaSeleccionado(it1, logro.id) }
                }
            }
        }
    }

    interface OnEmblemaSeleccionadoListener {
        fun onEmblemaSeleccionado(bitmap: Bitmap, nombreImagen: Int)
    }
}
