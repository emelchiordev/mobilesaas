# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# =============================================================================
# Retrofit 2
# =============================================================================
-keepattributes Signature
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# =============================================================================
# Gson / JSON (default Gson + GsonConverterFactory)
# =============================================================================
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Préserver les noms de champs annotés @SerializedName (mapping JSON stable en release)
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# =============================================================================
# OkHttp / Okio
# =============================================================================
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }

# =============================================================================
# Modèles API (data classes Kotlin + JSON) — packages réels du projet
# =============================================================================
# DTOs Retrofit / pull sync / auth / push / catalogue BAN…
-keep class re.melchior.saviomobile.data.remote.dto.** { *; }
# Interfaces API (sécurité supplémentaire avec R8 full mode)
-keep interface re.melchior.saviomobile.data.remote.api.** { *; }
-keep class re.melchior.saviomobile.data.remote.api.** { *; }

# =============================================================================
# Kotlin (data classes, coroutines utilisées par Retrofit)
# =============================================================================
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# =============================================================================
# Enums (souvent sérialisés en string ou référencés dans les DTOs)
# =============================================================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep enum re.melchior.saviomobile.data.remote.dto.** { *; }

# =============================================================================
# Dépendances diverses
# =============================================================================
# pdfbox-android — codecs JP2000 optionnels (artifact Gemalto non embarqué)
-dontwarn com.gemalto.jp2.**
