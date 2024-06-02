package com.chemecador.secretaria.ui.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.chemecador.secretaria.R
import com.chemecador.secretaria.databinding.FragmentNotesBinding
import com.chemecador.secretaria.ui.view.main.MainActivity.Companion.TITLE_KEY
import com.chemecador.secretaria.ui.view.main.MainActivity.Companion.TITLE_REQUEST_KEY
import com.chemecador.secretaria.ui.view.main.NotesListFragment.Companion.LIST_ID
import com.chemecador.secretaria.ui.view.main.NotesListFragment.Companion.LIST_NAME
import com.chemecador.secretaria.ui.view.rv.adapters.NotesAdapter
import com.chemecador.secretaria.ui.viewmodel.main.NotesViewModel
import com.chemecador.secretaria.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotesViewModel by viewModels()
    private lateinit var adapter: NotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the title for this fragment
        setFragmentResult(TITLE_REQUEST_KEY, Bundle().apply {
            putString(TITLE_KEY, getString(R.string.title_notes, arguments?.getString(LIST_NAME)))
        })

        initUI()

    }

    private fun initUI() {

        adapter = NotesAdapter()
        binding.rv.layoutManager = LinearLayoutManager(context)
        binding.rv.adapter = adapter

        val listId = arguments?.getString(LIST_ID)
        if (listId == null) {
            binding.tvError.isVisible = true
            return
        }
        viewModel.getNotes(listId).observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.pb.isVisible = true
                    binding.tvEmpty.isVisible = false
                    binding.tvError.isVisible = false
                }

                is Resource.Success -> {
                    binding.pb.isVisible = false
                    binding.tvError.isVisible = false
                    binding.tvEmpty.isVisible = resource.data.isNullOrEmpty()
                    adapter.submitList(resource.data)
                }

                is Resource.Error -> {
                    binding.pb.isVisible = false
                    binding.tvEmpty.isVisible = false
                    binding.tvError.isVisible = resource.data.isNullOrEmpty()
                    Toast.makeText(
                        context,
                        getString(R.string.error, resource.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }


        viewModel.error.observe(viewLifecycleOwner) { error ->
            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}