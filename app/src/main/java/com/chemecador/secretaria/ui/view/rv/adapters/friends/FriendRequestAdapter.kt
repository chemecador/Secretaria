package com.chemecador.secretaria.ui.view.rv.adapters.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.databinding.ListItemFriendRequestBinding
import com.chemecador.secretaria.ui.view.rv.holders.friends.FriendRequestViewHolder
import com.chemecador.secretaria.utils.FriendshipDiffCallback

class FriendRequestAdapter(
    private val onRequestAccepted: (Friendship) -> Unit,
    private val onRequestRejected: (Friendship) -> Unit
) : ListAdapter<Friendship, FriendRequestViewHolder>(FriendshipDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val binding =
            ListItemFriendRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendRequestViewHolder(binding, onRequestAccepted, onRequestRejected)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        val request = getItem(position)
        holder.bind(request)
    }
}