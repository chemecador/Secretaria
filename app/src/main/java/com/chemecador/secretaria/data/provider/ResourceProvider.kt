package com.chemecador.secretaria.data.provider

import androidx.annotation.StringRes

interface ResourceProvider {
    fun getString(@StringRes id: Int): String
    fun getStringArray(id: Int): Array<String>
}