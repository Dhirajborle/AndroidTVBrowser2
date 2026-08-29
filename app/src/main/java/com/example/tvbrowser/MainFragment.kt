package com.example.tvbrowser

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*

class MainFragment : BrowseSupportFragment() {

    private val addTile = Bookmark("+ Add Bookmark", "action://add")

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        title = getString(R.string.app_name)
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = Color.parseColor("#1565C0")

        loadRows()

        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            val bookmark = item as? Bookmark ?: return@OnItemViewClickedListener
            when (bookmark.url) {
                addTile.url -> startActivity(Intent(activity, AddBookmarkActivity::class.java))
                else -> {
                    val intent = Intent(activity, BrowserActivity::class.java)
                    intent.putExtra("url", bookmark.url)
                    intent.putExtra("title", bookmark.title)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadRows()
    }

    private fun loadRows() {
        val ctx = requireContext()
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = CardPresenter()

        val homeAdapter = ArrayObjectAdapter(cardPresenter)
        BookmarkStore.getAll(ctx).forEach { homeAdapter.add(it) }
        homeAdapter.add(addTile)
        rowsAdapter.add(ListRow(HeaderItem(0, "Home"), homeAdapter))

        val historyAdapter = ArrayObjectAdapter(cardPresenter)
        val history = HistoryStore.getAll(ctx)
        if (history.isEmpty()) historyAdapter.add(Bookmark("No history yet", "action://none"))
        else history.forEach { historyAdapter.add(it) }
        rowsAdapter.add(ListRow(HeaderItem(1, "History"), historyAdapter))

        val bookmarksAdapter = ArrayObjectAdapter(cardPresenter)
        BookmarkStore.getAll(ctx).forEach { bookmarksAdapter.add(it) }
        bookmarksAdapter.add(addTile)
        rowsAdapter.add(ListRow(HeaderItem(2, "Bookmarks"), bookmarksAdapter))

        val downloadsAdapter = ArrayObjectAdapter(cardPresenter)
        val downloads = DownloadStore.getAll(ctx)
        if (downloads.isEmpty()) downloadsAdapter.add(Bookmark("No downloads yet", "action://none"))
        else downloads.forEach { downloadsAdapter.add(it) }
        rowsAdapter.add(ListRow(HeaderItem(3, "Downloads"), downloadsAdapter))

        adapter = rowsAdapter
    }
}