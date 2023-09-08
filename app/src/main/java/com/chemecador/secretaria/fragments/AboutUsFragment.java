package com.chemecador.secretaria.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.chemecador.secretaria.R;
import com.chemecador.secretaria.databinding.FragmentAboutUsBinding;

import java.util.concurrent.atomic.AtomicInteger;

public class AboutUsFragment extends Fragment {

    private FragmentAboutUsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAboutUsBinding.inflate(inflater, container, false);

        String version;
        try {
            version = "Version : " + requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0).versionName;
        } catch (Exception e) {
            version = "Version 1.0.0";
        }


        binding.tvVersion.setText(version);

        AtomicInteger shortClicks = new AtomicInteger();
        AtomicInteger longClicks = new AtomicInteger();
        binding.tvVersion.setOnClickListener(v -> {

            shortClicks.getAndIncrement();
            if (longClicks.get() == 2) {
                // Obtener el FragmentManager del host (actividad)
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

                // Verificar si el AboutUsFragment está adjunto
                if (isAdded()) {
                    fragmentManager.beginTransaction()
                            .hide(this) // Ocultar el AboutUsFragment actual
                            .replace(R.id.container, new ExplorerFragment())
                            .addToBackStack(null)
                            .commit();
                }
            }
            if (shortClicks.get() > 2) shortClicks.set(0);
        });

        binding.tvVersion.setOnLongClickListener(v -> {
            longClicks.getAndIncrement();
            if (longClicks.get() > 2) longClicks.set(0);
            return true;
        });

        TextView tv = binding.tvContactUs;
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setOnClickListener(v -> sendEmail());

        return binding.getRoot();
    }

    private void sendEmail() {
        String[] recipients = {"chemecador@gmail.com"};
        String subject = "Contacto desde Secretaria";

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);

        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
