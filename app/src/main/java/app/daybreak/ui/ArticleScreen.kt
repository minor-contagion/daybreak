package app.daybreak.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.daybreak.MainViewModel
import app.daybreak.data.Article
import app.daybreak.data.Catalog

@Composable
fun ArticleScreen(vm: MainViewModel, a: Article) {
    val context = LocalContext.current
    val isSaved = vm.isSaved(a)

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        TextButton(
            onClick = { vm.openArticle = null },
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(text = "Назад")
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = Catalog.titleOf(a.categoryId),
            style = MaterialTheme.typography.labelMedium,
            color = Dawn.FirstLight
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = a.title,
            style = MaterialTheme.typography.headlineMedium,
            color = Dawn.Paper
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = a.source,
            style = MaterialTheme.typography.labelLarge,
            color = Dawn.Haze
        )
        Text(
            text = fullDate(a.time),
            style = MaterialTheme.typography.labelMedium,
            color = Dawn.Haze
        )
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Dawn.Rule)
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (a.summary.isNotEmpty()) {
                a.summary
            } else {
                "Источник не прислал краткое содержание. Полный текст откроется по кнопке ниже."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = Dawn.Paper
        )
        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "Читать у источника",
            modifier = Modifier.fillMaxWidth()
        ) { openLink(context, a.link) }
        Spacer(Modifier.height(8.dp))
        Row {
            TextButton(onClick = { vm.toggleSaved(a) }) {
                Text(text = if (isSaved) "Убрать из сохранённого" else "Сохранить")
            }
            TextButton(onClick = { share(context, a) }) {
                Text(text = "Поделиться")
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

private fun openLink(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: Exception) {
        Toast.makeText(context, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
    }
}

private fun share(context: Context, a: Article) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, a.title + "\n" + a.link)
    }
    context.startActivity(Intent.createChooser(send, "Поделиться"))
}
