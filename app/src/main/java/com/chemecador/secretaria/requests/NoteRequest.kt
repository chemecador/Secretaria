package com.chemecador.secretaria.requests

import com.chemecador.secretaria.items.Note

class NoteRequest(note: Note) {
    private val title: String?
    private val content: String?
    private val status: Int

    init {
        title = note.title
        content = note.content
        status = note.status
    }
}