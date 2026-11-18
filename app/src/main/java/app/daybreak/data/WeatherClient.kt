package app.daybreak.data

import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.roundToInt

/** Погода и поиск города через Open-Meteo (ключ не нужен). */
object WeatherClient {

    private fun get(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        conn.setRequestProperty("User-Agent", "DAYBREAK/$APP_VERSION")
        try {
            val code = conn.responseCode
            if (code !in 200..299) throw IOException("HTTP $code")
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    fun findPlace(query: String): Place? {
        val q = URLEncoder.encode(query.trim(), "UTF-8")
        val body = get("https://geocoding-api.open-meteo.com/v1/search?name=$q&count=1&language=ru&format=json")
        val results = JSONObject(body).optJSONArray("results") ?: return null
        if (results.length() == 0) return null
        val r = results.getJSONObject(0)
        return Place(r.optString("name", query.trim()), r.getDouble("latitude"), r.getDouble("longitude"))
    }

    fun current(place: Place): WeatherNow {
        val url = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=${place.lat}&longitude=${place.lon}" +
            "&current=temperature_2m,apparent_temperature,weather_code" +
            "&daily=temperature_2m_max,temperature_2m_min" +
            "&timezone=auto&forecast_days=1"
        val json = JSONObject(get(url))
        val cur = json.getJSONObject("current")
        val day = json.getJSONObject("daily")
        val temp = cur.getDouble("temperature_2m")
        return WeatherNow(
            tempC = temp.roundToInt(),
            feelsC = cur.optDouble("apparent_temperature", temp).roundToInt(),
            minC = day.getJSONArray("temperature_2m_min").optDouble(0, 0.0).roundToInt(),
            maxC = day.getJSONArray("temperature_2m_max").optDouble(0, 0.0).roundToInt(),
            text = describe(cur.optInt("weather_code", -1))
        )
    }

    private fun describe(code: Int): String = when (code) {
        0 -> "ясно"
        1 -> "преимущественно ясно"
        2 -> "переменная облачность"
        3 -> "пасмурно"
        45, 48 -> "туман"
        51, 53, 55, 56, 57 -> "морось"
        61, 63, 65, 66, 67 -> "дождь"
        71, 73, 75, 77 -> "снег"
        80, 81, 82 -> "ливни"
        85, 86 -> "снегопад"
        95, 96, 99 -> "гроза"
        else -> "без описания"
    }
}
