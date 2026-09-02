package com.example.weldcalc

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProjectAdapter(
    private val onItemClick: (Project) -> Unit,
    private val onDeleteClick: (Project) -> Unit
) : ListAdapter<Project, ProjectAdapter.ProjectViewHolder>(ProjectDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        private val tvProjectName: TextView = itemView.findViewById(R.id.tvProjectName)
        private val tvProjectDate: TextView = itemView.findViewById(R.id.tvProjectDate)
        private val tvProjectDetails: TextView = itemView.findViewById(R.id.tvProjectDetails)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(project: Project) {
            tvProjectName.text = project.name

            val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
            tvProjectDate.text = dateFormat.format(Date(project.createdAt))

            tvProjectDetails.text = "${project.profile} | ${project.material} | ${project.structureType}"

            val thumbnailFile = File(project.thumbnailPath)
            if (thumbnailFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(thumbnailFile.absolutePath)
                ivThumbnail.setImageBitmap(bitmap)
            } else {
                ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            itemView.setOnClickListener { onItemClick(project) }
            btnDelete.setOnClickListener { onDeleteClick(project) }
        }
    }

    class ProjectDiffCallback : DiffUtil.ItemCallback<Project>() {
        override fun areItemsTheSame(oldItem: Project, newItem: Project): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Project, newItem: Project): Boolean {
            return oldItem == newItem
        }
    }
}
