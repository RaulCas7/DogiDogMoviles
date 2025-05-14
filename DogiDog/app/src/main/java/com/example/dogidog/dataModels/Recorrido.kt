package com.example.dogidog.dataModels

data class Recorrido(
    val mascota: Mascota, // o solo mascotaId si prefieres usar un DTO simple
    val fecha: String, // Formato "yyyy-MM-dd"
    val distancia: Int?,
    val duracion: Int?
)