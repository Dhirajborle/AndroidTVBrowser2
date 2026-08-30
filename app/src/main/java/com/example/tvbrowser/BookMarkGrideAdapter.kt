package com.example.tvbrowser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import java.io.File

class BookmarkGridAdapter(
    private var items: List<Bookmark>,
    private val onClick: (Bookmark) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_BOOKMARK = 0
        private const val TYPE_ADD = 1
    }

    // Normal bookmark card
    class CardHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.card_title)
        val url: TextView = view.findViewById(R.id.card_url)
        val thumbnail: ImageView = view.findViewById(R.id.card_thumbnail)
    }

    // Add Bookmark button
    class AddHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.add_bookmark_button)
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].url == "action://add") {
            TYPE_ADD
        } else {
            TYPE_BOOKMARK
        }
    }

    fun update(newItems: List<Bookmark>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == TYPE_ADD) {

            val view = inflater.inflate(
                R.layout.item_add_bookmark,
                parent,
                false
            )

            AddHolder(view)

        } else {

            val view = inflater.inflate(
                R.layout.item_bookmark_card,
                parent,
                false
            )

            CardHolder(view)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        val item = items[position]

        if (holder is AddHolder) {

            holder.button.setOnClickListener {
                onClick(item)
            }

            holder.button.setOnFocusChangeListener { view, hasFocus ->
                view.isSelected = hasFocus
            }

        } else if (holder is CardHolder) {

            holder.title.text = item.title
            holder.url.text = item.url

            android.util.Log.d(
                "TVBrowser",
                "Loading thumbnail for ${item.title}: ${item.thumbnailPath}"
            )

            if (!item.thumbnailPath.isNullOrEmpty()) {

                val imgFile = File(item.thumbnailPath)

                if (imgFile.exists()) {

                    holder.thumbnail.load(imgFile) {
                        crossfade(true)
                        placeholder(R.drawable.ic_web_placeholder)
                        error(R.drawable.ic_web_placeholder)
                    }

                } else {

                    holder.thumbnail.setImageResource(
                        R.drawable.ic_web_placeholder
                    )
                }

            } else {

                holder.thumbnail.setImageResource(
                    R.drawable.ic_web_placeholder
                )
            }

            holder.itemView.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}