package com.example.dogidog.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogidog.R
import com.example.dogidog.dataModels.Mascota

class MascotaAdapter(
    private var listaMascotas: List<Mascota>,
    private val onClick: (Mascota) -> Unit
) : RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder>() {

    inner class MascotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagen: ImageView = itemView.findViewById(R.id.image)
        val nombre: TextView = itemView.findViewById(R.id.item_number)
        val raza: TextView = itemView.findViewById(R.id.content)
        val edad: TextView = itemView.findViewById(R.id.raza)
        val peso: TextView = itemView.findViewById(R.id.peso)

        fun bind(mascota: Mascota) {
            nombre.text = mascota.nombre
            raza.text = mascota.raza.nombre
            edad.text = "${mascota.edad} años"
            peso.text = "${mascota.peso} kg"

            Glide.with(itemView.context)
                .load(mascota.foto)
                .placeholder(R.drawable.bordercollie) // Imagen por defecto
                .into(imagen)

            itemView.setOnClickListener { onClick(mascota) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mascota, parent, false)
        return MascotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        holder.bind(listaMascotas[position])
    }

    override fun getItemCount() = listaMascotas.size

    fun actualizarLista(nuevaLista: List<Mascota>) {
        listaMascotas = nuevaLista
        notifyDataSetChanged()
    }

}
