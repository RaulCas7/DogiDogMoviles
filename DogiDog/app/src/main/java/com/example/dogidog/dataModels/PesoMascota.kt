package com.example.dogidog.dataModels

data class PesoMascota(
    val id: Int,
    val fecha: String,  // o LocalDate si lo conviertes
    val peso: Double
)
