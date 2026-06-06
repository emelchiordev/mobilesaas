import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
}

apply(plugin = "io.objectbox")

/** HTTP(S) de base pour Retrofit — ajoute « / » final si absent. */
fun urlWithTrailingSlash(raw: String): String {
    val t = raw.trim().trimEnd('/')
    return "$t/"
}

val localProperties =
    Properties().apply {
        rootProject.file("local.properties").takeIf { it.exists() }?.reader()?.use { load(it) }
    }

/** Surcharge locale : `DEV_BASE_URL=http://192.168.1.XX:3000` dans local.properties (gitignored). */
val debugBaseUrl =
    urlWithTrailingSlash(
        localProperties.getProperty("DEV_BASE_URL")?.trim()?.takeIf { it.isNotEmpty() }
            ?: "http://192.168.1.69:3000",
    )

/** Optionnel : `DEV_BAN_BASE_URL=http://192.168.1.XX:3001` — défaut émulateur :3001 */
val debugBanBaseUrl =
    urlWithTrailingSlash(
        localProperties.getProperty("DEV_BAN_BASE_URL")?.trim()?.takeIf { it.isNotEmpty() }
            ?: "http://192.168.1.69:3001",
    )

val sentryDsn =
    localProperties.getProperty("SENTRY_DSN")?.trim()?.takeIf { it.isNotEmpty() } ?: ""

/** Clé x-api-key pour GET sync/catalog — voir local.properties.example */
val banApiKeyPlaceholder = "change-me-with-a-strong-random-key"
val banApiKey =
    localProperties.getProperty("BAN_API_KEY")?.trim()?.takeIf { it.isNotEmpty() }
        ?: System.getenv("BAN_API_KEY")?.trim()?.takeIf { it.isNotEmpty() }
        ?: banApiKeyPlaceholder

android {
    namespace = "re.melchior.saviomobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "re.melchior.saviomobile"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BAN_API_KEY", "\"$banApiKey\"")
        buildConfigField("String", "SENTRY_DSN", "\"$sentryDsn\"")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
            // Émulateur : 10.0.2.2 — tablette LAN : surcharger via local.properties (DEV_BASE_URL)
            buildConfigField("String", "BASE_URL", "\"$debugBaseUrl\"")
            buildConfigField("String", "BAN_API_BASE_URL", "\"$debugBanBaseUrl\"")
        }
        release {
            isMinifyEnabled = true
            buildConfigField("String", "BASE_URL", "\"https://api.savio.re/\"")
            // Aligné front prod (ban.*) — ajuster si votre API catalogue est ailleurs
            buildConfigField("String", "BAN_API_BASE_URL", "\"https://ban.melchior.re/\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        // Workaround crash lint AGP + Kotlin UAST (NonNullableMutableLiveDataDetector)
        disable += "NullSafeMutableLiveData"
    }
}

/** Release : refuse un APK sans vraie clé BAN (évite HTTP 401 silencieux en prod). */
fun requireBanApiKeyForRelease(taskName: String) {
    if (banApiKey == banApiKeyPlaceholder) {
        throw GradleException(
            """
            |$taskName : BAN_API_KEY manquante ou placeholder.
            |Ajoutez dans local.properties (racine du projet, non versionné) :
            |  BAN_API_KEY=<même valeur que API_KEY sur https://ban.melchior.re>
            |Ou exportez la variable d'environnement BAN_API_KEY avant le build.
            |Test : curl -H "x-api-key: VOTRE_CLE" https://ban.melchior.re/sync/catalog → 200
            """.trimMargin(),
        )
    }
}

afterEvaluate {
    listOf("assembleRelease", "bundleRelease").forEach { taskName ->
        tasks.findByName(taskName)?.doFirst {
            requireBanApiKeyForRelease(taskName)
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    // AndroidX & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.foundation:foundation")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.3.0")
    implementation("androidx.compose.material3:material3-window-size-class:1.2.0")
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.coil.kt.coil.compose)


    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Retrofit + OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // WorkManager + Hilt Extension
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // CameraX
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    implementation("com.google.mlkit:text-recognition:16.0.1")

    implementation("androidx.concurrent:concurrent-futures:1.3.0")
    implementation("androidx.concurrent:concurrent-futures-ktx:1.3.0")

// Accompanist permissions
    implementation(libs.accompanist.permissions)

    // Coil 3
    implementation(libs.coil.compose)
    implementation(libs.coil.network)

    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    implementation(libs.sentry.android)

    implementation("io.objectbox:objectbox-android:3.8.0")
    kapt("io.objectbox:objectbox-processor:3.8.0")

    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")

    // Tests
    testImplementation(libs.junit)
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
