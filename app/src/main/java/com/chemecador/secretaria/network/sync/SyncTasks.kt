package com.chemecador.secretaria.network.sync

import android.content.Context
import com.chemecador.secretaria.R
import com.chemecador.secretaria.db.DB
import com.chemecador.secretaria.items.Task
import com.chemecador.secretaria.network.retrofit.Client
import com.chemecador.secretaria.network.retrofit.Service
import com.chemecador.secretaria.utils.PreferencesHandler
import com.chemecador.secretaria.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class SyncTasks {

    companion object {

        private const val className = "SyncTask"

        fun getTasks(ctx: Context, callback: (Boolean) -> Unit) {

            // Obtener la instancia de Retrofit
            val retrofit: Retrofit = Client.client!!

            // Crear una instancia del servicio de la API
            val apiService: Service = retrofit.create(Service::class.java)

            // Utilizar el servicio para realizar llamadas a la API
            val call = apiService.getTasks(
                PreferencesHandler.getToken(ctx), PreferencesHandler.getId(ctx)
            )

            // Ejecutar la llamada de forma asíncrona
            call.enqueue(object : Callback<ArrayList<Task>> {
                override fun onResponse(
                    call: Call<ArrayList<Task>>,
                    response: Response<ArrayList<Task>>
                ) {
                    if (response.isSuccessful) {
                        val result: ArrayList<Task> = response.body()!!
                        if (DB.getInstance(ctx).setTasks(result)) {
                            // Llamamos al callback con true si la sincronización fue exitosa
                            callback(true)
                        } else {
                            Utils.showToast(ctx, R.string.something_went_wrong)
                            // Llamamos al callback con false en caso de error
                            callback(false)
                        }
                    } else if (response.code() == 401) {
                        Utils.showToast(
                            ctx,
                            response.code()
                                .toString() + " : " + ctx.getString(R.string.unauthorized)
                        )
                        // Llamamos al callback con false en caso de error
                        callback(false)
                    } else {
                        Utils.showToast(
                            ctx,
                            response.code()
                                .toString() + " : " + ctx.getString(R.string.server_error)
                        )
                        // Llamamos al callback con false en caso de error
                        callback(false)
                    }
                }

                override fun onFailure(call: Call<ArrayList<Task>>, t: Throwable) {
                    Utils.showToast(ctx, ctx.getString(R.string.connection_error))
                    // Llamamos al callback con false en caso de error
                    callback(false)
                }
            })
        }
    }
}
