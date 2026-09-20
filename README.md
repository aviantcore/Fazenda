# Fazenda App

Android application for farm management (зони, каталог, журнал, дашборд, мапа).

## Build & Release

### Налаштування середовища (Java/JDK)
Для збірки використовується JDK 17 або JDK 21 (наприклад, JBR з Android Studio).
Шлях до JDK автоматично зафіксовано в `gradle.properties`:
```properties
org.gradle.java.home=C:/Program Files/Android/Android Studio/jbr
```
Тому будь-які Gradle-команди (`.\gradlew ...`) запускаються без ручного встановлення `JAVA_HOME`.

### Швидка Debug-збірка
```powershell
.\gradlew assembleDebug
```
Готовий APK зберігається у:
`app\build\outputs\apk\debug\app-debug.apk`

### Локальна Release-збірка (з підписом)
```powershell
.\build-release.ps1
```
Що робить скрипт:
1. Читає `app/version.properties`, автоматично збільшує `VERSION_CODE` та патч-версію (напр. 1.0.29 → 1.0.30)
2. Читає пароль keystore з `.env` (ключ `KEYSTORE_PASSWORD`)
3. Встановлює змінну `FAZENDA_STORE_PASSWORD` — Gradle автоматично підписує APK релізним ключем
4. Запускає `gradlew assembleRelease`
5. Копіює підписаний APK у каталог `dist/`:
   `dist\Fazenda-v{version}-Release.apk`
6. Верифікує підпис через `apksigner verify --print-certs`

### Встановлення на пристрій через ADB:
```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "dist\Fazenda-v{version}-Release.apk"
```

### Публікація релізу

```powershell
.\build-release.ps1 -Publish
```

Додатково:
1. Створює git-тег `v{version}`
2. Пушить тег на GitHub
3. Створює GitHub Release з APK як ассет

### GitHub Actions (CI/CD)

Файл: `.github/workflows/release.yml`

Тригер: пуш тега `v*`

```bash
git tag v1.0.9
git push origin v1.0.9
```

Що робить workflow:
1. Виставляє JDK 17 + Android SDK
2. Декодує keystore із секрету `KEYSTORE_BASE64`
3. Парсить версію з тега
4. Оновлює `version.properties`
5. Будує та підписує APK (через `FAZENDA_STORE_PASSWORD` із секрету `KEYSTORE_PASSWORD`)
6. Створює GitHub Release з APK

### Необхідні секрети GitHub

Для роботи GitHub Actions додай у `Settings → Secrets and variables → Actions`:

| Secret | Значення |
|--------|----------|
| `KEYSTORE_BASE64` | Keystore-файл у base64 |
| `KEYSTORE_PASSWORD` | Пароль до keystore |

Закодувати keystore в base64:
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("app\fazenda-keystore.jks")) | Set-Clipboard
```

## Файли проєкту

| Файл | Призначення |
|------|-------------|
| `build-release.ps1` | Скрипт збірки + публікації |
| `.env` | Пароль keystore (`KEYSTORE_PASSWORD=...`) — в .gitignore |
| `app/version.properties` | Поточна версія (`VERSION_CODE`, `VERSION_NAME`) |
| `app/build.gradle.kts` | Android-конфіг, читає `FAZENDA_STORE_PASSWORD` для підпису |
| `app/fazenda-keystore.jks` | Keystore-файл — в .gitignore |
| `.github/workflows/release.yml` | GitHub Actions CI/CD |

## Keystore

- Файл: `app/fazenda-keystore.jks`
- Alias: `fazenda-key`
- Пароль зберігається в `.env` (локально) та GitHub Secrets (для CI/CD)
- Gradle бере пароль зі змінної `FAZENDA_STORE_PASSWORD`
