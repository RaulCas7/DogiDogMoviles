package com.example.dogidog.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogidog.R
import com.example.dogidog.dataModels.Foto

class FotosAdapter(private val fotos: List<Foto>) : RecyclerView.Adapter<FotosAdapter.FotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_imagen, parent, false)
        return FotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val foto = fotos[position]
        // Aquí puedes cargar la imagen en el ImageView usando Glide o Picasso
        Glide.with(holder.itemView)
            .load(foto.imageUri)
            .into(holder.imgFoto)
    }

    override fun getItemCount(): Int = fotos.size

    class FotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgFoto: ImageView = view.findViewById(R.id.imgFoto) // Asegúrate de que el item_imagen tenga un ImageView con esta ID
    }
}