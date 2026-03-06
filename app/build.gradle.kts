/*
    Some additional files are required to build the project:
    1. Google configuration from
        https://console.firebase.google.com/u/0/project/smailer-24874/settings/general/android:com.bopr.android.smailer
        should be put into app/google-services.json
    2.  Signing keystore should be put into /app/keystore.p12
    3.  Signing and other user specific parameters should be put into /app/local.gradle.kts (see local.gradle.kts.sample)

    To run debug flavor add SHA-1 fingerprint from
    <user_dir>/.android/debug.keystore (password "android") to api console:
    https://console.developers.google.com/apis/credentials/oauthclient/376904884028-f0m6ki37c8b4cf93aktk0jgag3tiu922.apps.googleusercontent.com?project=smailer-24874

    Uninstall all application's apk-s from device before running tests cause they may lead to unpredictable results

*/

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties
import java.util.TimeZone

plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    id("kotlin-allopen")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").let {
        if (it.exists()) it else rootProject.file(".github/workflows/local.properties")
    }.reader().use(::load)
}

fun localProperty(name: String): String {
    return localProperties.getProperty(name) ?: ""
}

android {
    namespace = "com.bopr.android.smailer"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 111
        versionName = "1.12.2"
        applicationId = "com.bopr.android.smailer"
        base.archivesName = "smailer-$versionName"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = localProperty("key_alias")
            storeFile = rootProject.file("keystore.p12").apply {
                if (!exists()) throw InvalidUserDataException("Required file $this")
            }
            storePassword = localProperty("store_password")
            keyPassword = localProperty("key_password")
        }
    }

    buildTypes {
        all {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            resValue("string", "developer_email", localProperty("developer_email"))
            resValue("string", "fcm_server_key", localProperty("fcm_server_key"))
        }
        debug {
            manifestPlaceholders += mapOf("crashlyticsEnabled" to "false")
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true
            isShrinkResources = false
            isMinifyEnabled = false
            isDefault = true
            resValue(
                "string",
                "debug_telegram_token",
                localProperty("debug_telegram_token")
            )
        }
        release {
            manifestPlaceholders += mapOf("crashlyticsEnabled" to "false")
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
            isShrinkResources = true
            isMinifyEnabled = true
        }
    }

    buildFeatures {
        resValues = true
    }

    flavorDimensions += "main"

    productFlavors {
        create("paid") {
            dimension = "main"
            isDefault = true
        }
        create("free") {
            dimension = "main"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    testOptions.unitTests.isReturnDefaultValues = true

    packaging {
        resources {
            pickFirsts += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE.txt",
                "META-INF/LICENSE.md",
                "META-INF/NOTICE.md",
                "META-INF/kotlinx-html.kotlin_module",
                "META-INF/INDEX.LIST",
                "META-INF/io.netty.versions.properties",
            )
        }
    }

    configurations.all {
        exclude("org.apache.httpcomponents")
        exclude("commons-logging", "commons-logging")
        exclude("com.google.guava", "listenablefuture")
    }

    lint {
        abortOnError = false
    }
    
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.volley)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.location)
    implementation(libs.material)
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.drive)
    implementation(libs.google.api.services.gmail)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.android.mail)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.html.builder)

    "freeImplementation"(libs.play.services.ads)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.contrib)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.mockito.android)
}

allOpen {
    annotation("com.bopr.android.smailer.util.Mockable")
}

tasks.register("updateReleaseInfo") {
    val file = file("src/main/assets/release.properties")

    doFirst {
        Properties().also { properties ->
            file.reader().use { properties.load(it) }

            val buildNumber = properties.getProperty("build_number", "0").toInt() + 1
            properties.setProperty("build_number", buildNumber.toString())

            SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").apply {
                timeZone = TimeZone.getTimeZone("UTC")
                properties.setProperty("build_time", format(Date()))
            }

            file.writer().use { properties.store(it, "Release properties") }
        }
    }
}

tasks.register<Copy>("uploadRelease") {
    val uploadPath = localProperty("upload_path")

    from(
        fileTree(layout.buildDirectory.dir("outputs/apk/free")).files,
        fileTree(layout.buildDirectory.dir("outputs/apk/paid")).files
    )
    into(uploadPath)
    include("**/*.apk")

    doLast {
        logger.lifecycle("Uploaded release APK to $uploadPath.")
    }
}

tasks.named("preBuild") {
    finalizedBy("updateReleaseInfo")
}

afterEvaluate {
    tasks.named("assemblePaidDebug") {
        finalizedBy("uploadRelease")
    }
}
