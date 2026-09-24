# Fazenda App — «Розумний Сад» 🌱

Android-додаток для управління дачею/фермою: каталог рослин, журнал догляду, дашборд, інтерактивна карта, бекап/відновлення.

## Функціонал

- **Каталог** — рослини з фото-галереєю, GPS, зонами, категоріями
- **Журнал** — хронологія дій (підживлення, обприскування, заміна) з CameraX-фото та AI-діагнозом
- **Дашборд** — план обробок, погодний віджет, перевірка оновлень
- **Карта** — OsmDroid з кастомними аватарами-маркерами
- **Бекап** — ZIP (БД + фото), авто-бекап при виході
- **Пошук** — глобальний по рослинам, логам, хімікатам
- **Розклад** — CRUD обробок з нотифікаціями

## Tech Stack

Kotlin • Jetpack Compose • Material 3 • Room • CameraX • Coil • OsmDroid • Coroutines/Flow

## Build & Release

### Налаштування середовища
JDK 17 зафіксовано в `gradle.properties`:
```properties
org.gradle.java.home=C:/Program Files/Android/Android Studio/jbr
```

### Debug
```powershell
.\gradlew assembleDebug
# → app\build\outputs\apk\debug\app-debug.apk
```

### Release (локально)
```powershell
.\build-release.ps1
```
Скрипт автоматично:
1. Збільшує `VERSION_CODE` + патч у `app/version.properties`
2. Читає пароль keystore з `.env` (`KEYSTORE_PASSWORD=...`)
3. Збирає підписаний APK → `dist\Fazenda-v{version}-Release.apk`
4. Верифікує підпис через `apksigner verify --print-certs`

### Встановлення на пристрій
```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "dist\Fazenda-v1.0.31-Release.apk"
```

### Публікація GitHub Release
```powershell
.\build-release.ps1 -Publish
# → git tag v{version} + push + GitHub Release з APK
```

### GitHub Actions (CI/CD)

Файл: `.github/workflows/release.yml`
Тригер: пуш тега `v*`

```bash
git tag v1.0.31
git push origin v1.0.31
```

Workflow:
1. JDK 17 + Android SDK
2. Декодує keystore з `KEYSTORE_BASE64`
3. Оновлює `version.properties`
4. Збирає та підписує APK
5. Створює GitHub Release з APK

### Секрети GitHub

| Secret | Значення |
|--------|----------|
| `KEYSTORE_BASE64` | Keystore у base64 |
| `KEYSTORE_PASSWORD` | Пароль keystore |

Закодувати:
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("app\fazenda-keystore.jks")) | Set-Clipboard
```

## Файли проєкту

| Файл | Призначення |
|------|-------------|
| `.ai/AGENTS.md` | Інструкції для AI-агентів |
| `tasks.md` | Черга задач та прогрес |
| `DOCUMENTATION.md` | Архітектура, БД, навігація, стек |
| `build-release.ps1` | Скрипт збірки + публікації |
| `.env` | Пароль keystore (в .gitignore) |
| `app/version.properties` | Версія (VERSION_CODE, VERSION_NAME) |
| `app/build.gradle.kts` | Android-конфіг, signingConfigs |
| `app/fazenda-keystore.jks` | Keystore (в .gitignore) |
| `.github/workflows/release.yml` | CI/CD |

## Keystore

- Файл: `app/fazenda-keystore.jks`
- Alias: `fazenda-key`
- Пароль: `.env` (локально) / GitHub Secrets (CI/CD)
- Gradle: `FAZENDA_STORE_PASSWORD` env var
