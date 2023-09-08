package com.chemecador.secretaria.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.chemecador.secretaria.adapters.FriendRequestAdapter;
import com.chemecador.secretaria.databinding.FragmentFriendRequestBinding;
import com.chemecador.secretaria.items.Friend;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestFragment extends Fragment {

    private FragmentFriendRequestBinding binding;
    private RecyclerView rv;
    private List<Friend> friendList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFriendRequestBinding.inflate(getLayoutInflater());
        rv = binding.rvRequests;
        friendList = new ArrayList<>();

        if (friendList.isEmpty()){
            binding.tvNoRequests.setVisibility(View.VISIBLE);
        } else {
            binding.tvNoRequests.setVisibility(View.GONE);
        }
        FriendRequestAdapter adapter = new FriendRequestAdapter(requireContext(), friendList);
        rv.setAdapter(adapter);

        return binding.getRoot();
    }
}