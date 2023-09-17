package com.chemecador.secretaria.network.retrofit

import java.io.IOException

class ApiException : IOException {
    private val responseCode: Int

    constructor(responseCode: Int, message: String?) : super(message) {
        this.responseCode = responseCode
    }

    constructor(responseCode: Int) {
        this.responseCode = responseCode
    }
}