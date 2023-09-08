package com.chemecador.secretaria.items

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

/**
 * 11/05/2023
 * Clase Tarea.
 */
class Task(
    var id: Int,
    var title: String?,
    var content: String?,
    @SerializedName("start_time") var startTime: LocalDateTime?
) {

    override fun toString(): String {
        return "Task {" +
                "id = " + id +
                ", title = '" + title + '\'' +
                ", content = '" + content + '\'' +
                ", startTime = " + startTime + '}'
    }
}