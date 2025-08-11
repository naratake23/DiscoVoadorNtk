# DiscoVoadorNtk — Group Alarm & Push Notifications (Android)

## Overview
Android app (**Kotlin + Jetpack Compose**) that lets users create/join groups and broadcast alarm-style alerts to all members via **Firebase Cloud Messaging (FCM)**. It’s online/offline aware: if the sender is offline, the alert is queued with **WorkManager** and sent when connectivity returns. Alerts are designed to **vibrate and play an audible beep by default**, unless the recipient has **muted that sender for 24 hours**. The app keeps a **local message history**.

---

## Key Features
- **Groups:** create, join, leave groups; usernames are unique.
- **Broadcast alerts:** send an alarm message to all devices in groups the user belongs to.
- **Offline-first sending:** queued with WorkManager when offline; sent on reconnect.
- **24h per-member mute:** locally mute specific senders per group for 24 hours.
- **Alert behavior:** default vibration + periodic beep (foreground service & full-screen notification).
- **Message history:** local log (Room) of sent/received alerts per group.

---

## App Flow

### Sending
1. User composes an alert in **Group Screen** and taps to send.
2. **If online:**
  - ViewModel gets `(memberId, token)` pairs from Firestore and sends one-by-one via **FcmNotificationSender** (FCM HTTP v1).
  - If FCM returns **UNREGISTERED (404)**, that token is **removed** from Firestore and the loop continues.
3. **If offline:**
  - Enqueues **SendAlarmNotificationWorker** with the alert payload.
  - Worker checks connectivity and sends later (same per-token cleanup on **UNREGISTERED**).

### Receiving
1. FCM delivers a **data message** to `MyFirebaseMessagingService.onMessageReceived`.
2. The service checks local mute state for the sender in the target group (Room via `MutedUserLocalRepository`).
3. It starts a **foreground `AlarmService`** with extras: sender name, group name, message body, timestamp, and an `isMuted` flag.
4. `AlarmService` posts a **high-priority, full-screen notification** (`CATEGORY_CALL`, `PRIORITY_MAX`, full-screen intent to `AlarmActivity`), and:
  - **If not muted:** triggers vibration (`AlarmPlayVibration`) and periodic beeps (`AlarmPlayBeep`).
  - **If muted (24h window active):** skips sound/vibration but still shows the UI/notification as implemented.
5. The alert is also persisted to **local history (Room)**.
6. User can stop the alarm from **`AlarmActivity`** (calls `stopAlarmService`).

---

## Screens & Components

### Main Shell
- **`ScreenMain` (Compose):** `Scaffold` + global `SnackbarHost`.
- **Navigation:** `MyNavGraph` with typed routes in `SCNavigationRoutes`.

### Entry Screen
(`ScreenEntry`, `ViewModelEntryS1`, `EntryUiState`, `EntryUiEvent`)
- **Username setup:** dialog to insert a unique username; checks availability in Firestore (`userNames` collection).
  - Loading overlay blocks input during server round-trip.
  - Errors mapped via `InputErrorEntry` + snackbars.
- **Group list (local):** observes joined groups from Room and displays them.
- **Actions:**
  - Create group (name + password) → Firestore `groups` + `userGroups`.
  - Join group (name + password) → adds current device to group’s `members` and to `userGroups`.
  - Leave group → removes membership and updates local Room.
  - Select group → navigates to Group Screen.
  - Stop alarm → calls `stopAlarmService`.
- **Connectivity-aware:** shows/uses online state; initial bootstrap resolves username from DataStore or Firestore.

### Group Screen
(`ScreenGroup`, `ViewModelGroupS1`, `GroupUiState`, `GroupUiEvent`)
- **Header:** group name, actions (e.g., go to history).
- **Members:** list of `MemberUi` with per-member mute/unmute (24h) stored locally (Room).
  - The ViewModel periodically **recalculates** mute flags to expire them cleanly.
- **Compose & Send alert:** text field + send button.
  - **Online:** direct send via FCM HTTP v1 (per-token), cleaning **UNREGISTERED** tokens.
  - **Offline:** enqueues `SendAlarmNotificationWorker` with backoff/constraints.
- **Live messages:** observes Firestore `messages` subcollection for this group and **persists each message to Room**.
- **Navigate to History:** toggles `navigateHistoryScreen` and routes via `NavGraph`.

