package com.example.dogidog.dataModels


data class UsuariosLogro(
    val usuario: Usuario,
    val logro: Logro,
    val fechaDesbloqueo: String?
)