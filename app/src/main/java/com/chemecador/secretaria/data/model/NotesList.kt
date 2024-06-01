package com.chemecador.secretaria.data.model

import com.google.firebase.Timestamp

data class NotesList(
    var id: String = "",
    val name: String = "",
    val observers: List<String> = emptyList(),
    val date: Timestamp = Timestamp.now(),
    val type: String = "",
    val notes: List<Note> = emptyList()
)