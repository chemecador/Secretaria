package com.chemecador.secretaria.items

import com.google.gson.annotations.SerializedName

class Note {
    @SerializedName("list_id")
    var listId = 0
    var id = 0
    var title: String? = null
    var content: String? = null

    /**
     * Estado de la nota : 0 (sin terminar), 1 (terminada)
     */
    var status: Int = PRIVATE

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