package com.chemecador.secretaria.items

class NotesList(
    var id: Int? = null,
    var name: String,
    /**
     * Privacidad de la nota: 0 (pública), 1 (privada)
     */
    var privacy: Int,
    /**
     * Tipo de lista: 0 (lista normal), 1 (check list)
     */
    var type: Int
) {

    override fun toString(): String {
        return "NotesList {" +
                "id = " + id +
                ", name = '" + name + '\'' +
                ", privacy = " + privacy +
                ", type = " + type + '}'
    }

    companion object {
        const val NORMAL_LIST = 0
        const val CHECK_LIST = 1
        const val PUBLIC = 0
        const val PRIVATE = 1
    }
}