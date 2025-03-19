package com.example.dogidog.dataModels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class Raza(
    val id : Int,
    val nombre : String,
    val energia : Byte,
    val inteligencia: Byte,
    val socializacion : Byte,
    val cuidado: Byte,
    val pesoMinMacho: Double,
    val pesoMinHembra: Double,
    val pesoMaxMacho : Double,
    val pesoMaxHembra : Double,
    val edadMaxima : Int,
    val descripcion : String,
    val datoCurioso : String
) : Parcelable
