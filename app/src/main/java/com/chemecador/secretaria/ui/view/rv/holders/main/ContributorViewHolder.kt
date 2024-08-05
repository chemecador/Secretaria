package com.chemecador.secretaria.ui.view.rv.holders.main

import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.data.model.Friend
import com.chemecador.secretaria.databinding.ListItemContributorBinding


class ContributorViewHolder(private val binding: ListItemContributorBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(friend: Friend, onClick: (String) -> Unit) {
        binding.tvName.text = friend.name
        binding.root.setOnClickListener {
            onClick(friend.id)
        }
    }
}
