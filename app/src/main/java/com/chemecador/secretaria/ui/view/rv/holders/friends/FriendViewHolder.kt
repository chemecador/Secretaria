package com.chemecador.secretaria.ui.view.rv.holders.friends

import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.databinding.ListItemFriendBinding

class FriendViewHolder(private val binding: ListItemFriendBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(friend: Friendship, onDeleteFriend: (Friendship) -> Unit) {

        binding.tvFriendName.text = friend.receiverName

        binding.btnDelete.setOnClickListener {
            onDeleteFriend(friend)
        }
    }
}
