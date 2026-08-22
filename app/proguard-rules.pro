# GeoHunt ProGuard Rules - Optimized for R8 High Efficiency

# Proteger clases de datos marcadas con @Keep (uso quirúrgico para Firestore)
-keep @androidx.annotation.Keep class *
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler {
    public <init>(...);
}

# ViewModels (solo protegemos el constructor para que ViewModelProvider funcione)
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Compose rules - Usualmente manejadas por el plugin, añadimos protecciones base
-keep class androidx.compose.ui.platform.** { *; }
-dontwarn androidx.compose.ui.platform.**
