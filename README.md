# Pet Grooming Manager

Android app for managing pet grooming appointments, built with Kotlin and Jetpack Compose.

## Development Environment (VS Code)

### Prerequisites

1. **JDK 17+** - Download from [Adoptium](https://adoptium.net/)
2. **Android SDK** - Install via [Android Studio](https://developer.android.com/studio) (one-time setup) or [Command Line Tools](https://developer.android.com/studio#command-tools)
3. **Gradle 8.4+** - Will be auto-downloaded via wrapper

### VS Code Extensions

Install these extensions for the best experience:

| Extension | Purpose |
|-----------|---------|
| [Kotlin](https://marketplace.visualstudio.com/items?itemName=mathiasfrohlich.Kotlin) | Syntax highlighting, snippets |
| [Gradle for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-gradle) | Build tasks, dependency management |
| [Android iOS Emulator](https://marketplace.visualstudio.com/items?itemName=nicediemasmichel.vscode-android-ios-emulator) | Launch emulators from VS Code |

### Environment Variables

Add these to your system environment (Windows):

```powershell
# Set ANDROID_HOME (adjust path if needed)
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")

# Add to PATH
$path = [Environment]::GetEnvironmentVariable("PATH", "User")
$newPaths = @(
    "$env:LOCALAPPDATA\Android\Sdk\platform-tools",
    "$env:LOCALAPPDATA\Android\Sdk\emulator",
    "$env:LOCALAPPDATA\Android\Sdk\cmdline-tools\latest\bin"
)
[Environment]::SetEnvironmentVariable("PATH", "$path;$($newPaths -join ';')", "User")
```

Restart VS Code after setting environment variables.

---

## Build Commands

Run these from the `PetGroomingManager` directory:

```powershell
# Navigate to project
cd PetGroomingManager

# Build debug APK
.\gradlew assembleDebug

# Build release APK
.\gradlew assembleRelease

# Install on connected device/emulator
.\gradlew installDebug

# Clean build
.\gradlew clean

# Run unit tests
.\gradlew test

# Run instrumented tests (requires device/emulator)
.\gradlew connectedAndroidTest

# Check for dependency updates
.\gradlew dependencyUpdates
```

---

## Device/Emulator Management

```powershell
# List connected devices
adb devices

# List available emulators
emulator -list-avds

# Start an emulator
emulator -avd Pixel_6_API_34

# View device logs (filtered to app)
adb logcat -s "PetGrooming"

# View all logs
adb logcat

# Install APK manually
adb install app\build\outputs\apk\debug\app-debug.apk

# Uninstall app
adb uninstall com.petgrooming.manager
```

---

## Project Structure

```
PetGroomingManager/
├── app/
│   ├── build.gradle.kts          # App module config
│   ├── proguard-rules.pro        # Release obfuscation rules
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/petgrooming/manager/
│       │   ├── PetGroomingApp.kt           # Hilt Application
│       │   ├── MainActivity.kt             # Main entry point
│       │   ├── data/
│       │   │   └── local/                  # Room database
│       │   │       ├── dao/                # Data Access Objects
│       │   │       ├── entity/             # Database entities
│       │   │       ├── Converters.kt
│       │   │       └── PetGroomingDatabase.kt
│       │   ├── di/                         # Hilt modules
│       │   ├── domain/                     # Business logic (to add)
│       │   │   ├── model/                  # Domain models
│       │   │   ├── repository/             # Repository interfaces
│       │   │   └── usecase/                # Use cases
│       │   └── ui/
│       │       ├── theme/                  # Material 3 theme
│       │       ├── navigation/             # Nav graph (to add)
│       │       └── feature/                # Feature screens (to add)
│       └── res/
│           ├── values/strings.xml          # English strings
│           ├── values-th/strings.xml       # Thai strings
│           └── xml/                        # Backup rules
├── gradle/
│   └── libs.versions.toml        # Version catalog
├── build.gradle.kts              # Root build config
├── settings.gradle.kts           # Project settings
└── gradle.properties             # Gradle settings
```

---

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt 2.51.1 |
| Database | Room 2.6.1 |
| Async | Kotlin Coroutines + Flow |
| Images | Coil 2.6.0 |
| Background | WorkManager 2.9.0 |
| Navigation | Navigation Compose 2.7.7 |

---

## Features (Per Spec)

- [ ] **Booking Management** - CRUD, status flow, calendar views
- [ ] **Pet Records** - Pet profiles, owner info, before/after photos
- [ ] **Push Notifications** - Appointment reminders, ready alerts
- [ ] **Rebooking Reminders** - Configurable intervals, due tracking
- [ ] **Google Drive Sync** - Backup/restore functionality

---

## Debugging Tips

### View Compose Preview
Since VS Code doesn't have Compose Preview, use these alternatives:
1. **Hot reload on device** - Changes rebuild quickly with `installDebug`
2. **Interactive Mode** - Add `@Preview` annotations and run preview tests

### Common Issues

**"SDK location not found"**
- Create `local.properties` in project root:
  ```properties
  sdk.dir=C\:\\Users\\Ken Bywater\\AppData\\Local\\Android\\Sdk
  ```

**"Gradle sync failed"**
- Run `.\gradlew --refresh-dependencies`
- Check JDK version: `java -version` (needs 17+)

**"Device not found"**
- Enable USB debugging on device
- Run `adb kill-server && adb start-server`

---

## Quick Start

```powershell
# 1. Clone and navigate
cd "C:\Users\Ken Bywater\Documents\Grooming\PetGroomingManager"

# 2. Create local.properties (one-time)
"sdk.dir=C\:\\Users\\Ken Bywater\\AppData\\Local\\Android\\Sdk" | Out-File -Encoding ASCII local.properties

# 3. Build
.\gradlew assembleDebug

# 4. Start emulator (in separate terminal)
emulator -avd Pixel_6_API_34

# 5. Install and run
.\gradlew installDebug

# 6. View logs
adb logcat -s "PetGrooming"
```

---

## Using Copilot Agents

This project includes custom VS Code agents:

- **`@pet-grooming-dev`** - Build features with domain knowledge
- **`@spec-reviewer`** - Validate implementation against spec
- **`@new-feature`** - Scaffold new features (prompt template)

Example: Type `@pet-grooming-dev implement the booking list screen` in Copilot Chat.
