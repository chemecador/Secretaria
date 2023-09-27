package com.chemecador.secretaria.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.R
import com.chemecador.secretaria.interfaces.OnItemClickListener
import com.chemecador.secretaria.items.Task

class TaskAdapter(ctx: Context, private val taskList: MutableList<Task>) :
    RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(ctx)
    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = mInflater.inflate(R.layout.item_task, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Obtener la tarea actual
        val mTask = taskList[position]
        holder.bindData(mTask)
        // Asignar un listener de clic al elemento de la lista
        //holder.itemView.setOnClickListener { showTask(mTask) }

        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView

        init {
            tvTitle = itemView.findViewById(R.id.tv_title)
        }

        fun bindData(task: Task) {
            tvTitle.text = task.title
        }
    }
}