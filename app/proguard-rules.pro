# === App-Specific ProGuard Rules ===

# Keep data classes and model classes
-keep class com.example.data.** { *; }
-keep class com.example.ui.** { *; }

# Keep ViewModel classes
-keepclasseswithmembernames class androidx.lifecycle.ViewModel { *; }
-keepclasseswithmembernames class com.example.ui.ShoppingViewModel { *; }

# Keep Room Database
-keep class androidx.room.Room { *; }
-keep class androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclasseswithmembernames interface com.example.data.ShoppingDao { *; }

# Keep Moshi JSON serialization
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <fields>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }

# Keep Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Firebase AI
-keep class com.google.firebase.** { *; }

# Keep Jetpack Compose
-keep class androidx.compose.** { *; }

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Obfuscate package names
-repackageclasses ''
-allowaccessmodification
