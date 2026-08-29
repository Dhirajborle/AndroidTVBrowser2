package com.example.tvbrowser

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import com.example.tvbrowser.Bookmark
import com.example.tvbrowser.BookmarkStore
import com.example.tvbrowser.HistoryStore

class BrowserActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var bookmarkStar: TextView
    private var currentTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        webView = findViewById(R.id.web_view)
        urlEditText = findViewById(R.id.url_edit_text)
        progressBar = findViewById(R.id.progress_bar)
        bookmarkStar = findViewById(R.id.bookmark_star)
        val goButton = findViewById<Button>(R.id.go_button)

        setupWebView()

        val urlToLoad = intent.getStringExtra("url") ?: "https://www.google.com"
        currentTitle = intent.getStringExtra("title") ?: urlToLoad
        urlEditText.setText(urlToLoad)
        webView.loadUrl(urlToLoad)

        goButton.setOnClickListener { loadFromEditText() }
        bookmarkStar.setOnClickListener { toggleBookmark() }

        urlEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                loadFromEditText()
                true
            } else false
        }
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }
        webView.isFocusable = true
        webView.isFocusableInTouchMode = true
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        webView.webViewClient = object : WebViewClient() {
            // Inside setupWebView() -> webViewClient
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Use the actual current URL from the WebView
                val currentUrl = webView.url ?: url ?: return

                urlEditText.setText(currentUrl)
                currentTitle = view?.title?.takeIf { it.isNotBlank() } ?: currentUrl

                HistoryStore.add(this@BrowserActivity, currentTitle, currentUrl)
                captureThumbnail(currentUrl)

                updateStarState()
                webView.requestFocus()
            }

        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
            }
        }
    }

    // --- FIXED CAPTURE THUMBNAIL FUNCTION ---
    private fun captureThumbnail(url: String) {
        if (webView.width <= 0 || webView.height <= 0) return

        webView.postDelayed({
            try {
                val bitmap = Bitmap.createBitmap(webView.width, webView.height, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                webView.draw(canvas)

                // Save image
                val file = File(filesDir, "thumb_${url.hashCode()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
                }

                // Update store with the EXACT URL the browser is currently showing
                val savedPath = file.absolutePath
                BookmarkStore.updateThumbnail(this, url, savedPath)
                HistoryStore.updateThumbnail(this, url, savedPath)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 1500)
    }

    private fun toggleBookmark() {
        val url = webView.url ?: return
        val bookmarks = BookmarkStore.getAll(this)
        val existing = bookmarks.find { it.url == url }

        if (existing != null) {
            BookmarkStore.remove(this, existing)
            Toast.makeText(this, "Removed bookmark", Toast.LENGTH_SHORT).show()
        } else {
            // 1. Create the bookmark object
            val newBookmark = Bookmark(webView.title ?: url, url)
            BookmarkStore.add(this, newBookmark)

            // 2. IMPORTANT: Trigger capture immediately so the new bookmark gets a thumbnail
            captureThumbnail(url)

            Toast.makeText(this, "Bookmarked", Toast.LENGTH_SHORT).show()
        }
        updateStarState()
    }

    private fun updateStarState() {
        val url = webView.url ?: return
        val isBookmarked = BookmarkStore.getAll(this).any { it.url == url }
        bookmarkStar.text = if (isBookmarked) "★" else "☆"
        bookmarkStar.setTextColor(if (isBookmarked) 0xFFFFD700.toInt() else 0xFF888888.toInt())
    }

    private fun loadFromEditText() {
        var url = urlEditText.text.toString().trim()
        if (url.isEmpty()) return
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = if (url.contains(".") && !url.contains(" ")) "https://$url"
            else "https://www.google.com/search?q=${java.net.URLEncoder.encode(url, "UTF-8")}"
        }
        webView.loadUrl(url)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}