package com.chemecador.secretaria.ui.view.rv.holders.friends

import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.databinding.ListItemFriendRequestBinding
import com.chemecador.secretaria.utils.DateUtils

class FriendRequestViewHolder(
    private val binding: ListItemFriendRequestBinding,
    private val onRequestAccepted: (Friendship) -> Unit,
    private val onRequestRejected: (Friendship) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(request: Friendship) {
        binding.tvFriendName.text = request.senderName
        binding.tvRequestDate.text = DateUtils.formatSimple(request.requestDate)
        binding.btnAccept.setOnClickListener {
            onRequestAccepted(request)
        }
        binding.btnReject.setOnClickListener {
            onRequestRejected(request)
        }
    }
}
