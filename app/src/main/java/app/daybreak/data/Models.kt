package app.daybreak.data

const val APP_VERSION = "1.1.0"

data class Feed(val name: String, val url: String)

data class Category(val id: String, val title: String, val feeds: List<Feed>)

data class Article(
    val title: String,
    val summary: String,
    val link: String,
    val source: String,
    val categoryId: String,
    val time: Long
)

data class Place(val name: String, val lat: Double, val lon: Double)

data class WeatherNow(
    val tempC: Int,
    val feelsC: Int,
    val minC: Int,
    val maxC: Int,
    val text: String
)

object Catalog {
    const val CUSTOM_ID = "custom"

    val categories: List<Category> = listOf(
        Category(
            "world", "Мир",
            listOf(
                Feed("BBC Русская служба", "https://feeds.bbci.co.uk/russian/rss.xml"),
                Feed("Deutsche Welle", "https://rss.dw.com/rdf/rss-ru-all"),
                Feed("The Guardian", "https://www.theguardian.com/world/rss")
            )
        ),
        Category(
            "tech", "Технологии",
            listOf(
                Feed("Хабр", "https://habr.com/ru/rss/articles/"),
                Feed("BBC Technology", "https://feeds.bbci.co.uk/news/technology/rss.xml")
            )
        ),
        Category(
            "football", "Футбол",
            listOf(
                Feed("BBC Sport", "https://feeds.bbci.co.uk/sport/football/rss.xml"),
                Feed("The Guardian", "https://www.theguardian.com/football/rss")
            )
        ),
        Category(
            "music", "Музыка",
            listOf(Feed("The Guardian", "https://www.theguardian.com/music/rss"))
        ),
        Category(
            "games", "Игры",
            listOf(Feed("The Guardian", "https://www.theguardian.com/games/rss"))
        ),
        Category("streams", "Стримы", emptyList()),
        Category(
            "science", "Наука",
            listOf(
                Feed("BBC News", "https://feeds.bbci.co.uk/news/science_and_environment/rss.xml"),
                Feed("The Guardian", "https://www.theguardian.com/science/rss")
            )
        ),
        Category(
            "business", "Экономика",
            listOf(
                Feed("BBC News", "https://feeds.bbci.co.uk/news/business/rss.xml"),
                Feed("The Guardian", "https://www.theguardian.com/business/rss")
            )
        ),
        Category(
            "jobs", "Вакансии",
            listOf(Feed("We Work Remotely", "https://weworkremotely.com/remote-jobs.rss"))
        )
    )

    fun titleOf(id: String): String =
        if (id == CUSTOM_ID) "Мои источники"
        else categories.firstOrNull { it.id == id }?.title ?: ""
}
