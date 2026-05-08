package com.sakeena.easemovieapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProjectsAdapter(private val list: List<ProjectModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_EMPTY = 0
    private val TYPE_PROJECT = 1

    override fun getItemViewType(position: Int): Int {
        return if (list.isEmpty()) TYPE_EMPTY else TYPE_PROJECT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : RecyclerView.ViewHolder {

        return if (viewType == TYPE_EMPTY) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_empty_projects, parent, false)
            object : RecyclerView.ViewHolder(v) {}
        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_project, parent, false)
            ProjectVH(v)
        }
    }

    override fun getItemCount(): Int = if (list.isEmpty()) 1 else list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
        if (holder is ProjectVH) {
            val item = list[pos]
            holder.bind(item)
        }
    }

    class ProjectVH(v: View) : RecyclerView.ViewHolder(v) {
        fun bind(p: ProjectModel) {
            itemView.findViewById<TextView>(R.id.tvTitle).text = p.title
            itemView.findViewById<TextView>(R.id.tvTime).text = p.lastTime
            itemView.findViewById<TextView>(R.id.tvImages)
                .text = "🖼 ${p.imageCount} images"
            itemView.findViewById<TextView>(R.id.tvVoice)
                .text = if (p.hasVoice) "🎤 Voice added" else "🎤 No voice"

            itemView.findViewById<ProgressBar>(R.id.progress).progress = p.progress
        }
    }
}
