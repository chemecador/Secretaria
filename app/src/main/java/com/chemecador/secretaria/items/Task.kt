package com.chemecador.secretaria.items

import com.google.gson.annotations.SerializedName

/**
 * 11/05/2023
 * Clase Tarea.
 */
class Task(
    var id: Int = 0,
    var title: String,
    var content: String? = null,
    @SerializedName("start_time")
    var startTime: Long? = System.currentTimeMillis()
) {

    // Constructor vacío
    constructor() : this(0, "", "", 0)

    // Constructor sin el parámetro id
    constructor(title: String, content: String?, startTime: Long?) : this(0, title, content, startTime)


    override fun toString(): String {
        return "Task {" +
                "id = " + id +
                ", title = '" + title + '\'' +
                ", content = '" + content + '\'' +
                ", startTime = " + startTime + '}'
    }
}