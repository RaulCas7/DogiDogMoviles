package com.example.dogidog.dataModels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant

@Parcelize
data class Pregunta(
    val id: Int,
    val pregunta: String,
    val respuesta: String?,
    val audioRespuesta: String?,
    val contadorConsultas: Int,
    val fechaUltimaConsulta: String?,
    val fechaCreacion: String?
    ) : Parcelable
