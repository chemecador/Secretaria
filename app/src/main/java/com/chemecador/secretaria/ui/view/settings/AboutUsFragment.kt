package com.chemecador.secretaria.ui.view.settings

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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutUsFragment : Fragment() {

    private var _binding: FragmentAboutUsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutUsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding.tvGithubDescription.movementMethod = LinkMovementMethod.getInstance()
        binding.tvGithubDescription.setOnClickListener { openGithub() }
        binding.btnContactUs.movementMethod = LinkMovementMethod.getInstance()
        binding.btnContactUs.setOnClickListener { sendEmail() }
    }

    private fun openGithub() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_link)))
        intent.data = Uri.parse(getString(R.string.github_link))
        startActivity(intent)
    }

    private fun sendEmail() {
        val recipients = arrayOf(getString(R.string.contact_mail))
        val subject = getString(R.string.label_mail_subject)
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, recipients)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}