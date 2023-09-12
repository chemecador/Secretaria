package com.chemecador.secretaria.items

import com.google.gson.annotations.SerializedName



/**
 * Estado de la nota : 0 (sin terminar), 1 (terminada)
 */
class Note {
    @SerializedName("list_id")
    var listId = 0
    var id = 0
    var title: String = ""
    var content: String? = null
    var status: Int = PRIVATE

    constructor() {
        // Constructor vacío, no se requiere hacer nada adicional aquí
    }

    constructor(
        listId: Int,
        id: Int,
        title: String,
        content: String?,
        status: Int
    ) {
        this.listId = listId
        this.id = id
        this.title = title
        this.content = content
        this.status = status
    }

    override fun toString(): String {
        return "Note{" +
                "listId = " + listId +
                ", id = " + id +
                ", title = '" + title + '\'' +
                ", content = '" + content + '\'' +
                ", status = " + status + '}'
    }

    companion object {
        const val NORMAL_LIST = 0
        const val CHECK_LIST = 1
        const val PUBLIC = 0
        const val PRIVATE = 1
    }
}