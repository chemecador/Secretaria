package com.chemecador.secretaria.requests;

import com.google.gson.annotations.SerializedName;

public class PasswordRequest {

    @SerializedName("password")
    private final String oldPassword;

    @SerializedName("new_password")
    private final String newPassword;

    public PasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
