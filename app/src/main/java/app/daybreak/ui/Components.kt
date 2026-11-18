package app.daybreak.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.daybreak.MainViewModel
import app.daybreak.data.Article
import app.daybreak.data.Catalog

@Composable
fun ScreenTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        color = Dawn.Paper,
        modifier = Modifier
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)
    )
}

@Composable
fun Note(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Dawn.Haze,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
    )
}

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Dawn.FirstLight,
            contentColor = Dawn.Midnight
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun StoryRow(article: Article, showCategory: Boolean = true, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            if (showCategory) {
                Text(
                    text = Catalog.titleOf(article.categoryId),
                    style = MaterialTheme.typography.labelMedium,
                    color = Dawn.FirstLight
                )
                Spacer(Modifier.height(6.dp))
            }
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleLarge,
                color = Dawn.Paper,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = article.source,
                    style = MaterialTheme.typography.labelMedium,
                    color = Dawn.Haze,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = ago(article.time),
                    style = MaterialTheme.typography.labelMedium,
                    color = Dawn.Haze,
                    maxLines = 1
                )
            }
        }
        HorizontalDivider(color = Dawn.Rule)
    }
}

@Composable
fun ArticleList(
    list: List<Article>,
    emptyText: String,
    vm: MainViewModel,
    showCategory: Boolean = true,
    state: LazyListState = rememberLazyListState()
) {
    if (list.isEmpty()) {
        Note(emptyText)
    } else {
        LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
            items(list, key = { it.link }) { a ->
                StoryRow(a, showCategory) { vm.openArticle = a }
            }
        }
    }
}
