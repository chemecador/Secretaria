package com.chemecador.secretaria.requests

import com.google.gson.annotations.SerializedName

class PasswordRequest(
    @field:SerializedName("password") private val oldPassword: String, @field:SerializedName(
        "new_password"
    ) private val newPassword: String
)