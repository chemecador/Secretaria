package com.chemecador.secretaria.requests;

import com.chemecador.secretaria.items.Task;

import java.time.format.DateTimeFormatter;

/**
 * 05/08/2023
 * Clase TaskRequest
 *
 * */

public class TaskRequest {


    private String title;
    private String content;
    private String start_time;
    private int id;


    public TaskRequest(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.content = task.getContent();

        // Formatear la fecha y hora en el formato deseado
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.start_time = task.getStartTime().format(formatter);
    }
}
