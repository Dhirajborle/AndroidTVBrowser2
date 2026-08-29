package com.example.tvbrowser

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import android.widget.EditText
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val addTile = Bookmark("+ Add Bookmark", "action://add")
    private lateinit var grid: RecyclerView
    private lateinit var adapter: BookmarkGridAdapter
    private lateinit var navHome: TextView
    private lateinit var navHistory: TextView
    private lateinit var navBookmarks: TextView
    private lateinit var navDownloads: TextView
    private lateinit var urlEditText: EditText
    private lateinit var gobutton: Button
    private var currentSection = Section.HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navHome = findViewById(R.id.nav_home)
        navHistory = findViewById(R.id.nav_history)
        navBookmarks = findViewById(R.id.nav_bookmarks)
        navDownloads = findViewById(R.id.nav_downloads)
        grid = findViewById(R.id.content_grid)
        urlEditText = findViewById(R.id.url_edit_text)
        gobutton = findViewById(R.id.go_button)

        gobutton.setOnClickListener {
            loadFromEditText()
        }

        urlEditText.setOnEditorActionListener { _, actionId, _ ->
            loadFromEditText()
            true
        }

        grid.layoutManager = GridLayoutManager(this, 3)
        adapter = BookmarkGridAdapter(emptyList()) { item -> onItemChosen(item) }
        grid.adapter = adapter

        setupNavItem(navHome, Section.HOME)
        setupNavItem(navHistory, Section.HISTORY)
        setupNavItem(navBookmarks, Section.BOOKMARKS)
        setupNavItem(navDownloads, Section.DOWNLOADS)

        navHome.requestFocus()
        showSection(Section.HOME)
    }

    override fun onResume() {
        super.onResume()
        // Refresh the grid data whenever the user returns to this screen
        refreshData()
        showSection(currentSection)
    }

    private fun refreshData() {
        val bookmarks = BookmarkStore.getAll(this)
        adapter.update(bookmarks) // Assuming your adapter has an update function
    }

    // Wires d-pad focus, mouse hover, and click/tap to all instantly switch section.
    private fun setupNavItem(view: TextView, section: Section) {
        view.setOnFocusChangeListener { v, hasFocus ->
            v.isSelected = hasFocus
            if (hasFocus) showSection(section)
        }
        view.setOnHoverListener { v, event ->
            if (event.action == MotionEvent.ACTION_HOVER_ENTER) {
                v.requestFocus()
                showSection(section)
            }
            false
        }
        view.setOnClickListener {
            it.requestFocus()
            showSection(section)
        }
    }

    private fun showSection(section: Section) {
        currentSection = section
        val items: List<Bookmark> = when (section) {
            Section.HOME -> BookmarkStore.getAll(this) + addTile
            Section.BOOKMARKS -> BookmarkStore.getAll(this) + addTile
            Section.HISTORY -> HistoryStore.getAll(this).ifEmpty {
                listOf(Bookmark("No history yet", "action://none"))
            }
            Section.DOWNLOADS -> DownloadStore.getAll(this).ifEmpty {
                listOf(Bookmark("No downloads yet", "action://none"))
            }
        }
        adapter.update(items)
    }

    private fun onItemChosen(item: Bookmark) {
        when {
            item.url == addTile.url -> startActivity(Intent(this, AddBookmarkActivity::class.java))
            item.url.startsWith("action://") -> { /* placeholder row, do nothing */ }
            else -> {
                val intent = Intent(this, BrowserActivity::class.java)
                intent.putExtra("url", item.url)
                intent.putExtra("title", item.title)
                startActivity(intent)
            }
        }
    }

    private fun loadFromEditText() {
        var url = urlEditText.text.toString().trim()
        if (url.isEmpty()) return

        val finalUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            if (url.contains(".") && !url.contains(" ")) {
                "https://$url"
            } else {
                "https://www.google.com/search?q=${java.net.URLEncoder.encode(url, "UTF-8")}"
            }
        } else {
            url
        }

        val intent = Intent(this, BrowserActivity::class.java)
        intent.putExtra("url", finalUrl)
        startActivity(intent)
    }
}