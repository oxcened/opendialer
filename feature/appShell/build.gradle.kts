plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.alenajam.opendialer.feature.appShell"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":feature:callDetail"))
    implementation(project(":feature:calls"))
    implementation(project(":feature:contacts"))
    implementation(project(":feature:contactsSearch"))
    implementation(project(":feature:settings"))

    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization)
}

kotlin {
    jvmToolchain(21)
}
