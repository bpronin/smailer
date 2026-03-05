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

import org.gradle.kotlin.dsl.implementation
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
    rootProject.file("local.properties").let {
        if (it.exists()) it else rootProject.file(".github/workflows/local.properties")
    }.reader().use(::load)
}

fun getLocalProperty(name: String): String {
    return localProperties.getProperty(name) ?: ""
}

android{
//extensions.configure<ApplicationExtension> {
    namespace = "com.bopr.android.smailer"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        targetSdk = 36
        versionCode = 109
        versionName = "1.12.0"
        applicationId = "com.bopr.android.smailer"
        base.archivesName = "smailer-$versionName"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = getLocalProperty("key_alias")
            storeFile = rootProject.file("keystore.p12").apply {
                if (!exists()) throw InvalidUserDataException("Required file $this")
            }
            storePassword = getLocalProperty("store_password")
            keyPassword = getLocalProperty("key_password")
        }
    }
    
    buildTypes {
        all {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            resValue("string", "developer_email", getLocalProperty("developer_email"))
            resValue("string", "fcm_server_key", getLocalProperty("fcm_server_key"))
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
                getLocalProperty("debug_telegram_token")
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
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.work:work-runtime-ktx:2.11.1")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.android.gms:play-services-auth:21.5.1")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.material:material:1.13.0")
    implementation("com.google.api-client:google-api-client-android:2.9.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev99-1.23.0")
    implementation("com.google.apis:google-api-services-gmail:v1-rev98-1.25.0")
    implementation("com.google.firebase:firebase-analytics:23.0.0")
    implementation("com.google.firebase:firebase-crashlytics:20.0.4")
    implementation("com.google.firebase:firebase-messaging:25.0.1")
    implementation("com.sun.mail:android-mail:1.6.8")
    implementation ("io.ktor:ktor-server-core:3.4.0")
    implementation ("io.ktor:ktor-server-netty:3.4.0")
    implementation ("io.ktor:ktor-server-html-builder:3.4.0")
    
    "freeImplementation"("com.google.android.gms:play-services-ads:25.0.0")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("org.mockito:mockito-android:5.22.0")
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
    val uploadPath = getLocalProperty("upload_path")

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