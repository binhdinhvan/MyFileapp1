plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.myfile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myfile"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.activity:activity:1.8.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // [C] Xem anh/video/EXIF + nen-giai nen
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
}
