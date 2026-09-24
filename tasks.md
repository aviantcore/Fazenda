# Fazenda App — План розробки та прогрес

> Джерело правди для черги задач. Читати разом з `.ai/AGENTS.md` та `DOCUMENTATION.md`.
> Версія: 1.0.32 (VERSION_CODE = 45), вересень 2026.

---

## ✅ Реалізовано (v1.0.0 → v1.0.32)

### Core
- [x] Single Activity + Navigation Compose (3 bottom tabs)
- [x] Room DB v10 (8 entities, 7 DAO, 7 repositories)
- [x] Manual DI через FazendaApplication singleton
- [x] CSV seeding при першому запуску
- [x] Dark/Light тема (Material 3)

### Каталог рослин
- [x] Список з пошуком + фільтр chips за категоріями
- [x] Деталі рослини (галерея, GPS-карта, коментар, історія дій)
- [x] Multi-photo support (PlantPhotoEntity, горизонтальна галерея)
- [x] Додавання/редагування рослини (CameraX + галерея)
- [x] Long-press → призначити головне фото / видалити фото

### Журнал
- [x] Хронологічний список логів з кольоровими тегами
- [x] 6 типів дій: NOTE, FERTILIZE, SPRAY, REPLACE, ADD_PLANT, REMOVE_PLANT
- [x] CameraX для фото при створенні запису
- [x] AI-діагноз (deep-link Gemini + копіювання)
- [x] Вибір хімікатів для запису (M:M через log_chemicals)

### Дашборд
- [x] План обробок (ScheduleEntity → категорія × фаза × рецепт)
- [x] Перевірка оновлень (UpdateService → GitHub Releases)
- [x] Погодний віджет (Open-Meteo API + агро-рекомендації)

### Карта
- [x] OsmDroid з кастомними круглими маркерами-аватарами
- [x] Bottom Sheet при тапі (замість InfoWindow)
- [x] GPS-кнопка для геолокації

### Інфраструктура
- [x] Бекап/відновлення (ZIP: БД + фото)
- [x] Авто-бекап при виході (SAF DocumentTree)
- [x] Онбординг (3 кроки) → BackupSetup → Dashboard
- [x] build-release.ps1 (авто-версія, підпис, dist/)
- [x] GitHub Actions CI/CD (release.yml)
- [x] Нотифікації розкладу (ScheduleAlarmManager + Receiver)
- [x] Мережевий стан (NetworkConnectivityObserver)
- [x] Глобальний пошук (SearchScreen + SearchViewModel)
- [x] База знань (KnowledgeBaseScreen)
- [x] Multi FAB Speed Dial
- [x] ImageViewerDialog (повноекранний перегляд фото)
- [x] SearchableDropdown

---

## 🔲 Черга задач

### Phase 1: Foundation & Infrastructure
- [x] **Unified Search Engine**: розширити SearchViewModel — покриття Catalog + Journal + Chemicals з єдиним UI
- [x] **Notification System**: дописати повний scheduling flow — вибір дати/часу нагадування для ScheduleEntity, повторювальні alarm
- [x] **Shared UI Components**: виокремити переиспользовані компоненти для status indicators (Planned/In Progress/Completed) та уніфікованих form fields
- [x] **Connectivity Status**: додати візуальні індикатори стану підключення до Weather API та Wikipedia API (online/offline badge)

### Phase 2: Interaction Optimization
- [x] **Quick Actions FAB**: додати FAB Speed Dial на JournalScreen (вже є MultiFAB компонент — інтегрувати)
- [x] **Contextual Defaults**: запам'ятовувати останню вибрану зону/ділянку для швидкого заповнення форм
- [x] **Context Menus**: додати long-press/three-dot меню до всіх списків (частково є в каталозі — розширити на журнал та розклад)

### Phase 3: Visual & UX Polish
- [x] **Dashboard Analytics**: інтегрувати графіки — витрати хімікатів по місяцях, тренди росту (Compose charts або Canvas)
- [x] **Enhanced ImageViewerDialog**: додати pinch-to-zoom, swipe gestures, auto-reset zoom, clear page indicators (поточний — базовий pager)
- [x] **Status Highlighting**: кольорові статуси (🟢 Completed / 🟡 In Progress / ⚪ Planned) у всіх списках розкладу та журналу
- [x] **Progress Indicators**: skeleton screens або shimmer для довгих операцій (завантаження фото, мережеві запити)

### Phase 4: User Experience & Onboarding
- [ ] **Enhanced Onboarding**: розширити інтерктивний туторіал (tooltip hints, highlight UI elements)

### Phase 5: Verification & Testing
- [ ] Unit tests: SearchViewModel, ScheduleAlarmManager, BackupService
- [ ] UI tests: navigation flows (Onboarding → BackupSetup → Dashboard → Catalog → Details)
- [ ] Integration tests: NetworkConnectivityObserver, WeatherService, UpdateService

---

## 📝 Відомі проблеми та технічний борг

- `GlobalScope` в `FazendaApplication.performAutoBackup()` — замінити на `ProcessLifecycleOwner.lifecycleScope`
- `fallbackToDestructiveMigration(true)` в AppDatabase — видалити для production, писати повні міграції
- `exportSchema = false` — ввімкнути для автоматичної валідації міграцій
- `ImageViewerDialog` — 11.8KB, потрібен рефакторинг (pinch-to-zoom, gesture handling)
- Великі Screen-файли (EditPlantScreen 29.7KB, CreateLogScreen 29KB, PlantDetailsScreen 28KB) — потрібна декомпозиція на менші Composable

---

## 📊 Статистика коду

| Метрика | Значення |
|---------|----------|
| Screen файлів | 16 |
| ViewModel файлів | 12 |
| Entity файлів | 10 |
| DAO файлів | 7 |
| Repository файлів | 7 |
| Service файлів | 9 |
| Shared component файлів | 4 |
| Room DB version | 10 |
| Room migrations | 2 (8→9, 9→10) |
| App version | 1.0.32 (code 45) |
| Min SDK | 26 |
| Target SDK | 35 |
