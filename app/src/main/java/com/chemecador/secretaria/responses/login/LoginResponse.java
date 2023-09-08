package com.chemecador.secretaria.responses.login;

public class LoginResponse {

    private final int id;
    private final String token;

    public LoginResponse(int id, String token) {
        this.id = id;
        this.token = token;
    }


    public int getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                ", id=" + id +
                ", token='" + token + '\'' +
                '}';
    }
}
