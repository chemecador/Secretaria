package com.chemecador.secretaria.api;

import com.chemecador.secretaria.logger.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class Client {

    private static Retrofit retrofit;
    private static final String BASE_URL = "https://todo-api.paesa.workers.dev";

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Create the logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                // Print the log message
                Logger.i("Retrofit", message);
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Set log level to show request body

            // Create OkHttpClient with the logging interceptor
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            // Crear un formato de fecha y hora personalizado
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // Configurar Gson con el formato personalizado
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> {
                        String dateTimeString = json.getAsJsonPrimitive().getAsString();
                        if (dateTimeString.isEmpty()) {
                            return null;
                        } else {
                            return LocalDateTime.parse(dateTimeString, dateTimeFormatter);
                        }
                    })
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
