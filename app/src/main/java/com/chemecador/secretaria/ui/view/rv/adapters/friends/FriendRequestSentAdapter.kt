package com.chemecador.secretaria.ui.view.rv.adapters.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.databinding.ListItemFriendRequestSentBinding
import com.chemecador.secretaria.ui.view.rv.holders.friends.FriendRequestSentViewHolder
import com.chemecador.secretaria.utils.FriendshipDiffCallback

class FriendRequestSentAdapter(
    private val onRequestCancelled: (Friendship) -> Unit
) : ListAdapter<Friendship, FriendRequestSentViewHolder>(FriendshipDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestSentViewHolder {
        val binding =
            ListItemFriendRequestSentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return FriendRequestSentViewHolder(binding, onRequestCancelled)
    }

    override fun onBindViewHolder(holder: FriendRequestSentViewHolder, position: Int) {
        val request = getItem(position)
        holder.bind(request)
    }
}