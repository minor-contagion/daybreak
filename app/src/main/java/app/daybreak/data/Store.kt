package app.daybreak.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Всё локальное хранение: настройки, сохранённое, кэш последнего выпуска. */
class Store(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("daybreak", Context.MODE_PRIVATE)
    private val cacheFile = File(context.filesDir, "cache.json")

    fun loadName(): String = prefs.getString("name", "") ?: ""

    fun saveName(value: String) {
        prefs.edit().putString("name", value).apply()
    }

    fun loadEnabled(): Set<String> {
        val raw = prefs.getStringSet("enabled", null)
        return raw?.toSet() ?: Catalog.categories.map { it.id }.toSet()
    }

    fun saveEnabled(value: Set<String>) {
        prefs.edit().putStringSet("enabled", value).apply()
    }

    fun loadFeeds(): List<String> =
        (prefs.getString("feeds", "") ?: "").split('\n').filter { it.isNotBlank() }

    fun saveFeeds(value: List<String>) {
        prefs.edit().putString("feeds", value.joinToString("\n")).apply()
    }

    fun loadPlace(): Place? {
        val name = prefs.getString("place_name", null) ?: return null
        val lat = prefs.getFloat("place_lat", 0f).toDouble()
        val lon = prefs.getFloat("place_lon", 0f).toDouble()
        return Place(name, lat, lon)
    }

    fun savePlace(place: Place?) {
        val e = prefs.edit()
        if (place == null) {
            e.remove("place_name")
            e.remove("place_lat")
            e.remove("place_lon")
        } else {
            e.putString("place_name", place.name)
            e.putFloat("place_lat", place.lat.toFloat())
            e.putFloat("place_lon", place.lon.toFloat())
        }
        e.apply()
    }

    fun loadSaved(): List<Article> {
        val raw = prefs.getString("saved", null) ?: return emptyList()
        return try {
            fromJson(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveSaved(list: List<Article>) {
        prefs.edit().putString("saved", toJson(list)).apply()
    }

    fun loadCache(): List<Article> {
        return try {
            if (cacheFile.exists()) fromJson(cacheFile.readText()) else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadCacheTime(): Long = prefs.getLong("cache_time", 0L)

    fun saveCache(list: List<Article>, time: Long) {
        try {
            cacheFile.writeText(toJson(list))
            prefs.edit().putLong("cache_time", time).apply()
        } catch (e: Exception) {
        }
    }

    private fun toJson(list: List<Article>): String {
        val arr = JSONArray()
        for (a in list) {
            val o = JSONObject()
            o.put("title", a.title)
            o.put("summary", a.summary)
            o.put("link", a.link)
            o.put("source", a.source)
            o.put("categoryId", a.categoryId)
            o.put("time", a.time)
            arr.put(o)
        }
        return arr.toString()
    }

    private fun fromJson(raw: String): List<Article> {
        val arr = JSONArray(raw)
        val out = ArrayList<Article>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Article(
                    title = o.getString("title"),
                    summary = o.optString("summary"),
                    link = o.getString("link"),
                    source = o.optString("source"),
                    categoryId = o.optString("categoryId"),
                    time = o.optLong("time")
                )
            )
        }
        return out
    }
}
