package com.chemecador.secretaria.items

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

/**
 * 11/05/2023
 * Clase Tarea.
 */
class Task(
    var id: Int = 0,
    var title: String? = null,
    var content: String? = null,
    @SerializedName("start_time") var startTime: LocalDateTime? = null
) {

    override fun toString(): String {
        return "Task {" +
                "id = " + id +
                ", title = '" + title + '\'' +
                ", content = '" + content + '\'' +
                ", startTime = " + startTime + '}'
    }
}