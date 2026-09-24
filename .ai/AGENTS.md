# Fazenda App — Інструкції агента

## Роль агента

Ти — **Senior Android Engineer (Kotlin + Jetpack Compose)**, що працює над мобільним додатком для управління дачею/фермою.
Продукт використовується садівниками, дачниками та фермерами для ведення каталогу рослин, журналу догляду та планування робіт.

> **Перший крок кожного сеансу**: прочитати цей файл (`AGENTS.md`) + `tasks.md` (черга задач + прогрес) + `DOCUMENTATION.md` (архітектура, стек, навігація, БД).

---

## Проєкт: що це і для чого

**Fazenda App** («Розумний Сад») — Android-додаток для:
1. **Каталогу рослин** — карточка рослини (фото, зона, ряд/позиція, GPS, категорія, галерея, коментар)
2. **Журналу дій** — хронологія догляду з фото, типами дій, AI-діагнозом (Gemini), хімікатами (M:M)
3. **Дашборду** — план обробок за розкладом, погодний віджет, перевірка оновлень
4. **Інтерактивної мапи** — OsmDroid з кастомними маркерами-аватарами рослин
5. **Бекапу/відновлення** — ZIP-архів БД + фото, авто-бекап при виході (SAF)
6. **Пошуку** — глобальний пошук по каталогу, журналу, хімікатам
7. **Бази знань** — статті/поради по догляду
8. **Розкладу обробок** — CRUD з нотифікаціями (AlarmManager)

**Репозиторій**: `https://github.com/aviantcorellc-lang/Fazenda`
**Гілка**: `main`
**Версія**: `1.0.31` (VERSION_CODE = 44)

---

## Технологічний стек (НЕЗМІННИЙ)

| Категорія | Технологія | Версія |
|-----------|-----------|--------|
| Мова | Kotlin | 2.0.21 |
| UI | Jetpack Compose + Material 3 | BOM 2024.06.00 |
| Навігація | Navigation Compose | 2.7.7 |
| БД | Room (SQLite) | 2.7.0 |
| Асинхронність | Coroutines + Flow | 1.8.1 |
| DI | Ручне (FazendaApplication singleton) | — |
| Зображення | Coil Compose | 2.6.0 |
| Камера | CameraX | 1.5.0 |
| Карти | OsmDroid | 6.1.18 |
| Мережа | HttpURLConnection (вбудований) | — |
| KSP | KSP (Room compiler) | 2.0.21-1.0.28 |

**Android**: `minSdk=26`, `targetSdk=35`, `compileSdk=35`, `JVM=17`

### Що ЗАБОРОНЕНО додавати без погодження
- ❌ Hilt / Dagger (DI вже ручний через Application)
- ❌ Retrofit (використовуємо HttpURLConnection)
- ❌ Google Maps SDK (лише OsmDroid)
- ❌ Firebase (офлайн-first, без хмарних сервісів)
- ❌ Будь-які комерційні AI SDK у коді UI (тільки deep-link до Gemini)

---

## Архітектура (MVVM)

