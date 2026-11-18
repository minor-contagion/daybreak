package app.daybreak

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.daybreak.data.Article
import app.daybreak.data.Catalog
import app.daybreak.data.Feed
import app.daybreak.data.FeedClient
import app.daybreak.data.Place
import app.daybreak.data.Store
import app.daybreak.data.WeatherClient
import app.daybreak.data.WeatherNow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val store = Store(app)
    private var refreshJob: Job? = null

    var articles by mutableStateOf(store.loadCache())
        private set
    var loading by mutableStateOf(false)
        private set
    var loadedOnce by mutableStateOf(false)
        private set
    var failedSources by mutableStateOf(0)
        private set
    var lastUpdated by mutableStateOf(store.loadCacheTime())
        private set

    var saved by mutableStateOf(store.loadSaved())
        private set
    var enabled by mutableStateOf(store.loadEnabled())
        private set
    var customFeeds by mutableStateOf(store.loadFeeds())
        private set
    var userName by mutableStateOf(store.loadName())
        private set

    var place by mutableStateOf<Place?>(store.loadPlace())
        private set
    var weather by mutableStateOf<WeatherNow?>(null)
        private set
    var cityStatus by mutableStateOf("")
        private set
    var feedStatus by mutableStateOf("")
        private set

    var openArticle by mutableStateOf<Article?>(null)
    var sectionId by mutableStateOf<String?>(null)

    /** Выпуск: свежие материалы (до 72 часов) выбранных разделов, не больше 5 из каждого. */
    val digest: List<Article>
        get() {
            val cutoff = System.currentTimeMillis() - 72L * 60L * 60L * 1000L
            return articles
                .filter { (it.categoryId in enabled || it.categoryId == Catalog.CUSTOM_ID) && it.time >= cutoff }
                .groupBy { it.categoryId }
                .values
                .flatMap { it.take(5) }
                .sortedByDescending { it.time }
        }

    init {
        refresh()
        loadWeather()
    }

    fun refresh() {
        refreshJob?.cancel()
        loading = true
        refreshJob = viewModelScope.launch {
            val jobs = ArrayList<Pair<String, Feed>>()
            for (c in Catalog.categories) {
                for (f in c.feeds) jobs.add(Pair(c.id, f))
            }
            for (url in customFeeds) jobs.add(Pair(Catalog.CUSTOM_ID, Feed("", url)))

            val results = withContext(Dispatchers.IO) {
                coroutineScope {
                    jobs.map { job ->
                        async { runCatching { FeedClient.load(job.second, job.first) } }
                    }.awaitAll()
                }
            }

            val ok = results.count { it.isSuccess }
            if (ok > 0) {
                val merged = results
                    .flatMap { it.getOrNull().orEmpty() }
                    .distinctBy { it.link }
                    .sortedByDescending { it.time }
                val stamp = System.currentTimeMillis()
                articles = merged
                lastUpdated = stamp
                withContext(Dispatchers.IO) { store.saveCache(merged, stamp) }
            }
            failedSources = results.size - ok
            loadedOnce = true
            loading = false
        }
    }

    fun loadWeather() {
        val p = place ?: return
        viewModelScope.launch {
            weather = withContext(Dispatchers.IO) {
                runCatching { WeatherClient.current(p) }.getOrNull()
            }
        }
    }

    fun setCity(query: String) {
        val q = query.trim()
        if (q.isEmpty()) {
            place = null
            weather = null
            store.savePlace(null)
            cityStatus = "Погода отключена."
            return
        }
        cityStatus = "Ищу город…"
        viewModelScope.launch {
            val found = withContext(Dispatchers.IO) { runCatching { WeatherClient.findPlace(q) } }
            val p = found.getOrNull()
            if (p == null) {
                cityStatus = if (found.isFailure) "Нет соединения. Попробуйте ещё раз." else "Город не найден."
                return@launch
            }
            place = p
            store.savePlace(p)
            cityStatus = "Выбрано: " + p.name
            loadWeather()
        }
    }

    fun setName(value: String) {
        userName = value
        store.saveName(value)
    }

    fun toggleCategory(id: String) {
        enabled = if (id in enabled) enabled - id else enabled + id
        store.saveEnabled(enabled)
    }

    fun isSaved(a: Article): Boolean = saved.any { it.link == a.link }

    fun toggleSaved(a: Article) {
        saved = if (isSaved(a)) saved.filterNot { it.link == a.link } else listOf(a) + saved
        store.saveSaved(saved)
    }

    fun clearSaved() {
        saved = emptyList()
        store.saveSaved(saved)
    }

    fun addFeed(raw: String, onAdded: () -> Unit) {
        var text = raw.trim()
        if (text.isEmpty()) {
            feedStatus = "Введите адрес ленты."
            return
        }
        if (!text.contains("://")) text = "https://" + text
        if (text.startsWith("http://")) text = "https://" + text.removePrefix("http://")
        val url = text
        if (!url.startsWith("https://") || url.length < 12) {
            feedStatus = "Нужен адрес вида https://example.com/feed."
            return
        }
        if (url in customFeeds) {
            feedStatus = "Эта лента уже добавлена."
            return
        }
        feedStatus = "Проверяю ленту…"
        viewModelScope.launch {
            val check = withContext(Dispatchers.IO) {
                runCatching { FeedClient.load(Feed("", url), Catalog.CUSTOM_ID) }
            }
            if (check.isFailure) {
                feedStatus = "Не удалось прочитать ленту. Проверьте адрес."
                return@launch
            }
            customFeeds = customFeeds + url
            store.saveFeeds(customFeeds)
            feedStatus = "Лента добавлена."
            onAdded()
            refresh()
        }
    }

    fun removeFeed(url: String) {
        customFeeds = customFeeds - url
        store.saveFeeds(customFeeds)
        if (customFeeds.isEmpty() && sectionId == Catalog.CUSTOM_ID) sectionId = null
        refresh()
    }
}
