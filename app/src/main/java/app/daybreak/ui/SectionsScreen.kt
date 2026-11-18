package app.daybreak.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.daybreak.MainViewModel
import app.daybreak.data.Catalog

@Composable
fun SectionsScreen(vm: MainViewModel, listState: LazyListState) {
    var query by rememberSaveable { mutableStateOf("") }
    val q = query.trim()
    val sid = vm.sectionId

    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Разделы")
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(text = "Поиск по загруженным материалам") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))
        if (q.isNotEmpty()) {
            val found = vm.articles.filter {
                it.title.contains(q, ignoreCase = true) || it.summary.contains(q, ignoreCase = true)
            }
            ArticleList(
                list = found.take(100),
                emptyText = "Ничего не найдено среди загруженных материалов.",
                vm = vm
            )
        } else if (sid == null) {
            CategoryList(vm, listState)
        } else {
            SectionView(vm, sid)
        }
    }
}

@Composable
private fun CategoryList(vm: MainViewModel, listState: LazyListState) {
    val ids = buildList<String> {
        Catalog.categories.forEach { add(it.id) }
        if (vm.customFeeds.isNotEmpty()) add(Catalog.CUSTOM_ID)
    }
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        items(ids, key = { it }) { id ->
            val count = vm.articles.count { it.categoryId == id }
            val feeds = Catalog.categories.firstOrNull { it.id == id }?.feeds
            val subtitle = when {
                feeds != null && feeds.isEmpty() -> "Источники не подключены"
                count == 0 && vm.loading -> "Загружаю…"
                count == 0 -> "Пока нет материалов"
                else -> count.toString() + " " + ruPlural(count, "материал", "материала", "материалов")
            }
            Column(Modifier.fillMaxWidth().clickable { vm.sectionId = id }) {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        text = Catalog.titleOf(id),
                        style = MaterialTheme.typography.titleLarge,
                        color = Dawn.Paper
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Dawn.Haze
                    )
                }
                HorizontalDivider(color = Dawn.Rule)
            }
        }
    }
}

@Composable
private fun SectionView(vm: MainViewModel, id: String) {
    val list = vm.articles.filter { it.categoryId == id }
    val feeds = Catalog.categories.firstOrNull { it.id == id }?.feeds
    val noSources = feeds != null && feeds.isEmpty()

    TextButton(
        onClick = { vm.sectionId = null },
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        Text(text = "Все разделы")
    }
    Text(
        text = Catalog.titleOf(id),
        style = MaterialTheme.typography.headlineMedium,
        color = Dawn.Paper,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
    )
    ArticleList(
        list = list,
        emptyText = if (noSources) {
            "Для этого раздела нет источников. Добавьте RSS- или Atom-ленту в профиле: подойдёт, например, канал YouTube."
        } else {
            "Пока нет материалов. Нажмите «Обновить» на вкладке «Сегодня»."
        },
        vm = vm,
        showCategory = false
    )
}