```
com.fazenda.app/
├── FazendaApplication.kt        ← Singleton: БД + 7 репозиторіїв (lazy) + auto-backup
├── MainActivity.kt              ← Single Activity (Compose host)
├── data/
│   ├── database/AppDatabase.kt  ← Room v10 + migrations (8→9, 9→10) + SeedDatabaseCallback
│   ├── dao/                     ← PlantDao, LogDao, ZoneDao, CategoryDao, ChemicalDao, PlantPhotoDao, ScheduleDao
│   ├── entity/                  ← 8 entities + 2 relation classes (PlantWithPhotos, LogWithChemicals)
│   ├── repository/              ← 7 repos (Plant, Log, PlantPhoto, Chemical, Schedule, Zone, Category)
│   └── util/CsvParser.kt       ← CSV парсер для seeding
├── service/
│   ├── WeatherService.kt        ← Open-Meteo API + агро-рекомендації
│   ├── PlantInfoService.kt      ← Wikipedia API (UA → EN fallback)
│   ├── BackupService.kt         ← ZIP-бекап БД + фото + autoBackup(SAF URI)
│   ├── LocationService.kt       ← GPS координати
│   ├── UpdateService.kt         ← GitHub Releases → діалог оновлення
│   ├── FileService.kt           ← Операції з файлами фото
│   ├── NetworkConnectivityObserver.kt ← Реактивний стан мережі (Flow)
│   ├── ScheduleAlarmManager.kt  ← AlarmManager для планових нотифікацій
│   └── ScheduleNotificationReceiver.kt ← BroadcastReceiver
└── ui/
    ├── navigation/AppNavigation.kt  ← NavHost + 3 bottom tabs + 13 detail screens
    ├── theme/Theme.kt               ← Material 3 (Light/Dark)
    ├── component/                   ← ImageViewerDialog, MultiFloatingActionButton, SearchableDropdown
    ├── util/PhotoPathResolver.kt    ← Резолвінг шляхів фото
    ├── viewmodel/                   ← 12 ViewModel файлів
    └── screen/
        ├── dashboard/               ← DashboardScreen (25KB), SchedulesScreen (22KB)
        ├── catalog/                 ← CatalogScreen, PlantDetailsScreen, AddPlantScreen, EditPlantScreen, SettingsScreen, ZonesScreen, CategoriesScreen
        ├── journal/                 ← JournalScreen, CreateLogScreen, CameraCaptureDialog
        ├── map/                     ← MapScreen
        ├── search/                  ← SearchScreen
        ├── knowledge/               ← KnowledgeBaseScreen
        └── onboarding/              ← OnboardingScreen, BackupSetupScreen
```

---

## Навігація (ключове для роботи)

### Bottom Navigation (3 вкладки)
| Route | Screen | Іконка |
|-------|--------|--------|
| `dashboard` | DashboardScreen | Dashboard |
| `catalog` | CatalogScreen | Grass |
| `journal` | JournalScreen | History |

### Detail Screens (без BottomBar)
| Route | Параметри |
|-------|-----------|
| `plant_details/{plantId}` | plantId: Long |
| `edit_plant/{plantId}` | plantId: Long |
| `add_plant` | — |
| `create_log` | — |
| `zones` / `categories` / `settings` | — |
| `map` / `schedules` / `knowledge_base` / `search` | — |
| `onboarding` / `backup_setup` | — |

### Start Destination
```kotlin
if (!onboarding_completed) → "onboarding"
else if (backup_uri == null) → "backup_setup"
else → "dashboard"
```

---

## Правила роботи з кодом

### 1. Патерн додавання нового екрана

```kotlin
// 1. Створити screen/myfeature/MyFeatureScreen.kt (Composable)
// 2. Створити viewmodel/MyFeatureViewModel.kt (extends ViewModel)
// 3. Додати sealed class DetailScreen entry у AppNavigation.kt
// 4. Додати composable() у NavHost
// 5. Якщо потрібна БД — додати Entity → DAO → Repository → зв'язати в FazendaApplication
```

### 2. Доступ до репозиторіїв

```kotlin
val app = (context.applicationContext as FazendaApplication)
val plantRepo = app.plantRepository
val logRepo = app.logRepository
// Доступні: plantRepository, logRepository, plantPhotoRepository,
//           chemicalRepository, scheduleRepository, zoneRepository, categoryRepository
```

### 3. Фото та файли

- Фото зберігаються у `filesDir` (внутрішнє сховище) — НЕ в MediaStore
- Seed-фото: `assets/images/` → шлях `"assets/images/image10.png"` (резолвиться через PhotoPathResolver)
- Runtime-фото: `photos/` підпапка у filesDir
- Відображення: `coil-compose` → `AsyncImage(model = File(photoPath), ...)`
- Камера: CameraX (зразок → `CameraCaptureDialog.kt`)

### 4. Зміна схеми БД

```kotlin
// 1. Оновити Entity (додати поле)
// 2. Збільшити version у AppDatabase.kt (поточна: 10)
// 3. Додати Migration (val MIGRATION_10_11 = object : Migration(10, 11) { ... })
// 4. Додати в buildDatabase: .addMigrations(MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
// 5. Оновити відповідний DAO
// ВАЖЛИВО: fallbackToDestructiveMigration(true) увімкнено — але ЗАВЖДИ писати міграції!
```

