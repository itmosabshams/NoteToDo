plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("kapt")
    id ("kotlin-parcelize")
    id ("dagger.hilt.android.plugin")
}

android {
    namespace = "com.shams.notetodo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.shams.notetodo"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    // Dagger Hilt
    val hiltVersion = "2.50"
    implementation(libs.hilt.android)
    kapt("com.google.dagger:hilt-compiler:$hiltVersion")
    implementation(libs.androidx.hilt.navigation.compose)
    // Navigation
    implementation(libs.androidx.navigation.compose)
    // Room
    val roomVersion = "2.6.1"
    implementation(libs.androidx.room.runtime)
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation(libs.androidx.room.ktx)
    // Coil
    implementation(libs.coil.compose)
    // Permission
    implementation(libs.accompanist.permissions)
    // Icons - Extended
    implementation(libs.androidx.material.icons.extended)
    // LocalTime
    implementation(libs.kotlinx.datetime)
    // Lottie Animation
    implementation(libs.lottie.compose)
    // Gson
    implementation(libs.gson)
    // Data Store
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.serialization.json)
    // Lifecycle
    val lifecycleVersion = "2.6.1"
    implementation(libs.androidx.lifecycle.service)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    // Worker
    val workerVersion = "2.7.1"
    implementation(libs.androidx.work.runtime.ktx)
    // HiltWorker
    val hiltWorkerVersion = "1.0.0"
    implementation(libs.androidx.hilt.work)
    kapt("androidx.hilt:hilt-compiler:$hiltWorkerVersion")
    // Swipeable
    implementation(libs.androidx.material)
//    dependencies {
//        implementation ("com.github.samanzamani.persian-date:PersianDate:0.8.9")
//    }

    implementation("com.github.hamooo90:jalali-datepicker-compose:1.2.0")
    implementation("ir.huri:JalaliCalendar:1.3.3")
    implementation("com.github.msarhan:ummalqura-calendar:1.1.8")
//    implementation("nl.dionsegijn:confetti-compose:1.1.2")
    implementation("nl.dionsegijn:konfetti-compose:2.0.5")

    implementation("com.airbnb.android:lottie-compose:6.3.0")

}