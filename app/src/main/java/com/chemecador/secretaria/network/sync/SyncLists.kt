package com.chemecador.secretaria.network.sync

import android.content.Context
import com.chemecador.secretaria.R
import com.chemecador.secretaria.db.DB
import com.chemecador.secretaria.items.NotesList
import com.chemecador.secretaria.network.retrofit.Client.client
import com.chemecador.secretaria.network.retrofit.Service
import com.chemecador.secretaria.utils.PreferencesHandler
import com.chemecador.secretaria.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class SyncLists {

    companion object {

        private const val className = "SyncList"

        fun getLists(ctx: Context, callback: (Boolean) -> Unit) {
            // Obtener la instancia de Retrofit
            val retrofit: Retrofit = client

            // Crear una instancia del servicio de la API
            val apiService: Service = retrofit.create(Service::class.java)

            // Utilizar el servicio para realizar llamadas a la API
            val call: Call<ArrayList<NotesList>> =
                apiService.getLists(PreferencesHandler.getToken(ctx), PreferencesHandler.getId(ctx))

            // Ejecutar la llamada de forma asíncrona
            call.enqueue(object : Callback<ArrayList<NotesList>> {
                override fun onResponse(
                    call: Call<ArrayList<NotesList>>,
                    response: Response<ArrayList<NotesList>>
                ) {
                    if (response.isSuccessful) {
                        val result: ArrayList<NotesList> = response.body()!!
                        if (DB.getInstance(ctx).setLists(result)) {
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
                            response.code().toString() + " : " + ctx.getString(R.string.unauthorized)
                        )
                        // Llamamos al callback con false en caso de error
                        callback(false)
                    } else {
                        Utils.showToast(ctx,response.code().toString() + " : " + ctx.getString(R.string.server_error))
                        // Llamamos al callback con false en caso de error
                        callback(false)
                    }
                }

                override fun onFailure(call: Call<ArrayList<NotesList>>, t: Throwable) {
                    Utils.showToast(ctx, ctx.getString(R.string.connection_error))
                    // Llamamos al callback con false en caso de error
                    callback(false)
                }
            })
        }
    }
}
