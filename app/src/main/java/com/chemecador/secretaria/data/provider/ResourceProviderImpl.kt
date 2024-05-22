package com.chemecador.secretaria.data.provider

import android.content.Context
import androidx.annotation.StringRes
import com.chemecador.secretaria.data.provider.ResourceProvider
import javax.inject.Inject

class ResourceProviderImpl @Inject constructor(private val context: Context) : ResourceProvider {
    override fun getString(@StringRes id: Int): String {
        return context.getString(id)
    }
}

