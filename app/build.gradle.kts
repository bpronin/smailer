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

    Uninstall all application"s apk-s from device before running tests cause they may lead to unpredictable results

*/

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties
import java.util.TimeZone

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-allopen")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties").let {
        if (it.exists()) it else rootProject.file(".github/workflows/local.properties")
    }

    file.reader().use(::load)
}

val keyStore = rootProject.file("keystore.p12").apply {
    if (!exists()) throw InvalidUserDataException("Required file $this")
}

android {
    namespace = "com.bopr.android.smailer"
    compileSdk = 35

    defaultConfig {
        versionCode = 102
        versionName = "1.10.3"
        applicationId = "com.bopr.android.smailer"
        minSdk = 21
        targetSdk = 35
        base.archivesName = "smailer-$versionName"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = "sample"
            storeFile = keyStore
            storePassword = localProperties.getProperty("store_password")
            keyPassword = localProperties.getProperty("key_password")
        }
    }

    buildTypes {
        all {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            resValue("string", "developer_email", localProperties.getProperty("developer_email"))
            resValue("string", "fcm_server_key", localProperties.getProperty("fcm_server_key"))
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true
            isShrinkResources = false
            isMinifyEnabled = false
            manifestPlaceholders += mapOf("crashlyticsEnabled" to "false")
            isDefault = true
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
            isShrinkResources = true
            isMinifyEnabled = true
            manifestPlaceholders += mapOf("crashlyticsEnabled" to "true")
        }
//        create("staging") { /* minified debuggable */
//            signingConfig = signingConfigs.getByName("debug")
//            isDebuggable = true
//            isShrinkResources = true
//            isMinifyEnabled = true
//            manifestPlaceholders += mapOf("crashlyticsEnabled" to "false")
//            sourceSets +=
//        }
    }

    flavorDimensions += listOf("main")

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
                "META-INF/kotlinx-html.kotlin_module"
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
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.api-client:google-api-client-android:2.7.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev99-1.23.0")
    implementation("com.google.apis:google-api-services-gmail:v1-rev98-1.25.0")
    implementation("com.google.firebase:firebase-analytics:22.1.2")
    implementation("com.google.firebase:firebase-crashlytics:19.2.1")
    implementation("com.google.firebase:firebase-messaging:24.1.0")
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.0.21")

    "freeImplementation"("com.google.android.gms:play-services-ads:23.5.0")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("org.mockito:mockito-android:5.14.2")
}

allOpen {
    annotation("com.bopr.android.smailer.util.Mockable")
}

tasks.register("updateReleaseInfo") {
    val file = file("src/main/assets/release.properties")

    doFirst {
        Properties().apply {
            file.reader().use(::load)

            val n = getProperty("build_number").toInt() + 1
            setProperty("build_number", n.toString())

            SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").apply {
                timeZone = TimeZone.getTimeZone("UTC")
                setProperty("build_time", format(Date()))
            }

            file.writer().use { store(it, "Release properties") }
        }
    }
}

tasks.register<Copy>("uploadRelease") {
    from(
        fileTree(layout.buildDirectory.dir("outputs/apk/free")).files,
        fileTree(layout.buildDirectory.dir("outputs/apk/paid")).files
    )
    into(localProperties.getProperty("upload_path"))
    include("**/*.apk")
}

tasks.named("preBuild") {
    finalizedBy("updateReleaseInfo")
}

//tasks.named("assemble") {
//    finalizedBy("uploadRelease")
//}

afterEvaluate {
    tasks.named("assemblePaidDebug") {
        finalizedBy("uploadRelease")
    }
}