// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // kotlin.android plugin removed — Kotlin support is built into AGP 9.0+
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt) apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}

buildscript{
    dependencies {
        classpath(libs.androidx.navigation.safe.args.gradle.plugin)
    }
}
