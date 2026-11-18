# DAYBREAK 1.1 — Android

Утренний выпуск на телефоне: свежие заголовки из открытых RSS-лент, погода,
поиск, сохранённое. Kotlin, Jetpack Compose, Material 3. Сервер не нужен.

**Важно.** Проект написан в среде без Android SDK и без интернета, поэтому он
ни разу не компилировался. Первая сборка может выдать ошибку — пришлите лог
сборки, и я её исправлю.

## Как получить APK без Android Studio (GitHub Actions)

1. Создайте на github.com пустой репозиторий (кнопка New).
2. Распакуйте архив. В репозитории: Add file → Upload files → перетащите всё
   содержимое папки DAYBREAK, включая скрытую папку `.github`. Commit changes.
3. Откройте вкладку Actions. Сборка «Build APK» стартует сама, 5–10 минут.
4. Откройте завершённый запуск (зелёная галочка), внизу в Artifacts скачайте
   `DAYBREAK-debug-apk`. Это ZIP, внутри лежит `app-debug.apk`.
5. Перенесите `app-debug.apk` на телефон, откройте и разрешите установку из
   этого источника. Play Protect может предупредить: APK подписан
   отладочным ключом, это нормально для личной сборки.

Если папка `.github` не загрузилась (она скрытая), создайте файл вручную:
Add file → Create new file, имя `.github/workflows/build-apk.yml`, и вставьте
содержимое одноимённого файла из архива.

## Как собрать в Android Studio

Откройте папку DAYBREAK, дождитесь синхронизации Gradle и выберите
Build → Build Bundle(s) / APK(s) → Build APK(s). Файл появится в
`app/build/outputs/apk/debug/app-debug.apk`.

## Источники

Ленты по умолчанию лежат в `data/Models.kt` (объект `Catalog`). Их адреса не
проверялись из-за отсутствия сети: недоступный источник просто пропускается, а
приложение пишет, сколько источников не ответило. Свои RSS/Atom-ленты (сайты,
вакансии, каналы YouTube) добавляются во вкладке «Профиль».

Раздел «Стримы» без источников: у Twitch нет публичной RSS-ленты. Добавьте
каналы YouTube через «Свои источники».

## Структура

    app/src/main/java/app/daybreak/
      MainActivity.kt, MainViewModel.kt
      data/  Models.kt, FeedClient.kt (RSS/Atom), WeatherClient.kt (Open-Meteo), Store.kt
      ui/    Theme.kt, Format.kt, Components.kt, App.kt,
             TodayScreen.kt, SectionsScreen.kt, SavedScreen.kt,
             ProfileScreen.kt, ArticleScreen.kt

## Что дальше (DAYBREAK 2.0)

Серверная часть: проверка дат, дедупликация, ИИ-редактор, расписание
выпусков и уведомления. Сейчас всё работает прямо на телефоне.