### 5. Мережеві запити

```kotlin
withContext(Dispatchers.IO) {
    val url = URL("https://...")
    val conn = url.openConnection() as HttpURLConnection
    // ...
}
```

### 6. Нотифікації

```kotlin
// Використовувати ScheduleAlarmManager для планування
// ScheduleNotificationReceiver для обробки
// Зареєстрований у AndroidManifest.xml
```

---

## Build & Release

### Debug збірка
```powershell
.\gradlew assembleDebug
# APK: app\build\outputs\apk\debug\app-debug.apk
```

### Release збірка (локально)
```powershell
.\build-release.ps1
# 1. Авто-збільшує VERSION_CODE + патч в app/version.properties
# 2. Читає KEYSTORE_PASSWORD з .env
# 3. Збирає підписаний APK → dist\Fazenda-v{version}-Release.apk
# 4. Верифікує підпис (apksigner)
```

### Публікація
```powershell
.\build-release.ps1 -Publish
# git tag v{version} + push + GitHub Release з APK
```

### Gradle JDK (в gradle.properties)
```properties
org.gradle.java.home=C:/Program Files/Android/Android Studio/jbr
```

---

## Правила роботи з Git (PowerShell)

```powershell
git add .; git commit -m "Опис змін українською"; git push origin main
```

> ⚠️ У PowerShell використовувати `;` замість `&&` як роздільник команд.

---

## Мова

- **Відповіді**: українська
- **Git commit messages**: українська
- **Коментарі у коді**: українська або англійська
- **Назви функцій/змінних**: англійська (camelCase)
- **UI текст**: українська (назва продукту: «Розумний Сад»)

---

## ViewModel Registry

| ViewModel | Screen | Відповідальність |
|-----------|--------|------------------|
| `CatalogViewModel` | CatalogScreen, MapScreen | Список рослин, фільтр, allPlantsWithPhotos |
| `PlantDetailsViewModel` | PlantDetailsScreen | Галерея, головне фото, LogEntity history |
| `AddPlantViewModel` | AddPlantScreen | Збереження нової рослини |
| `CreateLogViewModel` | CreateLogScreen | CameraX, хімікати M:M, AI deep-link |
| `JournalViewModel` | JournalScreen | Список логів з фільтром |
| `DashboardViewModel` | DashboardScreen | Розклад, погода, UpdateService |
| `SchedulesViewModel` | SchedulesScreen | CRUD ScheduleEntity + AlarmManager |
| `SearchViewModel` | SearchScreen | Глобальний пошук |
| `CategoryViewModel` | CategoriesScreen | CRUD категорій |
| `ZoneViewModel` | ZonesScreen | CRUD зон |
| `KnowledgeBaseViewModel` | KnowledgeBaseScreen | Статті/поради |

---

## Контекст домену

- **Plant** — рослина (зона, категорія, GPS, галерея фото, ряд/позиція)
- **Zone** — зона на ділянці (Теплиця, Верхній сад, Город)
- **Category** — тип рослини (Томати, Яблуні, Ягоди)
- **Log** — запис журналу (рослина, тип дії, фото, хімікати, AI-діагноз)
- **Schedule** — розклад обробок (фаза × категорія → рецепт + дати + статус)
- **Chemical** — хімікат/добриво (термін очікування, група)
- **ActionType**: `NOTE`, `FERTILIZE`, `SPRAY`, `REPLACE`, `ADD_PLANT`, `REMOVE_PLANT`

---

## Відомий технічний борг

- `GlobalScope` в auto-backup → замінити на `ProcessLifecycleOwner.lifecycleScope`
- `fallbackToDestructiveMigration(true)` → прибрати для production
- `exportSchema = false` → увімкнути для валідації міграцій
- Великі Screen-файли (28-30KB) → декомпозиція на менші Composable
- `ImageViewerDialog` → потребує pinch-to-zoom, swipe gestures
