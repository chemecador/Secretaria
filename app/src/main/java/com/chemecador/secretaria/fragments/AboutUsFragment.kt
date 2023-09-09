package com.chemecador.secretaria.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chemecador.secretaria.R
import com.chemecador.secretaria.databinding.FragmentAboutUsBinding
import java.util.concurrent.atomic.AtomicInteger

class AboutUsFragment : Fragment() {
    private var binding: FragmentAboutUsBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutUsBinding.inflate(inflater, container, false)
        val version: String = try {

            //val versionName = BuildConfig.VERSION_NAME
            val versionName = "AAA"
            "Version : $versionName"
        } catch (e: Exception) {
            "Version 1.0.0"
        }
        binding!!.tvVersion.text = version
        val shortClicks = AtomicInteger()
        val longClicks = AtomicInteger()
        binding!!.tvVersion.setOnClickListener {
            shortClicks.getAndIncrement()
            if (longClicks.get() == 2) {
                // Obtener el FragmentManager del host (actividad)
                val fragmentManager = requireActivity().supportFragmentManager

                // Verificar si el AboutUsFragment está adjunto
                if (isAdded) {
                    fragmentManager.beginTransaction()
                        .hide(this) // Ocultar el AboutUsFragment actual
                        .replace(R.id.container, ExplorerFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
            if (shortClicks.get() > 2) shortClicks.set(0)
        }
        binding!!.tvVersion.setOnLongClickListener {
            longClicks.getAndIncrement()
            if (longClicks.get() > 2) longClicks.set(0)
            true
        }
        val tv = binding!!.tvContactUs
        tv.movementMethod = LinkMovementMethod.getInstance()
        tv.setOnClickListener { sendEmail() }
        return binding!!.root
    }

    private fun sendEmail() {
        val recipients = arrayOf("chemecador@gmail.com")
        val subject = "Contacto desde Secretaria"
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, recipients)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}