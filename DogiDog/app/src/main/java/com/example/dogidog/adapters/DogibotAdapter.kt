package com.example.dogidog.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogidog.R
import com.example.dogidog.dataModels.Mensaje
import com.example.dogidog.databinding.ItemMensajeBinding



class DogibotAdapter(private val mensajes: MutableList<Mensaje>, private val usuarioNombre: String) : RecyclerView.Adapter<DogibotAdapter.MensajeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajeViewHolder {
        val binding = ItemMensajeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MensajeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MensajeViewHolder, position: Int) {
        val mensaje = mensajes[position]
        holder.bind(mensaje)
    }

    override fun getItemCount(): Int = mensajes.size

    inner class MensajeViewHolder(private val binding: ItemMensajeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mensaje: Mensaje) {
            if (mensaje.esUsuario) {
                binding.mensajeUsuario.text = mensaje.texto
                binding.nombreUsuario.text = usuarioNombre
                binding.mensajeUsuarioLayout.visibility = View.VISIBLE
                binding.mensajeBotLayout.visibility = View.GONE

                // Puedes poner la imagen del usuario si quieres aquí también
                binding.imgUsuario.setImageResource(R.drawable.bordercollie)

            } else {
                binding.mensajeBot.text = mensaje.texto
                binding.nombreBot.text = "Dogibot"
                binding.mensajeBotLayout.visibility = View.VISIBLE
                binding.mensajeUsuarioLayout.visibility = View.GONE

                if (mensaje.imagenPerfil != null) {
                    binding.imgBot.setImageBitmap(mensaje.imagenPerfil)
                } else {
                    binding.imgBot.setImageResource(R.drawable.bordercollie) // Imagen por defecto
                }
            }
        }

    }


    // Agregar un mensaje y actualizar la vista
    fun agregarMensaje(mensaje: Mensaje) {
        mensajes.add(mensaje)
        notifyItemInserted(mensajes.size - 1)
    }


}
