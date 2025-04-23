package com.chemecador.secretaria.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.firebase.Timestamp

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val date: Timestamp = Timestamp.now(),
    val completed: Boolean = false,
    val order: Int = 0,
    val creator: String = "",
    val color: Int = Color.White.toArgb()
)
