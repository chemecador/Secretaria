package com.chemecador.secretaria.ui.view.rv.adapters.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.chemecador.secretaria.data.model.Friend
import com.chemecador.secretaria.databinding.ListItemContributorBinding
import com.chemecador.secretaria.ui.view.rv.holders.main.ContributorViewHolder
import com.chemecador.secretaria.utils.FriendDiffCallback

class ContributorAdapter(
    private val onClick: (String) -> Unit
) : ListAdapter<Friend, ContributorViewHolder>(FriendDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContributorViewHolder {
        val binding =
            ListItemContributorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContributorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContributorViewHolder, position: Int) {
        val friend = getItem(position)
        holder.bind(friend, onClick)
    }
}
