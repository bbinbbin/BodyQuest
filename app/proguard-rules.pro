# BodyQuest ProGuard Rules

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room entities - keep fields for SQLite mapping
-keep class com.bodyquest.app.data.local.entity.** { *; }

# Room DAOs
-keep class * extends androidx.room.RoomDatabase { *; }

# Hilt - keep generated components
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Kotlin serialization / enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Compose - keep composable functions metadata
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Coroutines
-dontwarn kotlinx.coroutines.**

# EncryptedSharedPreferences + Google Tink
-keep class androidx.security.crypto.** { *; }
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn com.google.crypto.tink.**

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }
-dontwarn com.google.firebase.**

# Credential Manager
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**
