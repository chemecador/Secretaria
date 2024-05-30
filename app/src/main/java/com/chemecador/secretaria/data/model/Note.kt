package com.chemecador.secretaria.data.model

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val completed: Boolean = false,
    val order: Int = 0
)
