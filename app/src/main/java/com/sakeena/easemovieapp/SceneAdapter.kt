package com.sakeena.easemovieapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class SceneAdapter(
    private val items: MutableList<SceneModel>,
    private val listener: SceneListener
) : RecyclerView.Adapter<SceneAdapter.VH>() {

    interface SceneListener {
        fun onEdit(scene: SceneModel, position: Int)
        fun onRegenerate(scene: SceneModel, position: Int)
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgScene)
        val tvText: TextView = view.findViewById(R.id.tvSceneText)
        val btnEdit: Button = view.findViewById(R.id.btnEditScene)
        val btnReg: Button = view.findViewById(R.id.btnRegenerate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_scene, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvText.text = item.text

        // Load image with Glide; handle null by showing placeholder
        val imgPath = item.imagePath
        if (!imgPath.isNullOrEmpty()) {
            // Glide will handle http URLs and file paths
            Glide.with(holder.img.context)
                .load(imgPath)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.img)
        } else {
            holder.img.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.btnEdit.setOnClickListener { listener.onEdit(item, position) }
        holder.btnReg.setOnClickListener { listener.onRegenerate(item, position) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItem(pos: Int, newItem: SceneModel) {
        if (pos in 0 until items.size) {
            items[pos] = newItem
            notifyItemChanged(pos)
        }
    }

    fun setAll(list: List<SceneModel>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }
}
