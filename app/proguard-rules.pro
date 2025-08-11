# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


##############################################
# --------- Diagnóstico inicial (opcional enquanto testa; depois comente) ---------
# Mantém arquivo de mapeamento legível nos logs
-keepattributes SourceFile,LineNumberTable

# --------- Hilt / Dagger ---------
# Em geral as libs já têm consumer rules, mas garantimos:
-dontwarn javax.inject.**
-dontwarn dagger.**
-keep class dagger.hilt.** { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }

# --------- Firebase (mensageria/analytics/etc.) ---------
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }

# Mantém seu FirebaseMessagingService e o nome de classe (manifest aponta por nome)
-keepnames class com.liberty.discovoadorntk.notifications.fcm.MyFirebaseMessagingService
-keep class com.liberty.discovoadorntk.notifications.fcm.MyFirebaseMessagingService { *; }

# --------- WorkManager + HiltWorker ---------
# Workers são instanciados por nome/assinatura
-keep class * extends androidx.work.ListenableWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Hilt Worker e AssistedInject gerados
-keep class androidx.hilt.work.HiltWorkerFactory { *; }
-keep class dagger.assisted.** { *; }
-dontwarn dagger.assisted.**

# --------- Firestore toObject(...) (mapeamento por reflexão) ---------
# Mantenha os seus modelos usados com Firestore
-keep class com.liberty.discovoadorntk.core.data.** { *; }

# --------- Room (geralmente ok; só silenciar warns) ---------
-dontwarn androidx.room.**
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# --------- Coroutines / Kotlin std (silenciar avisos comuns) ---------
-dontwarn kotlinx.coroutines.**
-dontwarn kotlin.**

# --------- OkHttp/conscrypt (você já tinha) ---------
-dontwarn okhttp3.internal.platform.**
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# Google HTTP Client + Auth (necessário p/ GoogleCredentials.fromStream)
-keep class com.google.api.client.json.** { *; }
-keep class com.google.api.client.util.** { *; }
-keep class com.google.api.client.googleapis.** { *; }
-keep class com.google.auth.** { *; }

-dontwarn com.google.api.client.**
-dontwarn com.google.auth.**



#-dontobfuscate
##############################################
