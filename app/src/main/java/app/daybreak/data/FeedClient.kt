package app.daybreak.data

import android.net.Uri
import android.text.Html
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Загружает и разбирает RSS 2.0, RSS 1.0 (RDF) и Atom. Работает только в фоновом потоке. */
object FeedClient {

    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14) DAYBREAK/$APP_VERSION"

    fun load(feed: Feed, categoryId: String): List<Article> {
        val conn = URL(feed.url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 15_000
        conn.setRequestProperty("User-Agent", USER_AGENT)
        conn.setRequestProperty(
            "Accept",
            "application/rss+xml, application/atom+xml, application/xml, text/xml, */*"
        )
        try {
            val code = conn.responseCode
            if (code !in 200..299) throw IOException("HTTP $code")
            val result = conn.inputStream.use { parse(it, feed, categoryId) }
            if (result.isEmpty()) throw IOException("В ленте нет записей")
            return result
        } finally {
            conn.disconnect()
        }
    }

    private fun parse(input: InputStream, feed: Feed, categoryId: String): List<Article> {
        val p = Xml.newPullParser()
        p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        p.setInput(input, null)

        val out = ArrayList<Article>()
        var channelTitle = ""
        var inItem = false
        var title = ""
        var link = ""
        var guid = ""
        var date = ""
        var summary = ""

        var event = p.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                val name = p.name
                if (name == "item" || name == "entry") {
                    inItem = true
                    title = ""
                    link = ""
                    guid = ""
                    date = ""
                    summary = ""
                } else if (name == "title") {
                    val t = readText(p)
                    if (inItem) {
                        title = t
                    } else if (channelTitle.isEmpty()) {
                        channelTitle = t.trim()
                    }
                } else if (inItem) {
                    when (name) {
                        "link" -> {
                            val href: String? = p.getAttributeValue(null, "href")
                            val rel: String? = p.getAttributeValue(null, "rel")
                            val t = readText(p).trim()
                            if (!href.isNullOrBlank()) {
                                if ((rel == null || rel == "alternate") && link.isEmpty()) {
                                    link = href.trim()
                                }
                            } else if (t.isNotEmpty() && link.isEmpty()) {
                                link = t
                            }
                        }
                        "guid", "id" -> {
                            val t = readText(p).trim()
                            if (guid.isEmpty()) guid = t
                        }
                        "pubDate", "published", "updated", "dc:date" -> {
                            val t = readText(p)
                            if (date.isEmpty()) date = t
                        }
                        "description", "summary", "content", "content:encoded", "media:description" -> {
                            val t = readText(p)
                            if (summary.isEmpty()) summary = t
                        }
                    }
                }
            } else if (event == XmlPullParser.END_TAG && (p.name == "item" || p.name == "entry")) {
                inItem = false
                val cleanTitle = clean(title)
                val url = if (link.isNotEmpty()) link else if (guid.startsWith("http")) guid else ""
                if (cleanTitle.isNotEmpty() && url.isNotEmpty()) {
                    val sourceName = feed.name.ifBlank { channelTitle.ifBlank { hostOf(feed.url) } }
                    out.add(
                        Article(
                            title = cleanTitle,
                            summary = shorten(clean(summary.take(8000)), 500),
                            link = url,
                            source = sourceName,
                            categoryId = categoryId,
                            time = parseDate(date)
                        )
                    )
                }
            }
            event = p.next()
        }
        return out
    }

    /** Читает текст элемента до его закрывающего тега, пропуская вложенные теги. */
    private fun readText(p: XmlPullParser): String {
        val sb = StringBuilder()
        var depth = 1
        while (depth > 0) {
            when (p.next()) {
                XmlPullParser.TEXT -> sb.append(p.text)
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.END_DOCUMENT -> return sb.toString()
            }
        }
        return sb.toString()
    }

    private fun clean(raw: String): String {
        if (raw.isBlank()) return ""
        val text = Html.fromHtml(raw, Html.FROM_HTML_MODE_COMPACT).toString()
        return text.replace('\uFFFC', ' ').replace(Regex("\\s+"), " ").trim()
    }

    private fun shorten(s: String, max: Int): String {
        if (s.length <= max) return s
        val cut = s.substring(0, max)
        val space = cut.lastIndexOf(' ')
        val base = if (space > max / 2) cut.substring(0, space) else cut
        return base.trimEnd(',', '.', ';', ':', ' ') + "…"
    }

    private fun hostOf(url: String): String = Uri.parse(url).host ?: url

    private fun parseDate(raw: String): Long {
        val s = raw.trim()
        if (s.isEmpty()) return 0L
        try {
            return ZonedDateTime.parse(s, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
        } catch (e: Exception) {
        }
        try {
            return OffsetDateTime.parse(s).toInstant().toEpochMilli()
        } catch (e: Exception) {
        }
        try {
            val parsed = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US).parse(s)
            if (parsed != null) return parsed.time
        } catch (e: Exception) {
        }
        return 0L
    }
}
