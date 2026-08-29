package com.example.tvbrowser

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object DownloadStore {
    private const val PREFS = "tv_browser_prefs"
    private const val KEY_DOWNLOADS = "downloads"

    fun getAll(context: Context): List<Bookmark> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_DOWNLOADS, null) ?: return emptyList()
        val array = JSONArray(raw)
        val result = mutableListOf<Bookmark>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result.add(Bookmark(obj.getString("title"), obj.getString("url")))
        }
        return result
    }

    fun add(context: Context, title: String, url: String) {
        val current = getAll(context).toMutableList()
        current.add(0, Bookmark(title, url))
        saveAll(context, current)
    }

    private fun saveAll(context: Context, items: List<Bookmark>) {
        val array = JSONArray()
        items.forEach {
            val obj = JSONObject()
            obj.put("title", it.title)
            obj.put("url", it.url)
            array.put(obj)
        }
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DOWNLOADS, array.toString()).apply()
    }
}