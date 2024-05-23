plugins {    alias(libs.plugins.androidApplication)
}
android {    namespace = "com.example.registration"
    compileSdk = 34
    defaultConfig {        applicationId = "com.example.registration"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
        isMinifyEnabled = false
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }    }
    compileOptions {        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8    }
}
dependencies {
    implementation(libs.appcompat)
    implementation (libs.material)
    implementation(libs.activity)
    implementation (libs.constraintlayout)
    implementation(libs.glance)
    implementation(libs.work.runtime)
    testImplementation(libs.junit)
    androidTestImplementation (libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("androidx.glance:glance-appwidget:1.0.0-alpha03")
    implementation ("com.squareup.okhttp3:okhttp:4.9.2")
}