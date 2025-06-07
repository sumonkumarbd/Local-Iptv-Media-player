plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mediaplayer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mediaplayer"
        minSdk = 21
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
}

dependencies {
    // AndroidX Libraries
    implementation (libs.appcompat.v161)
    implementation (libs.constraintlayout.v214)
    implementation (libs.viewpager.v100)
    implementation (libs.fragment.v162)

    // Material Design
    implementation (libs.material.v1100)

    // Media libraries (optional - for enhanced video playback)
    // implementation 'androidx.media3:media3-exoplayer:1.2.0'
    // implementation 'androidx.media3:media3-ui:1.2.0'

    // Testing
    testImplementation (libs.junit)
    androidTestImplementation (libs.junit.v115)
    androidTestImplementation (libs.espresso.core.v351)
}