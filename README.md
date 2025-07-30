## About DiscoVoadorNtk

DiscoVoadorNtk is an Android app written in **Kotlin** showcasing a complete group‑message alarm system with history and a modern UI.

- **Architecture**  
  - **MVVM** with **Kotlin Coroutines & StateFlow** for clear separation of UI, business logic and data  
  - Layered modules:  
    - `core` (models, utilities, services)  
    - `data` (Room database, DataStore, repositories)  
    - `ui` (Jetpack Compose screens & components)  
    - `notifications` (WorkManager workers, FCM helpers)

- **Dependency Injection**  
  - **Hilt** (Dagger‑Hilt) for providing ViewModels, repositories and utility classes  

- **UI & Navigation**  
  - **Jetpack Compose** for declarative UI  
  - **Navigation‑Compose** with sealed‐class routes (`SCNavigationRoutes`) for type‑safe navigation  
  - Custom components: `BaseText`, `BaseButton`, `BaseLazyColumn`, and a sealed‐class–driven `AppSnackbarHost`

- **Persistence & Sync**  
  - **Room** for local message history  
  - **Kotlin DataStore** for lightweight settings  
  - **ConnectivityObserver** (Flow) to monitor network changes  

- **Notifications & Scheduling**  
  - **WorkManager** (`CoroutineWorker`) to schedule and retry alarms  
  - **Firebase Cloud Messaging** (FCM) via `FirebaseManager` and `FcmNotificationSender`  
  - Custom notification channel (`AlarmChannel`)

- **Alarm Service**  
  - `AlarmService` to play sounds and trigger vibrations  
  - Reusable helper `stopAlarmService(context)` to stop the alarm from anywhere  


All code is modular and designed for easy maintenance.  

---

##  Download

**Signed APK** — [Download v2.4](https://github.com/naratake23/DiscoVoadorNtk/raw/main/app/release/discoVoadorNtk%20v2.4.apk)
