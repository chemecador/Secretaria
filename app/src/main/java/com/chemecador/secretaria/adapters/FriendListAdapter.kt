package com.chemecador.secretaria.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.R
import com.chemecador.secretaria.items.Friend

class FriendListAdapter(ctx: Context, list: List<Friend>?) :
    RecyclerView.Adapter<FriendListAdapter.ViewHolder?>() {
    private val friendsList: List<Friend>?
    private val mInflater: LayoutInflater
    private val ctx: Context

    init {
        mInflater = LayoutInflater.from(ctx)
        friendsList = list
        this.ctx = ctx
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = mInflater.inflate(R.layout.item_friend_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return friendsList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Obtener la tarea actual
        val friend: Friend = friendsList!![position]
        holder.bindData(friend)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView

        init {
            tvUsername = itemView.findViewById(R.id.tv_username)
        }

        fun bindData(friend: Friend) {
            tvUsername.text = friend.username
        }
    }
}