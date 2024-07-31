package com.chemecador.secretaria.ui.view.rv.holders.friends

import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.databinding.ListItemFriendBinding

class FriendViewHolder(private val binding: ListItemFriendBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(friend: Friendship, currentUserId: String, onDeleteFriend: (Friendship) -> Unit) {
        val friendName = if (friend.receiverId == currentUserId) {
            friend.senderName
        } else {
            friend.receiverName
        }

        binding.tvFriendName.text = friendName

        binding.btnDelete.setOnClickListener {
            onDeleteFriend(friend)
        }
    }
}

