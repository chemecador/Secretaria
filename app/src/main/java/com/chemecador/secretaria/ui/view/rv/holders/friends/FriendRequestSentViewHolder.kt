package com.chemecador.secretaria.ui.view.rv.holders.friends

import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.databinding.ListItemFriendRequestSentBinding
import com.chemecador.secretaria.utils.DateUtils

class FriendRequestSentViewHolder(
    private val binding: ListItemFriendRequestSentBinding,
    private val onRequestCancelled: (Friendship) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(request: Friendship) {
        binding.tvFriendName.text = request.senderName
        binding.tvRequestDate.text = DateUtils.formatSimple(request.requestDate)
        binding.btnCancel.setOnClickListener {
            onRequestCancelled(request)
        }
    }
}
