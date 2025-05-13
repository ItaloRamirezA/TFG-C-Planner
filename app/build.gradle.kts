plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.app.cplanner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.app.cplanner"
        minSdk = 25
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))

    // Firebase dependencias
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    // Google Play Services
    implementation("com.google.android.gms:play-services-safetynet:18.0.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // AndroidX dependencias
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.androidx.activity)

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    // Dependencias para testear
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Glide core
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("androidx.activity:activity-ktx:1.7.2")

    // Para el color wheel (circulo de colores)
    implementation("com.github.yukuku:ambilwarna:2.0.1")

//    implementation("androidx.core:core-ktx:1.9.0")
//    implementation("androidx.appcompat:appcompat:1.6.1")
//    implementation("com.google.android.material:material:1.11.0")
//    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
//    implementation(libs.androidx.activity)
//    implementation(libs.firebase.storage.ktx)
//    testImplementation("junit:junit:4.13.2")
//    androidTestImplementation("androidx.test.ext:junit:1.1.5")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
//
//    // Firebase
//    implementation("com.google.firebase:firebase-firestore-ktx:24.8.1")
//
//    // Firebase BoM
//    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
//    implementation("com.google.firebase:firebase-auth")
//
//    //AUTH Firebase
//    implementation ("com.google.firebase:firebase-auth:21.0.1")
//    implementation ("com.google.android.gms:play-services-safetynet:18.0.0")
//
//    // AUTH Google
//    implementation("com.google.android.gms:play-services-auth:20.7.0")
//
//    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
}