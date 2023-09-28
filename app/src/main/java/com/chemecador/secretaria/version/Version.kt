package com.chemecador.secretaria.version

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

    data class Version(val name: String, val description: String)

    class VersionAdapter (private val versions: List<Version>):
        RecyclerView.Adapter<VersionAdapter.VersionViewHolder>() {



        inner class VersionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvVersion: TextView = itemView.findViewById(R.id.tv_versionName)
            val tvDescripcion: TextView = itemView.findViewById(R.id.tv_descripcion)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VersionViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_version, parent, false)
            return VersionViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: VersionViewHolder, position: Int) {
            val currentVersion = versions[position]
            holder.tvVersion.text = currentVersion.name
            holder.tvDescripcion.text = currentVersion.description
        }

        override fun getItemCount(): Int {
           return versions.size
        }
    }

    companion object {
        val versions = mutableListOf(
            Version("1.0.0", "Publicación de la aplicación el Play Store. Se permite insertar, actualizar y eliminar listas, notas y tareas.\nProgramada en Java."),
            Version("1.0.1", "Migración del código a Kotlin. Se permite visualizar con más detalle las notas y las tareas.\nMejoras estéticas y de estabilidad.")
        )


        fun getLastVersion() : Version {
            return versions[versions.size - 1]
        }

        fun showPatchNotes(context: Context) {
            val dialogBuilder = MaterialAlertDialogBuilder(context)
            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.dialog_versions, null)
            val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rv)
            val layoutManager = LinearLayoutManager(context)
            recyclerView.layoutManager = layoutManager

            val adapter = VersionAdapter(versions)
            recyclerView.adapter = adapter

            dialogBuilder.setView(dialogView)
            val alertDialog = dialogBuilder.create()
            alertDialog.show()
        }
    }
}