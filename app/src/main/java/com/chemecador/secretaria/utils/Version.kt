package com.chemecador.secretaria.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chemecador.secretaria.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Version {

    data class Version(val name: String, val date: String, val description: String)

    class VersionAdapter(private val versions: List<Version>) :
        RecyclerView.Adapter<VersionAdapter.VersionViewHolder>() {


        inner class VersionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvVersion: TextView = itemView.findViewById(R.id.tv_versionName)
            val tvDate: TextView = itemView.findViewById(R.id.tv_date)
            val tvDescripcion: TextView = itemView.findViewById(R.id.tv_descripcion)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VersionViewHolder {
            val itemView =LayoutInflater.from(parent.context).inflate(R.layout.item_version, parent, false)
            return VersionViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: VersionViewHolder, position: Int) {
            val currentVersion = versions[position]
            holder.tvVersion.text = currentVersion.name
            holder.tvDate.text = currentVersion.date
            holder.tvDescripcion.text = currentVersion.description
        }

        override fun getItemCount(): Int {
            return versions.size
        }
    }

    companion object {

        fun getVersions(context: Context): MutableList<Version> {

            val versionsArray = context.resources.getStringArray(R.array.versions)
            val datesArray = context.resources.getStringArray(R.array.dates)
            val descriptionsArray = context.resources.getStringArray(R.array.descriptions)

            val versions = mutableListOf<Version>()

            for (i in versionsArray.indices) {
                versions.add(Version(versionsArray[i], datesArray[i], descriptionsArray[i]))
            }
            return versions
        }

        fun showPatchNotes(context: Context) {

            val dialogBuilder = MaterialAlertDialogBuilder(context)
            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.dialog_versions, null)
            val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rv)
            val layoutManager = LinearLayoutManager(context)
            recyclerView.layoutManager = layoutManager

            val adapter = VersionAdapter(getVersions(context))
            recyclerView.adapter = adapter

            dialogBuilder.setView(dialogView)
            val alertDialog = dialogBuilder.create()
            alertDialog.show()
        }
    }
}