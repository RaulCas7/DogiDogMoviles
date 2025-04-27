package com.example.dogidog.dataModels

data class Documentacion(
    val id: Int,
    val mascota: Mascota,  // Asegúrate de tener el modelo Mascota
    val tipo: String,
    val fecha: String,
    val descripcion: String?,
    val archivo: String?,
    val creadoEn: String
)
