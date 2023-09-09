package com.chemecador.secretaria.responses.login

class LoginResponse(@JvmField val id: Int, @JvmField val token: String) {

    override fun toString(): String {
        return "LoginResponse{" +
                ", id=" + id +
                ", token='" + token + '\'' +
                '}'
    }
}