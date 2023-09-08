package com.chemecador.secretaria.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.chemecador.secretaria.R;
import com.chemecador.secretaria.databinding.FragmentAddFriendBinding;
import com.google.android.material.snackbar.Snackbar;

public class AddFriendFragment extends Fragment {


    private FragmentAddFriendBinding binding;


    public AddFriendFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //inflater.inflate(R.layout.fragment_add_friend, container, false);
        binding = FragmentAddFriendBinding.inflate(getLayoutInflater());

        binding.btnConfirm.setOnClickListener(v -> {

            // Ocultar el teclado
            InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

            String friendName = binding.etFriendName.getText().toString();
            Snackbar
                    .make(binding.getRoot(), getString(R.string.invitation_sent_to) + " \"" + friendName + "\"", Snackbar.LENGTH_LONG)
                    .setAnchorView(R.id.fab)
                    .show();
        });
        return binding.getRoot();
    }
}