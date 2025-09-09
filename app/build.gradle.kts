plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    //id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.abhik.mudrasahayak"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.abhik.mudrasahayak"
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
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Firebase Bill of Materials (BoM) - This manages all Firebase library versions
    // Use the latest version available (e.g., 34.2.0 or newer)
    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))

    implementation("com.google.firebase:firebase-firestore")

    // Add the dependencies for the Firebase products you want to use.
    // The BoM ensures you get compatible versions, so you don't specify them here.
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth") // Just this one line is needed for Auth.
}