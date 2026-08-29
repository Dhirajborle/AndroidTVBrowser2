package com.example.tvbrowser

import android.graphics.Color
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter

class CardPresenter : Presenter() {
    private val cardWidth = 313
    private val cardHeight = 176

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        cardView.setBackgroundColor(Color.DKGRAY)

        cardView.setOnFocusChangeListener { v, hasFocus ->
            v.animate()
                .scaleX(if (hasFocus) 1.08f else 1f)
                .scaleY(if (hasFocus) 1.08f else 1f)
                .setDuration(150)
                .start()
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val bookmark = item as Bookmark
        val cardView = viewHolder.view as ImageCardView
        cardView.titleText = bookmark.title
        cardView.contentText = bookmark.url
        cardView.setMainImageDimensions(cardWidth, cardHeight)
        cardView.mainImageView.setBackgroundColor(Color.rgb(60, 60, 60))
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.badgeImage = null
        cardView.mainImage = null
    }
}