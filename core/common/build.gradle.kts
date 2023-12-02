plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
  id("kotlin-kapt")
}

android {
  namespace = "dev.alenajam.opendialer.core.common"
  compileSdk = 34

  defaultConfig {
    minSdk = 24

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  buildFeatures {
    viewBinding = true
  }
}

dependencies {

  implementation("androidx.core:core-ktx:1.9.0")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("com.google.android.material:material:1.10.0")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

  implementation("com.squareup.picasso:picasso:2.71828")
  implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
  implementation("androidx.navigation:navigation-ui-ktx:2.7.5")
  implementation("androidx.preference:preference-ktx:1.2.1")
  implementation("com.google.code.gson:gson:2.9.0")
}

kotlin {
  jvmToolchain(17)
}