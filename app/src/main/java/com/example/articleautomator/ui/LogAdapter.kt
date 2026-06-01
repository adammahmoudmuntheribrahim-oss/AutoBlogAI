package com.example.articleautomator.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.articleautomator.R
import com.example.articleautomator.data.LogEntry
import java.text.SimpleDateFormat
import java.util.*

class LogAdapter(private var logs: MutableList<LogEntry>) : RecyclerView.Adapter<LogAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val message: TextView = view.findViewById(R.id.log_message)
        val timestamp: TextView = view.findViewById(R.id.log_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val log = logs[position]
        holder.message.text = log.message
        holder.timestamp.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
        
        when (log.status) {
            "SUCCESS" -> holder.message.setTextColor(Color.GREEN)
            "ERROR" -> holder.message.setTextColor(Color.RED)
            else -> holder.message.setTextColor(Color.BLACK)
        }
    }

    override fun getItemCount() = logs.size

    fun updateData(newLogs: List<LogEntry>) {
        logs.clear()
        logs.addAll(newLogs)
        notifyDataSetChanged()
    }
}
