plugins {
    alias(libs.plugins.android.application)
    // kotlin.android plugin removed — Kotlin support is built into AGP 9.0+
    alias(libs.plugins.hilt)
    id("com.google.devtools.ksp")
}

apply(
    plugin = "androidx.navigation.safeargs.kotlin"
)

android {
    namespace = "com.rajatt7z.fitbykit"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.rajatt7z.fitbykit"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 2          // Increment before each Play Store upload
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    // ── Release signing ────────────────────────────────────────────────────────
    // To configure release signing:
    //   1. Generate a keystore: keytool -genkey -v -keystore fitbykit-release.jks
    //      -alias fitbykit -keyalg RSA -keysize 2048 -validity 10000
    //   2. Add these keys to local.properties (never commit to git):
    //      KEYSTORE_FILE=../fitbykit-release.jks
    //      KEY_ALIAS=fitbykit
    //      KEY_PASSWORD=your_key_password
    //      STORE_PASSWORD=your_store_password
    //   3. Uncomment the signingConfigs block below and update the release build.
    //
    // signingConfigs {
    //     create("release") {
    //         val props = java.util.Properties()
    //         props.load(rootProject.file("local.properties").inputStream())
    //         storeFile = file(props["KEYSTORE_FILE"] as String)
    //         storePassword = props["STORE_PASSWORD"] as String
    //         keyAlias = props["KEY_ALIAS"] as String
    //         keyPassword = props["KEY_PASSWORD"] as String
    //     }
    // }

    buildTypes {
        release {
            // R8 enabled: dead-code removal + obfuscation + resource shrinking
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // TODO: Replace with release signing once keystore is configured (see above)
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":workout_api"))
    implementation(libs.mpandroidchart)
    implementation(libs.osmdroid.android)
    implementation(libs.play.services.location)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.appcompat)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.core)
    implementation(libs.glide)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.circleimageview)
    implementation(libs.material.v1140alpha01)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // LocalBroadcastManager: secure in-app broadcast for step count updates
    implementation(libs.androidx.localbroadcastmanager)
}
