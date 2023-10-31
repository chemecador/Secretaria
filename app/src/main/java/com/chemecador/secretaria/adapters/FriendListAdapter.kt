package com.chemecador.secretaria.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.R
import com.chemecador.secretaria.items.Friend

class FriendListAdapter(
    private val friendsList: List<Friend>
) : RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = friendsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // friendsList[position].let { holder.bindData(it) }
        holder.bindData(friendsList[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var tvUsername: TextView

        init {
            tvUsername = itemView.findViewById(R.id.tv_username)
        }

        fun bindData(friend: Friend) {
            tvUsername.text = friend.username
        }
    }
}