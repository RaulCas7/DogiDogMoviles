package com.example.dogidog.dataModels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Usuario(
    val id : Int,
    val usuario: String,
    val email: String,
    val password: String,
    val contadorPreguntas: Int = 0
) : Parcelable