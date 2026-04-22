# Add project specific ProGuard rules here.

# Preserve stack trace info for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Kotlin ──────────────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# ── Retrofit + OkHttp ────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keepattributes Signature
-keepattributes Exceptions

# ── Gson (used by Retrofit GsonConverterFactory) ─────────────────────────────
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
# Keep all WGER / MealDB model classes (they are serialized/deserialized by Gson)
-keep class com.rajatt7z.workout_api.** { *; }

# ── Room ─────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface *
-dontwarn androidx.room.**

# ── Hilt / Dagger ────────────────────────────────────────────────────────────
#noinspection ExpensiveKeepRuleInspection
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclassmembers class * {
    @javax.inject.Inject <fields>;
    @javax.inject.Inject <init>(...);
}
-dontwarn dagger.**

# ── Glide ────────────────────────────────────────────────────────────────────
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# ── OSMDroid ─────────────────────────────────────────────────────────────────
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# ── AndroidX Navigation SafeArgs ─────────────────────────────────────────────
-keepnames class * implements android.os.Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# ── MPAndroidChart ───────────────────────────────────────────────────────────
-keep class com.github.mikephil.charting.** { *; }

# ── CircleImageView ───────────────────────────────────────────────────────────
-keep class de.hdodenhof.circleimageview.** { *; }

# ── Google Play Services / Location ──────────────────────────────────────────
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ── Material Components ───────────────────────────────────────────────────────
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ── ViewBinding (keep generated binding classes) ─────────────────────────────
-keep class com.rajatt7z.fitbykit.databinding.** { *; }

# ── App model / utility classes ───────────────────────────────────────────────
-keep class com.rajatt7z.fitbykit.Utils.** { *; }
-keep class com.rajatt7z.fitbykit.database.** { *; }

# ── Coroutines ────────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ── NewPipe / NanoJson (leftover from previous build, kept safe) ──────────────
-keep class org.schabi.newpipe.** { *; }
-keep class com.grack.nanojson.** { *; }
-dontwarn org.schabi.newpipe.**
-dontwarn com.grack.nanojson.**
