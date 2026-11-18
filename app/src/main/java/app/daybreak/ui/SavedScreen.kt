package app.daybreak.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.daybreak.MainViewModel

@Composable
fun SavedScreen(vm: MainViewModel, listState: LazyListState) {
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Сохранённое")
        ArticleList(
            list = vm.saved,
            emptyText = "Здесь появятся материалы, которые вы сохраните. Откройте любую статью и нажмите «Сохранить».",
            vm = vm,
            state = listState
        )
    }
}
