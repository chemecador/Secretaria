package com.chemecador.secretaria.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.chemecador.secretaria.adapters.FriendListAdapter;
import com.chemecador.secretaria.databinding.FragmentFriendListBinding;
import com.chemecador.secretaria.items.Friend;

import java.util.ArrayList;
import java.util.List;

public class FriendListFragment extends Fragment {

    private FragmentFriendListBinding binding;
    private RecyclerView rv;
    private List<Friend> friendList;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFriendListBinding.inflate(getLayoutInflater());
        rv = binding.rvFriends;
        friendList = new ArrayList<>();

        if (friendList.isEmpty()){
            binding.tvNoFriends.setVisibility(View.VISIBLE);
        } else {
            binding.tvNoFriends.setVisibility(View.GONE);
        }
        FriendListAdapter adapter = new FriendListAdapter(requireContext(), friendList);
        rv.setAdapter(adapter);


        return binding.getRoot();
    }
}