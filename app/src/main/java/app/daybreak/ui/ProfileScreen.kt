package app.daybreak.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.daybreak.MainViewModel
import app.daybreak.data.APP_VERSION
import app.daybreak.data.Catalog

@Composable
fun ProfileScreen(vm: MainViewModel) {
    var name by rememberSaveable { mutableStateOf(vm.userName) }
    var city by rememberSaveable { mutableStateOf(vm.place?.name ?: "") }
    var feedUrl by rememberSaveable { mutableStateOf("") }
    var confirmClear by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenTitle("Профиль")

        Block("Имя") {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    vm.setName(it)
                },
                label = { Text(text = "Как к вам обращаться") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Hint("Имя появится в приветствии на вкладке «Сегодня».")
        }

        Block("Погода") {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text(text = "Город") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                PrimaryButton(text = "Найти город") { vm.setCity(city) }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = vm.cityStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Dawn.Haze,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Hint("Данные Open-Meteo. Пустое поле отключает погоду.")
        }

        Block("Что входит в выпуск") {
            Catalog.categories.forEach { c ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = c.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Dawn.Paper
                        )
                        val names = if (c.feeds.isEmpty()) {
                            "Источники не подключены"
                        } else {
                            c.feeds.map { it.name }.distinct().joinToString(", ")
                        }
                        Text(
                            text = names,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Dawn.Haze
                        )
                    }
                    Switch(
                        checked = c.id in vm.enabled,
                        onCheckedChange = { vm.toggleCategory(c.id) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Hint("Переключатели управляют только выпуском на вкладке «Сегодня». Вкладка «Разделы» показывает всё.")
        }

        Block("Свои источники") {
            Hint("Любая RSS- или Atom-лента: сайт, блог, вакансии, канал YouTube (адрес вида youtube.com/feeds/videos.xml?channel_id=…).")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = feedUrl,
                onValueChange = { feedUrl = it },
                label = { Text(text = "Адрес ленты") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                PrimaryButton(text = "Добавить") { vm.addFeed(feedUrl) { feedUrl = "" } }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = vm.feedStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Dawn.Haze,
                    modifier = Modifier.weight(1f)
                )
            }
            vm.customFeeds.forEach { url ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Dawn.Paper,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { vm.removeFeed(url) }) {
                        Text(text = "Удалить")
                    }
                }
            }
        }

        Block("О приложении") {
            Text(
                text = "DAYBREAK " + APP_VERSION,
                style = MaterialTheme.typography.titleMedium,
                color = Dawn.Paper
            )
            Spacer(Modifier.height(4.dp))
            Hint("Заголовки собираются из открытых RSS-лент прямо на телефоне, без сервера. Тексты статей остаются у источников: кнопка «Читать у источника» открывает страницу в браузере.")
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = {
                    if (confirmClear) {
                        vm.clearSaved()
                        confirmClear = false
                    } else {
                        confirmClear = true
                    }
                },
                enabled = vm.saved.isNotEmpty()
            ) {
                Text(
                    text = if (confirmClear) {
                        "Нажмите ещё раз: удалить " + vm.saved.size
                    } else {
                        "Очистить сохранённое (" + vm.saved.size + ")"
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Block(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Dawn.Paper
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
    HorizontalDivider(color = Dawn.Rule)
}

@Composable
private fun Hint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Dawn.Haze
    )
}
