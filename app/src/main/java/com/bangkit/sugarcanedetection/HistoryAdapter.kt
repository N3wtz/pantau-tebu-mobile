package com.bangkit.sugarcanedetection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class HistoryAdapter(private val items: List<ClassificationEntity>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val resultText: TextView = view.findViewById(R.id.resultText)
        val confidenceText: TextView = view.findViewById(R.id.confidenceText)
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        holder.resultText.text = item.result
        holder.confidenceText.text = "Confidence: ${item.confidence}"
        Glide.with(holder.imageView.context).load(item.imagePath).into(holder.imageView)
    }

    override fun getItemCount(): Int = items.size
}
