package com.chemecador.secretaria.api;

import java.io.IOException;

public class ApiException extends IOException {

    private final int responseCode;

    public ApiException(int responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }

    public ApiException(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }
}