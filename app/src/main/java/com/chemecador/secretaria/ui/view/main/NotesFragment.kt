package com.chemecador.secretaria.ui.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.chemecador.secretaria.R
import com.chemecador.secretaria.databinding.FragmentNotesBinding
import com.chemecador.secretaria.ui.view.main.MainActivity.Companion.TITLE_KEY
import com.chemecador.secretaria.ui.view.main.MainActivity.Companion.TITLE_REQUEST_KEY
import com.chemecador.secretaria.ui.view.main.NotesListFragment.Companion.LIST_ID
import com.chemecador.secretaria.ui.view.main.NotesListFragment.Companion.LIST_NAME
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listId = arguments?.getString(LIST_ID)
        val listName = arguments?.getString(LIST_NAME)

        // Set the title for this fragment
        setFragmentResult(TITLE_REQUEST_KEY, Bundle().apply {
            putString(TITLE_KEY, getString(R.string.title_notes, listName))
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}