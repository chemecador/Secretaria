package com.chemecador.secretaria.requests

import com.chemecador.secretaria.items.Task
import java.time.format.DateTimeFormatter

/**
 * 05/08/2023
 * Clase TaskRequest
 *
 */
class TaskRequest(task: Task) {
    private val title: String?
    private val content: String?
    private val start_time: String
    private val id: Int

    init {
        id = task.id
        title = task.title
        content = task.content

        // Formatear la fecha y hora en el formato deseado
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        start_time = task.startTime!!.format(formatter)
    }
}