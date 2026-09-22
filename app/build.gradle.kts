plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.nintec"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.nintec"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Supabase configuration — injected into BuildConfig
        buildConfigField("String", "SUPABASE_URL", "\"https://mcegdtcufzxpmezpncab.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1jZWdkdGN1Znp4cG1lenBuY2FiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAwOTIzNzQsImV4cCI6MjEwNTY2ODM3NH0.cRUAUjDvVEj2vc-PVk3_bICkrDYiMwiLPJBBGm1UQZU\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.fragment)
    implementation(libs.material)
    implementation(libs.recyclerview)

    // Supabase integration: Retrofit + OkHttp + Gson
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Image loading from Supabase Storage
    implementation(libs.glide)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}