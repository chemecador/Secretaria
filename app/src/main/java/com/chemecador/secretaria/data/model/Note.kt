package com.chemecador.secretaria.data.model

import com.google.firebase.Timestamp

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val date: Timestamp? = null,
    val completed: Boolean = false,
    val order: Int = 0
)
