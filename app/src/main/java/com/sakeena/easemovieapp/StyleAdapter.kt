package com.sakeena.easemovieapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StyleAdapter(
    private val styles: List<StyleModel>,
    private val onSelect: (StyleModel) -> Unit
) : RecyclerView.Adapter<StyleAdapter.StyleVH>() {

    inner class StyleVH(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgStyle)
        val text: TextView = view.findViewById(R.id.tvStyle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StyleVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_style, parent, false)
        return StyleVH(view)
    }

    override fun onBindViewHolder(holder: StyleVH, position: Int) {
        val item = styles[position]
        holder.text.text = item.name
        holder.img.setImageResource(item.icon)

        holder.itemView.setOnClickListener {
            styles.forEach { it.selected = false }
            item.selected = true
            notifyDataSetChanged()
            onSelect(item)
        }
    }

    override fun getItemCount(): Int = styles.size
}


