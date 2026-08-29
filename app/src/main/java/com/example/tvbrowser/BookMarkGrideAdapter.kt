package com.example.tvbrowser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import java.io.File

class BookmarkGridAdapter(
    private var items: List<Bookmark>,
    private val onClick: (Bookmark) -> Unit
) : RecyclerView.Adapter<BookmarkGridAdapter.CardHolder>() {

    // 1. Update ViewHolder to include the ImageView and fix names
    class CardHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.card_title)
        val url: TextView = view.findViewById(R.id.card_url)
        val thumbnail: ImageView = view.findViewById(R.id.card_thumbnail) // Ensure this ID exists in XML
    }

    fun update(newItems: List<Bookmark>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bookmark_card, parent, false)
        return CardHolder(view)
    }

    override fun onBindViewHolder(holder: CardHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.url.text = item.url

        // DEBUG LOG
        android.util.Log.d("TVBrowser", "Loading thumbnail for ${item.title}: ${item.thumbnailPath}")

        if (!item.thumbnailPath.isNullOrEmpty()) {
            val imgFile = File(item.thumbnailPath)
            if (imgFile.exists()) {
                holder.thumbnail.load(imgFile) {
                    crossfade(true)
                    placeholder(R.drawable.ic_web_placeholder)
                    error(R.drawable.ic_web_placeholder)
                }
            } else {
                holder.thumbnail.setImageResource(R.drawable.ic_web_placeholder)
            }
        } else {
            holder.thumbnail.setImageResource(R.drawable.ic_web_placeholder)
        }

        // 4. Handle D-pad clicks for Android TV
        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount() = items.size
}