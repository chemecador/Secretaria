package com.chemecador.secretaria.ui.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.databinding.FragmentNoteDetailBinding
import com.chemecador.secretaria.ui.view.main.NotesFragment.Companion.NOTE_ID
import com.chemecador.secretaria.ui.view.main.NotesListFragment.Companion.LIST_ID
import com.chemecador.secretaria.ui.viewmodel.main.NoteDetailViewModel
import com.chemecador.secretaria.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoteDetailFragment : Fragment() {

    private var _binding: FragmentNoteDetailBinding? = null
    private val binding get() = _binding!!

    // Inyectar ViewModel
    private val viewModel: NoteDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        val noteId = requireArguments().getString(NOTE_ID)
        val listId = requireArguments().getString(LIST_ID)

        if (listId.isNullOrBlank() || noteId.isNullOrBlank()) {
            binding.tvError.isVisible = true
            return binding.root
        }

        viewModel.getNoteById(listId, noteId).observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.pb.isVisible = true
                    binding.content.isVisible = false
                    binding.tvError.isVisible = false
                }

                is Resource.Success -> {
                    binding.pb.isVisible = false
                    binding.content.isVisible = true
                    binding.tvError.isVisible = false
                    resource.data?.let { note ->
                        bind(note)
                    }
                }

                is Resource.Error -> {
                    binding.pb.isVisible = false
                    binding.tvError.isVisible = true
                    binding.content.isVisible = false
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        return binding.root
    }

    private fun bind(note: Note) {
        binding.tvTitle.text = note.title
        binding.tvContent.text = note.content
        binding.tvDate.text = note.date?.toDate().toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
