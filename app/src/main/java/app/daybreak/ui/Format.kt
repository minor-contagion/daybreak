package app.daybreak.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val RU: Locale = Locale.forLanguageTag("ru")

fun ago(time: Long): String {
    if (time <= 0L) return ""
    val minutes = (System.currentTimeMillis() - time) / 60_000L
    return when {
        minutes < 1 -> "только что"
        minutes < 60 -> "$minutes мин назад"
        minutes < 24 * 60 -> "${minutes / 60} ч назад"
        minutes < 48 * 60 -> "вчера"
        else -> SimpleDateFormat("d MMMM", RU).format(Date(time))
    }
}

fun fullDate(time: Long): String =
    if (time <= 0L) "Дата не указана источником"
    else SimpleDateFormat("d MMMM yyyy, HH:mm", RU).format(Date(time))

fun clock(time: Long): String = SimpleDateFormat("HH:mm", RU).format(Date(time))

fun dateLine(): String =
    SimpleDateFormat("EEEE, d MMMM", RU).format(Date()).replaceFirstChar { it.uppercase() }

fun greeting(hour: Int, name: String): String {
    val base = when (hour) {
        in 5..11 -> "Доброе утро"
        in 12..17 -> "Добрый день"
        in 18..22 -> "Добрый вечер"
        else -> "Доброй ночи"
    }
    val n = name.trim()
    return if (n.isEmpty()) base else "$base, $n"
}

fun signed(n: Int): String = when {
    n > 0 -> "+$n"
    n < 0 -> "−${-n}"
    else -> "0"
}

fun ruPlural(n: Int, one: String, few: String, many: String): String {
    val m100 = n % 100
    val m10 = n % 10
    return when {
        m100 in 11..14 -> many
        m10 == 1 -> one
        m10 in 2..4 -> few
        else -> many
    }
}
