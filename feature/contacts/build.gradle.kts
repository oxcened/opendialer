plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
  id("kotlin-kapt")
  id("com.google.dagger.hilt.android")
  alias(libs.plugins.compose.compiler)
}

android {
  namespace = "dev.alenajam.opendialer.feature.contacts"
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
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.2"
  }
}

dependencies {
  implementation(project(":data:contacts"))
  implementation(project(":core:common"))

  implementation("androidx.core:core-ktx:1.12.0")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("com.google.android.material:material:1.10.0")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

  implementation("com.google.dagger:hilt-android:2.48.1")
  kapt("com.google.dagger:hilt-compiler:2.48.1")
  androidTestImplementation("com.google.dagger:hilt-android-testing:2.48.1")
  kaptAndroidTest("com.google.dagger:hilt-compiler:2.48.1")
  testImplementation("com.google.dagger:hilt-android-testing:2.48.1")
  kaptTest("com.google.dagger:hilt-compiler:2.48.1")

  implementation("com.squareup.picasso:picasso:2.71828")
  implementation("org.ocpsoft.prettytime:prettytime:4.0.1.Final")

  implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
  implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
  implementation("androidx.lifecycle:lifecycle-reactivestreams-ktx:2.6.2")

  implementation("androidx.fragment:fragment-ktx:1.6.2")
  implementation("androidx.legacy:legacy-support-v4:1.0.0")
  implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
  implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
  implementation("androidx.lifecycle:lifecycle-reactivestreams-ktx:2.6.2")
  implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
  implementation("androidx.navigation:navigation-ui-ktx:2.7.5")
  implementation("androidx.preference:preference-ktx:1.2.1")
  implementation("androidx.recyclerview:recyclerview:1.3.2")

  val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
  implementation(composeBom)
  androidTestImplementation(composeBom)
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.ui:ui-tooling-preview")
  debugImplementation("androidx.compose.ui:ui-tooling")
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
  implementation("androidx.compose.material:material-icons-extended")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
  implementation("androidx.compose.runtime:runtime-livedata")

  implementation("io.coil-kt:coil-compose:2.5.0")
}

kotlin {
  jvmToolchain(17)
}