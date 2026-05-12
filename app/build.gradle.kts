plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.sakeena.easemovieapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sakeena.easemovieapp"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // ✅ Firebase & Google Play Services
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    // ✅ Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")

    // ✅ Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    // ✅ Material UI
    implementation("com.google.android.material:material:1.11.0")

    // ✅ RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // ✅ Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // ✅ Retrofit + API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    // ✅ OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // ✅ Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // ✅ ExoPlayer
    implementation("com.google.android.exoplayer:exoplayer:2.18.1")

    // ✅ Glide
    implementation("com.github.bumptech.glide:glide:4.14.2")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.facebook.android:facebook-login:17.0.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")

    // ✅ Image View
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // ✅ Zoom Image
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    // ✅ Crop
    implementation("com.github.yalantis:ucrop:2.2.8")

    //lottie animation
    implementation("com.airbnb.android:lottie:6.4.0")

    // ✅ Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}