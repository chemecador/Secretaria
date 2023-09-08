package com.chemecador.secretaria.responses;

public class IdResponse {

    private final int id;

    public IdResponse(int id) {
        this.id = id;
    }


    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "IdResponse{" +
                ", id=" + id +
                '}';
    }
}
