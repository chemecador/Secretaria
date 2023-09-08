package com.chemecador.secretaria.requests;

import com.chemecador.secretaria.items.Note;


public class NoteRequest {

    private String title;
    private String content;
    private int status;


    public NoteRequest(Note note) {
        this.title = note.getTitle();
        this.content = note.getContent();
        this.status = note.getStatus();
    }
}
