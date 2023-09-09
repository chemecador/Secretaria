package com.chemecador.secretaria.fragments

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.chemecador.secretaria.R
import com.chemecador.secretaria.utils.PreferencesHandler

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val changePasswordPref = findPreference<Preference>("pref_key_change_password")!!
        changePasswordPref.isEnabled = PreferencesHandler.isOnline(requireContext())
        changePasswordPref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            // Crear una instancia del fragmento ChangePassFragment
            val fragment = ChangePassFragment()

            // Obtener el FragmentManager
            val fragmentManager = parentFragmentManager

            // Iniciar una transacción de fragmentos
            val transaction = fragmentManager.beginTransaction()

            // Reemplazar el fragmento actual por el fragmento ChangePassFragment
            transaction.replace(R.id.settings_container, fragment)

            // Agregar la transacción a la pila de retroceso (opcional)
            transaction.addToBackStack(null)

            // Confirmar la transacción
            transaction.commit()
            true // Devuelve true para indicar que se ha manejado el clic en la preferencia
        }
    }
}