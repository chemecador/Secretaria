package com.chemecador.secretaria.items

import java.time.LocalDate

class Friend(
    /** ID del amigo  */
    var id: Int,
    /** Username del amigo  */
    var username: String?,
    /** Tiempo que lleváis siendo amigos  */
    var since: LocalDate?
) {

}