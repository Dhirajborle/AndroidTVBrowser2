package com.example.tvbrowser

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object HistoryStore {
    private const val PREFS = "tv_browser_prefs"
    private const val KEY_HISTORY = "history"
    private const val MAX_ITEMS = 30

    fun getAll(context: Context): List<Bookmark> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val array = JSONArray(raw)
        val result = mutableListOf<Bookmark>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            // FIX: Retrieve the thumbnailPath from JSON if it exists
            result.add(Bookmark(
                obj.getString("title"),
                obj.getString("url"),
                obj.optString("thumbnailPath", null) // optString returns null if key is missing
            ))
        }
        return result
    }

    fun add(context: Context, title: String, url: String) {
        val current = getAll(context).toMutableList()
        current.removeAll { it.url == url }
        current.add(0, Bookmark(title, url))
        saveAll(context, current.take(MAX_ITEMS))
    }

    private fun saveAll(context: Context, items: List<Bookmark>) {
        val array = JSONArray()
        items.forEach {
            val obj = JSONObject()
            obj.put("title", it.title)
            obj.put("url", it.url)
            // FIX: Save the thumbnailPath to JSON
            obj.put("thumbnailPath", it.thumbnailPath)
            array.put(obj)
        }
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    fun updateThumbnail(context: Context, url: String, path: String) {
        val history = getAll(context).toMutableList()
        val targetUrl = url.trim().removeSuffix("/")

        val index = history.indexOfFirst {
            it.url.trim().removeSuffix("/") == targetUrl
        }

        if (index != -1) {
            history[index] = history[index].copy(thumbnailPath = path)
            saveAll(context, history)
            android.util.Log.d("TVBrowser", "HistoryStore: Saved path for $targetUrl")
        } else {
            // It's normal to see this if the URL is not in History yet
            android.util.Log.e("TVBrowser", "HistoryStore: URL not found $targetUrl")
        }
    }

    fun clear(context: Context) = saveAll(context, emptyList())
}