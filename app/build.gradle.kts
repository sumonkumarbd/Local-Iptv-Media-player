plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.feed.sphere"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.feed.sphere"
        minSdk = 21
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    //ViewPager
    implementation(libs.viewpager2)
    
    // ExoPlayer dependencies
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.datasource)
    implementation(libs.media3.common)


    implementation(libs.leanback)
    implementation(libs.circleimageview)



    // volley
    implementation(libs.volley)

    //Glide
    implementation(libs.glide)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0") {
        exclude(group = "com.github.bumptech.glide", module = "annotations")
    }
    implementation(libs.annotations)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}