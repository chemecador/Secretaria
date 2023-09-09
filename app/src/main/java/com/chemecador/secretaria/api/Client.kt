package com.chemecador.secretaria.api

import com.chemecador.secretaria.logger.Logger
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import okhttp3.OkHttpClient

import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Client {
    private var retrofit: Retrofit? = null
    private const val BASE_URL = "https://todo-api.paesa.workers.dev"
    @JvmStatic
    val client: Retrofit?
        get() {
            if (retrofit == null) {
                // Create the logging interceptor
                val loggingInterceptor = HttpLoggingInterceptor { message: String? ->
                    // Print the log message
                    Logger.i("Retrofit", message!!)
                }
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY) // Set log level to show request body

                // Create OkHttpClient with the logging interceptor
                val client: OkHttpClient = OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build()

                // Crear un formato de fecha y hora personalizado
                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                // Configurar Gson con el formato personalizado
                val gson = GsonBuilder()
                    .registerTypeAdapter(
                        LocalDateTime::class.java,
                        JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                            val dateTimeString = json.asJsonPrimitive.asString
                            if (dateTimeString.isEmpty()) {
                                return@JsonDeserializer null
                            } else {
                                return@JsonDeserializer LocalDateTime.parse(
                                    dateTimeString,
                                    dateTimeFormatter
                                )
                            }
                        })
                    .create()
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            }
            return retrofit
        }
}