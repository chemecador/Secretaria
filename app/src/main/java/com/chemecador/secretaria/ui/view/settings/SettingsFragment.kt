package com.chemecador.secretaria.ui.view.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.chemecador.secretaria.R
import com.chemecador.secretaria.databinding.FragmentSettingsBinding
import com.chemecador.secretaria.ui.view.login.LoginActivity
import com.chemecador.secretaria.ui.viewmodel.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        setupThemeSelector()
        observeViewModel()
    }

    private fun initUI() {
        binding.btnLogout.setOnClickListener { logout() }
        binding.btnContactUs.movementMethod = LinkMovementMethod.getInstance()
        binding.btnContactUs.setOnClickListener { sendEmail() }
    }

    private fun observeViewModel() {
        viewModel.email.observe(viewLifecycleOwner) {
            binding.tvEmail.text =
                if (it.isNullOrEmpty()) getString(R.string.label_data_not_provided) else it
        }


        viewModel.themeMode.observe(viewLifecycleOwner) { currentMode ->
            val themeValues = resources.getStringArray(R.array.theme_values)
            val index = themeValues.indexOf(currentMode)
            if (binding.spThemeSelector.selectedItemPosition != index) {
                binding.spThemeSelector.setSelection(index, false)
            }
        }
    }

    private fun setupThemeSelector() {
        val themeOptions = resources.getStringArray(R.array.theme_options)
        val themeValues = resources.getStringArray(R.array.theme_values)

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, themeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spThemeSelector.adapter = adapter

        binding.spThemeSelector.post {
            binding.spThemeSelector.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedMode = themeValues[position]
                        if (viewModel.themeMode.value != selectedMode) {
                            viewModel.setThemeMode(selectedMode)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
        }
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

    private fun logout() {
        viewModel.signOut()
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}