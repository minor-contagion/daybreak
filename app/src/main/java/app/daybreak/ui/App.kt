package app.daybreak.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.daybreak.MainViewModel

enum class MainTab(val label: String) {
    Today("Сегодня"),
    Sections("Разделы"),
    Saved("Сохранённое"),
    Profile("Профиль")
}

@Composable
fun DaybreakApp(vm: MainViewModel) {
    var tab by rememberSaveable { mutableStateOf(MainTab.Today) }
    val todayState = rememberLazyListState()
    val sectionsState = rememberLazyListState()
    val savedState = rememberLazyListState()

    val article = vm.openArticle
    val inSection = tab == MainTab.Sections && vm.sectionId != null

    BackHandler(enabled = article != null) { vm.openArticle = null }
    BackHandler(enabled = article == null && inSection) { vm.sectionId = null }
    BackHandler(enabled = article == null && !inSection && tab != MainTab.Today) {
        tab = MainTab.Today
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Dawn.Midnight,
        contentColor = Dawn.Paper
    ) {
        Column(Modifier.fillMaxSize().imePadding()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (article != null) {
                    ArticleScreen(vm, article)
                } else {
                    when (tab) {
                        MainTab.Today -> TodayScreen(vm, todayState) { tab = MainTab.Profile }
                        MainTab.Sections -> SectionsScreen(vm, sectionsState)
                        MainTab.Saved -> SavedScreen(vm, savedState)
                        MainTab.Profile -> ProfileScreen(vm)
                    }
                }
            }
            TabBar(tab) { target ->
                vm.openArticle = null
                if (target == tab && target == MainTab.Sections) vm.sectionId = null
                tab = target
            }
        }
    }
}

@Composable
private fun TabBar(current: MainTab, onSelect: (MainTab) -> Unit) {
    Column(Modifier.fillMaxWidth().background(Dawn.Midnight).navigationBarsPadding()) {
        HorizontalDivider(color = Dawn.Rule)
        Row(Modifier.fillMaxWidth()) {
            MainTab.entries.forEach { t ->
                val active = t == current
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(t) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .width(20.dp)
                                .height(2.dp)
                                .background(if (active) Dawn.FirstLight else Color.Transparent)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = t.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (active) Dawn.Paper else Dawn.Haze,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
