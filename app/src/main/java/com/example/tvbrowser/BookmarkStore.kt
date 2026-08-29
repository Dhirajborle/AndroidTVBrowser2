package com.example.tvbrowser

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object BookmarkStore {
    private const val PREFS = "tv_browser_prefs"
    private const val KEY_BOOKMARKS = "bookmarks"

    fun getAll(context: Context): List<Bookmark> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_BOOKMARKS, null) ?: return emptyList()
        val array = JSONArray(raw)
        val result = mutableListOf<Bookmark>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result.add(Bookmark(
                obj.getString("title"),
                obj.getString("url"),
                // Retrieve the path from JSON
                obj.optString("thumbnailPath", null)
            ))
        }
        return result
    }

    fun add(context: Context, bookmark: Bookmark) {
        val current = getAll(context).toMutableList()
        if (current.none { it.url == bookmark.url }) {
            current.add(bookmark)
            saveAll(context, current)
        }
    }

    fun remove(context: Context, bookmark: Bookmark) {
        val current = getAll(context).toMutableList()
        current.removeAll { it.url == bookmark.url }
        saveAll(context, current)
    }

    private fun saveAll(context: Context, items: List<Bookmark>) {
        val array = JSONArray()
        items.forEach {
            val obj = JSONObject()
            obj.put("title", it.title)
            obj.put("url", it.url)
            // Save the path to JSON
            obj.put("thumbnailPath", it.thumbnailPath)
            array.put(obj)
        }
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BOOKMARKS, array.toString()).apply()
    }

    fun updateThumbnail(context: Context, url: String, path: String) {
        val bookmarks = getAll(context).toMutableList()

        // Improved normalization to handle YouTube's mobile redirect and query params
        fun normalize(u: String): String {
            return u.lowercase()
                .replace("https://", "")
                .replace("http://", "")
                .replace("www.", "")
                .replace("m.", "") // Added: Strip the 'm.' subdomain
                .split("?")[0]     // Added: Ignore everything after '?' (query params)
                .trimEnd('/')
        }

        val targetUrl = normalize(url)
        android.util.Log.d("TVBrowser", "Target URL normalized: $targetUrl")

        val index = bookmarks.indexOfFirst {
            val storedUrl = normalize(it.url)
            android.util.Log.d("TVBrowser", "Checking against stored: $storedUrl")
            storedUrl == targetUrl
        }

        if (index != -1) {
            bookmarks[index] = bookmarks[index].copy(thumbnailPath = path)
            saveAll(context, bookmarks)
            android.util.Log.d("TVBrowser", "BookmarkStore: Successfully updated path for $targetUrl")
        } else {
            // If exact normalized match fails, try a "contains" match for YouTube specifically
            val fuzzyIndex = bookmarks.indexOfFirst {
                val stored = normalize(it.url)
                stored.contains(targetUrl) || targetUrl.contains(stored)
            }

            if (fuzzyIndex != -1) {
                bookmarks[fuzzyIndex] = bookmarks[fuzzyIndex].copy(thumbnailPath = path)
                saveAll(context, bookmarks)
                android.util.Log.d("TVBrowser", "BookmarkStore: Fuzzy matched and updated $targetUrl")
            } else {
                android.util.Log.e("TVBrowser", "BookmarkStore: URL not found: $targetUrl")
            }
        }
    }
}