# ProGuard & R8 Optimization Rules for DYNIMETIZE ZX

# Preserve Line Numbers for Crash Reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# Jetpack Compose Rules
-keepclassmembers class * extends androidx.compose.ui.Modifier { *; }
-dontwarn androidx.compose.**

# Room Database Rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Moshi & Retrofit Networking
-keepclassmembers class * {
    @com.squareup.moshi.* <methods>;
    @com.squareup.moshi.* <fields>;
}
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Data Models & Config
-keep class com.example.data.** { *; }
-keepclassmembers class com.example.data.** { *; }

# Shizuku Privileged API Bridge
-dontwarn moe.shizuku.**
-keep class moe.shizuku.** { *; }
