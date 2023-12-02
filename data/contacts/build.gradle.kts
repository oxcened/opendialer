plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
  id("kotlin-kapt")
  id("com.google.dagger.hilt.android")
}

android {
  namespace = "dev.alenajam.opendialer.data.contacts"
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
}

dependencies {
  implementation("androidx.core:core-ktx:1.12.0")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

  implementation("com.google.dagger:hilt-android:2.48.1")
  kapt("com.google.dagger:hilt-compiler:2.48.1")
  androidTestImplementation("com.google.dagger:hilt-android-testing:2.48.1")
  kaptAndroidTest("com.google.dagger:hilt-compiler:2.48.1")
  testImplementation("com.google.dagger:hilt-android-testing:2.48.1")
  kaptTest("com.google.dagger:hilt-compiler:2.48.1")
}

kotlin {
  jvmToolchain(17)
}