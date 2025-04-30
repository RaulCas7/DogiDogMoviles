package com.example.dogidog.dataModels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MascotaCrear(
    val usuario: Usuario,
    val nombre : String,
    val raza : Raza,
    val edad : Int,
    val fechaNacimiento : String,
    val peso : Double,
    val genero : String,
    val esterilizado : Boolean,
    val fechaProximaVacunacion : String,
    val fechaProximaDesparasitacion : String,
    val foto : String,
    val metrosRecorridos : Long) : Parcelable