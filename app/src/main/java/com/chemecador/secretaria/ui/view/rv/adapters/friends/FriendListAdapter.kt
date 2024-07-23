package com.chemecador.secretaria.ui.view.rv.adapters.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.databinding.ListItemFriendBinding
import com.chemecador.secretaria.ui.view.rv.holders.friends.FriendViewHolder
import com.chemecador.secretaria.utils.FriendshipDiffCallback

class FriendListAdapter(
    private val onDeleteFriend: (Friendship) -> Unit
) : ListAdapter<Friendship, FriendViewHolder>(FriendshipDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding =
            ListItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = getItem(position)
        holder.bind(friend, onDeleteFriend)
    }
}