### History Screen
(`ScreenHistory`, `ViewModelHistoryS1`, `HistoryUiState`)
- **Data source:** Room (`AlarmMessageLocalRepository.observeByGroupId`).
- **UI:** reverse-chronological list of `AlarmMessageInfo` with sender, body, and timestamp.
- **Navigation:** back to Group Screen.

### Alarm Activity
(`AlarmActivity`)
- Full-screen alert UI shown from the notification (full-screen intent).
- Displays sender name, group name, message body, timestamp, and a **Stop** button that stops the alarm service.

---

## Architecture & Patterns
- **Layers:**  
  UI (Compose) → ViewModel (MVVM) → Managers/Repositories (Firestore + Room + DataStore)
- **State & Events:** immutable `UiState` objects + sealed events (`EntryUiEvent`, `GroupUiEvent`) for one-way data flow.
- **Concurrency:** Kotlin Coroutines (`viewModelScope`, `Dispatchers.IO`/`Main`), structured concurrency.
- **Background work:** WorkManager for reliable, constraint-aware queued sends.

---

## Notifications & Alarm Behavior
- **Channel:** `AlarmChannel.createChannel(...)` created on first run / permission grant. Full-screen category via notification setup in `AlarmService`.
- **Foreground service:** `AlarmService` drives the experience (beep + vibration) regardless of the channel’s default sound setting.
- **Android 13+:** runtime permission for `POST_NOTIFICATIONS` requested in `MainActivity`.
- **Always vibrate & beep (when not muted):** handled by `AlarmPlayVibration` and `AlarmPlayBeep` from the service loop; not just by channel defaults.

---

## Data & Persistence

### Local (Room)
- **DB:** `AppDatabase`
  - `groups` → `GroupEntity(groupId, groupName)`
  - `alarm_messages` → `AlarmMessageEntity(id, groupId, senderId, senderName, messageBody, timestamp)`
  - `muted_users` → `MutedUserEntity(groupId, userId, mutedUntil)` (composite PK: `groupId`,`userId`)
- **DAOs:** `GroupDao`, `AlarmMessageDao`, `MutedUserDao`
- **Repositories:**
  - `GroupLocalRepository(Impl)` — join/leave storage & observe all groups
  - `AlarmMessageLocalRepository(Impl)` — insert/observe messages by group
  - `MutedUserLocalRepository(Impl)` — mute/unmute/expire and query muted members per group

### Preferences (DataStore)
- `DataStoreRepository(Impl)` — small key/value (e.g., local username).

### Cloud (Firestore)
- **`groups/{groupId}`** document:
  - fields: `groupName`, `passwordHash`, `members: Map<deviceId, userName>`
  - subcollection **`messages/{messageId}`** with `AlarmMessageInfo` (id, senderId, senderName, messageBody, timestamp)
- **`devices/{deviceId}`** document:
  - fields: `fcm_token` (current token for that device)
- **`userGroups/{deviceId}`** document:
  - field: `groups: [ { groupId, groupName }, ... ]` (array for quick listing)
- **`userNames/{userName}`** document:
  - field: `deviceId` (enforces unique usernames)

---

## Dependency Injection (Hilt)
- **App:** `MyApplication` annotated with `@HiltAndroidApp`, provides `HiltWorkerFactory` to WorkManager.
- **Modules:**
  - `DatabaseModule` → Room (DB + DAOs) and binds local repos (`GroupLocalRepository`, `AlarmMessageLocalRepository`, `MutedUserLocalRepository`).
  - `DataStoreModule` → `DataStore<Preferences>` and `DataStoreRepository`.
  - `GeneralModule` → `ConnectivityObserver`, `FcmHttpClient`, `FcmNotificationSender`, `FirebaseManager`, and `@DeviceIdQualifier String` (via `Settings.Secure.ANDROID_ID`).

---

## Tech Stack
Kotlin (JVM 17), Jetpack Compose (Material 3), Navigation-Compose, Hilt, WorkManager, Firebase (Auth, Firestore, Messaging via BoM), Google Auth HTTP client (for FCM HTTP v1), Room, DataStore, Coroutines, Coil.

---

##  Download

**Signed APK** — [Download v2.10](https://github.com/naratake23/DiscoVoadorNtk/raw/main/app/release/discoVoadorNtk_v2.10.apk)
