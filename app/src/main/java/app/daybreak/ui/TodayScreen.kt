package app.daybreak.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.daybreak.MainViewModel
import app.daybreak.data.Article
import app.daybreak.data.Catalog
import java.util.Calendar

@Composable
fun TodayScreen(vm: MainViewModel, listState: LazyListState, onOpenProfile: () -> Unit) {
    val digest = vm.digest
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        item(key = "masthead") { Masthead(vm, onOpenProfile) }
        item(key = "status") { StatusLine(vm) }
        if (digest.isEmpty()) {
            item(key = "empty") { Note(emptyText(vm)) }
        } else {
            val lead = digest.first()
            item(key = "lead") { LeadStory(lead) { vm.openArticle = lead } }
            items(digest.drop(1), key = { it.link }) { a ->
                StoryRow(a) { vm.openArticle = a }
            }
        }
        item(key = "end") { Spacer(Modifier.height(24.dp)) }
    }
}

private fun emptyText(vm: MainViewModel): String {
    if (vm.articles.isEmpty()) {
        return if (vm.loadedOnce && !vm.loading) {
            "Ни один источник не ответил. Проверьте интернет и нажмите «Обновить»."
        } else {
            "Собираю выпуск…"
        }
    }
    return "За последние три дня в выбранных разделах нет новых материалов. Более старые лежат во вкладке «Разделы»."
}

/** Шапка — единственный яркий элемент: небо над горизонтом, цвет зависит от времени суток. */
@Composable
private fun Masthead(vm: MainViewModel, onOpenProfile: () -> Unit) {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val sky = horizonColors(hour)
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to sky[0],
                    0.6f to sky[1],
                    0.85f to sky[2],
                    1f to sky[3]
                )
            )
    ) {
        Column(
            Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 96.dp)
        ) {
            Text(
                text = "DAYBREAK",
                style = MaterialTheme.typography.displayLarge,
                color = Dawn.Paper
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = dateLine(),
                style = MaterialTheme.typography.titleMedium,
                color = Dawn.Haze
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = greeting(hour, vm.userName),
                style = MaterialTheme.typography.titleLarge,
                color = Dawn.Paper
            )
            Spacer(Modifier.height(6.dp))
            WeatherLine(vm, onOpenProfile)
        }
    }
}

@Composable
private fun WeatherLine(vm: MainViewModel, onOpenProfile: () -> Unit) {
    val p = vm.place
    val w = vm.weather
    if (p == null) {
        TextButton(onClick = onOpenProfile, contentPadding = PaddingValues(0.dp)) {
            Text(
                text = "Указать город для погоды",
                style = MaterialTheme.typography.labelLarge
            )
        }
    } else if (w == null) {
        Text(
            text = p.name + ": погода пока не загрузилась",
            style = MaterialTheme.typography.bodyMedium,
            color = Dawn.Paper
        )
    } else {
        Text(
            text = signed(w.tempC) + "°, " + w.text,
            style = MaterialTheme.typography.titleMedium,
            color = Dawn.Paper
        )
        Text(
            text = p.name + ". Ощущается как " + signed(w.feelsC) + "°. За сутки от " +
                signed(w.minC) + "° до " + signed(w.maxC) + "°",
            style = MaterialTheme.typography.bodyMedium,
            color = Dawn.Paper
        )
    }
}

@Composable
private fun StatusLine(vm: MainViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = statusText(vm),
            style = MaterialTheme.typography.labelMedium,
            color = Dawn.Haze,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = { vm.refresh() }, enabled = !vm.loading) {
            Text(text = "Обновить")
        }
    }
    HorizontalDivider(color = Dawn.Rule)
}

private fun statusText(vm: MainViewModel): String {
    if (vm.loading) return "Обновляю источники…"
    val parts = ArrayList<String>()
    if (vm.lastUpdated > 0L) parts.add("Обновлено " + ago(vm.lastUpdated) + ".")
    if (vm.failedSources > 0) parts.add("Не ответили источники: " + vm.failedSources + ".")
    return parts.joinToString(" ")
}

@Composable
private fun LeadStory(a: Article, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
            Text(
                text = Catalog.titleOf(a.categoryId),
                style = MaterialTheme.typography.labelMedium,
                color = Dawn.FirstLight
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = a.title,
                style = MaterialTheme.typography.headlineMedium,
                color = Dawn.Paper,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
            if (a.summary.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = a.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Dawn.Haze,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = a.source,
                    style = MaterialTheme.typography.labelMedium,
                    color = Dawn.Haze,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = ago(a.time),
                    style = MaterialTheme.typography.labelMedium,
                    color = Dawn.Haze,
                    maxLines = 1
                )
            }
        }
        HorizontalDivider(color = Dawn.Rule)
    }
}
