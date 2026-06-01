package com.example.articleautomator.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.articleautomator.R

class RssAdapter(
    private val urls: MutableList<String>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<RssAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val urlText: TextView = view.findViewById(R.id.rss_url_text)
        val deleteBtn: ImageButton = view.findViewById(R.id.delete_rss)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rss, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.urlText.text = urls[position]
        holder.deleteBtn.setOnClickListener { onDelete(position) }
    }

    override fun getItemCount() = urls.size

    fun addUrl(url: String) {
        urls.add(url)
        notifyItemInserted(urls.size - 1)
    }
}
