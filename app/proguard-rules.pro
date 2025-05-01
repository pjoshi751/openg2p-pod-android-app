# Add project specific ProGuard rules here.
# You can control the optimization options in the build.gradle file.

# Retain generic signatures for Gson serialization/deserialization
-keepattributes Signature

# Retain specific classes used by Gson
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken

# Retain fields used by Gson
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Retrofit interfaces and their methods
-keep interface retrofit2.Call
-keep interface retrofit2.Callback
-keep interface com.openg2p.pod.data.ApiService { *; }

# Keep OkHttp classes
-keep class okhttp3.** { *; }
-keep interface okio.** { *; }

# Keep Kotlin Coroutines internal classes
-keepnames class kotlinx.coroutines.internal.** { *; }

# Keep Jetpack Compose specifics
-keepclassmembers class * { @androidx.compose.runtime.Composable <methods>; }
-keepclassmembers class * implements androidx.compose.runtime.Composer { *; }
-keepclassmembers class * implements androidx.compose.ui.tooling.preview.PreviewParameterProvider { *; }

