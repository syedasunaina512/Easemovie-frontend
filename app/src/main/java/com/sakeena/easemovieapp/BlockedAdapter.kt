package com.sakeena.easemovieapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class BlockedAdapter(
    private val list: MutableList<BlockedUser>,
    private val onUnblock: (Int) -> Unit
) : RecyclerView.Adapter<BlockedAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val btnUnblock: TextView = itemView.findViewById(R.id.btnUnblock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blocked_user, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user = list[position]
        holder.tvName.text = user.name

        holder.btnUnblock.setOnClickListener {
            val removedUser = list[position]
            onUnblock(position)

            Toast.makeText(
                holder.itemView.context,
                "${removedUser.name} unblocked",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}