package com.example.dogidog.dataModels


import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize


@Parcelize
data class Mascota(
    val id: Int,
    val usuario: Usuario,
    val nombre: String,
    val raza: Raza,
    val edad: Int?,
    val fechaNacimiento: String,
    val peso: Double?,
    val genero: String?,
    val esterilizado: Boolean?,
    val fechaProximaVacunacion: String?,
    val fechaProximaDesparasitacion: String?,
    val foto: String?,
    val metrosRecorridos: Long?,
    val pienso: String?,
    val microchip: String?,
    @IgnoredOnParcel
    var fotoBitmap: Bitmap? = null // Ignorar este campo en parcelación
)
 : Parcelable
