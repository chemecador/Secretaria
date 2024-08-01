package com.chemecador.secretaria.data.model

import com.google.firebase.Timestamp

data class Friendship(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderName: String = "",
    val receiverName: String = "",
    val receiverCode: String = "",
    val requestDate: Timestamp = Timestamp.now(),
    val acceptanceDate: Timestamp? = null
)
