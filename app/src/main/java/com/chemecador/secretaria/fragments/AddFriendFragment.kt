package com.chemecador.secretaria.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.chemecador.secretaria.R
import com.chemecador.secretaria.databinding.FragmentAddFriendBinding
import com.google.android.material.snackbar.Snackbar

class AddFriendFragment : Fragment() {
    private var binding: FragmentAddFriendBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        //inflater.inflate(R.layout.fragment_add_friend, container, false);
        binding = FragmentAddFriendBinding.inflate(layoutInflater)
        binding!!.btnConfirm.setOnClickListener { v: View ->

            // Ocultar el teclado
            val inputMethodManager =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            val friendName = binding!!.etFriendName.text.toString()
            Snackbar
                .make(
                    binding!!.root,
                    getString(R.string.invitation_sent_to) + " \"" + friendName + "\"",
                    Snackbar.LENGTH_LONG
                )
                .setAnchorView(R.id.bnv_friends)
                .show()
        }
        return binding!!.root
    }
}