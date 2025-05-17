package com.example.dogidog.dataModels

data class RecorridoRequest(
    val mascotaId: Int,
    val fecha: String,
    val distancia: Int,
    val duracion: Int
)