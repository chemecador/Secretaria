package com.chemecador.secretaria.fragments;

import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.chemecador.secretaria.R;
import com.chemecador.secretaria.utils.PreferencesHandler;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference changePasswordPref = findPreference("pref_key_change_password");
        assert changePasswordPref != null;
        changePasswordPref.setEnabled(PreferencesHandler.isOnline(requireContext()));
        changePasswordPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                // Crear una instancia del fragmento ChangePassFragment
                ChangePassFragment fragment = new ChangePassFragment();

                // Obtener el FragmentManager
                FragmentManager fragmentManager = getParentFragmentManager();

                // Iniciar una transacción de fragmentos
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                // Reemplazar el fragmento actual por el fragmento ChangePassFragment
                transaction.replace(R.id.settings_container, fragment);

                // Agregar la transacción a la pila de retroceso (opcional)
                transaction.addToBackStack(null);

                // Confirmar la transacción
                transaction.commit();
                return true; // Devuelve true para indicar que se ha manejado el clic en la preferencia
            }
        });
    }
}