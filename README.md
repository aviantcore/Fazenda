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

### Основний спосіб — GitHub Actions (CI/CD)

Збірка відбувається автоматично при пуші тега `v*`:

```bash
git tag v1.0.37
git push origin v1.0.37
```

GitHub Actions автоматично:
1. Використовує JDK 17 та Android SDK
2. Декодує keystore з `KEYSTORE_BASE64`
3. Оновлює `version.properties`
4. Збирає та підписає APK
5. Створює GitHub Release з APK

### Секрети GitHub (обов'язкові)

| Secret | Значення |
|--------|----------|
| `KEYSTORE_BASE64` | Keystore у base64 |
| `KEYSTORE_PASSWORD` | Пароль keystore |

Налаштування: `GitHub → Repo → Settings → Secrets and variables → Actions`

### Debug-збірка (лише для перевірки коду)

```powershell
.\gradlew assembleDebug
# → app\build\outputs\apk\debug\app-debug.apk
```

### Встановлення на пристрій

```powershell
adb install -r "Fazenda-v1.0.36.apk"
```

## Файли проєкту

| Файл | Призначення |
|------|-------------|
| `.ai/AGENTS.md` | Інструкції для AI-агентів |
| `tasks.md` | Черга задач та прогрес |
| `DOCUMENTATION.md` | Архітектура, БД, навігація, стек |
| `.github/workflows/release.yml` | CI/CD |
| `app/version.properties` | Версія (VERSION_CODE, VERSION_NAME) |
| `app/build.gradle.kts` | Android-конфіг, signingConfigs |

## Keystore

- Alias: `fazenda-key`
- Зберігається в GitHub Secrets (base64)
- Пароль: `KEYSTORE_PASSWORD` secret
- **dist/** — виключно локальні артефакти збірки, не комітяться (додано до `.gitignore`)
