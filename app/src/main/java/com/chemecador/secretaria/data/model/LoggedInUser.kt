package com.chemecador.secretaria.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
class LoggedInUser(val userId: String, @JvmField val displayName: String)