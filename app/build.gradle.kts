plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Keep as the string version
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.watcomtravels"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.watcomtravels"
        minSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        // Not sure which build config works
        buildFeatures.buildConfig = true
        buildConfig = true
    }

    secrets {
        // To add your Maps API key to this project:
        // 1. If the secrets.properties file does not exist, create it in the same folder as the local.properties file.
        // 2. Add this line, where YOUR_API_KEY is your API key:
        //        MAPS_API_KEY=YOUR_API_KEY
        propertiesFileName = "secrets.properties"

        // A properties file containing default secret values. This file can be
        // checked in version control.
        defaultPropertiesFileName = "local.defaults.properties"

        // Configure which keys should be ignored by the plugin by providing regular expressions.
        // "sdk.dir" is ignored by default.
        ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
        ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
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
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.junit.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.play.services.location)
    implementation(libs.maps.compose)
    implementation(libs.secrets.gradle.plugin)
//    implementation(libs.kotlin.bom)
    implementation("com.google.android.libraries.places:places:4.1.0")


    testImplementation("io.mockk:mockk:1.13.3")
    // Required -- JUnit 4 framework
    testImplementation(libs.junit.junit)
    // Optional -- Robolectric environment
    // https://mvnrepository.com/artifact/org.robolectric/robolectric
    testImplementation("org.robolectric:robolectric:4.14.1")
    // Optional -- Mockito framework
    testImplementation ("org.mockito:mockito-core:5.14.2")
    // Optional -- mockito-kotlin
    // testImplementation ("org.mockito.kotlin:mockito-kotlin:5.14.2")
    // testImplementation ("org.kotlin.mockito:kotlin-mockito:5.14.2")
    // Optional -- Mockk framework
    // testImplementation("io.mockk:mockk")
    testImplementation ("org.json:json:20240303")

}