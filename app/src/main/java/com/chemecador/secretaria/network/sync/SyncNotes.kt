package com.chemecador.secretaria.network.sync

import android.content.Context
import com.chemecador.secretaria.R
import com.chemecador.secretaria.db.DB
import com.chemecador.secretaria.items.Note
import com.chemecador.secretaria.network.retrofit.Client
import com.chemecador.secretaria.network.retrofit.Service
import com.chemecador.secretaria.utils.PreferencesHandler
import com.chemecador.secretaria.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit

class SyncNotes {

    companion object {

        suspend fun getNotes(ctx: Context): Boolean {
            
            return withContext(Dispatchers.IO) {
                try {
                    // Obtener la instancia de Retrofit
                    val retrofit: Retrofit = Client.client

                    // Crear una instancia del servicio de la API
                    val apiService: Service = retrofit.create(Service::class.java)

                    // Utilizar el servicio para realizar llamadas a la API
                    val result: ArrayList<Note> = apiService.getNotes(
                        PreferencesHandler.getToken(ctx),
                        PreferencesHandler.getId(ctx)
                    )

                    if (result.isEmpty()) {
                        return@withContext true
                    }

                    if (DB.getInstance(ctx).setNotes(result)) {
                        true
                    } else {
                        Utils.showToast(ctx, R.string.something_went_wrong)
                        false
                    }
                } catch (e: HttpException) {
                    when (e.code()) {
                        401 -> Utils.showToast(
                            ctx,
                            e.code().toString() + " : " + ctx.getString(R.string.unauthorized)
                        )
                        else -> Utils.showToast(
                            ctx,
                            e.code().toString() + " : " + ctx.getString(R.string.server_error)
                        )
                    }
                    false
                } catch (t: Throwable) {
                    Utils.showToast(ctx, ctx.getString(R.string.connection_error))
                    false
                }
            }
        }

    }
}