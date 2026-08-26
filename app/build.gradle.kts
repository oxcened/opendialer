plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    alias(libs.plugins.navigation.safe.args)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.services)
}

val appVersionName = providers.gradleProperty("appVersionName").orNull
    ?: error("appVersionName must be set in gradle.properties")
val semanticVersion = Regex("""(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)""")
    .matchEntire(appVersionName)
    ?: error("appVersionName must use MAJOR.MINOR.PATCH semantic versioning")
val appVersionCode = semanticVersion.groupValues.drop(1).map(String::toLong).let { (major, minor, patch) ->
    require(minor <= 999 && patch <= 999) {
        "appVersionName minor and patch values must be at most 999"
    }
    val code = major * 1_000_000 + minor * 1_000 + patch
    require(code in 1..Int.MAX_VALUE.toLong()) {
        "appVersionName is too large to derive an Android versionCode"
    }
    code.toInt()
}

android {
    namespace = "dev.alenajam.opendialer"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.alenajam.opendialer"
        minSdk = 24
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystorePath = providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull
    if (keystorePath != null) {
        signingConfigs {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull
                    ?: error("ANDROID_KEYSTORE_PASSWORD must be set when signing a release")
                keyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").orNull
                    ?: error("ANDROID_KEY_ALIAS must be set when signing a release")
                keyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull
                    ?: error("ANDROID_KEY_PASSWORD must be set when signing a release")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isDebuggable = true
            versionNameSuffix = "-debug"
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    lint {
        baseline = file("lint-baseline.xml")
        enable += setOf("HardcodedText", "UnusedResources")
        checkDependencies = true
        abortOnError = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

tasks.register("validateReleaseVersion") {
    group = "release"
    description = "Validates that RELEASE_TAG matches appVersionName."
    doLast {
        val releaseTag = providers.environmentVariable("RELEASE_TAG").orNull
            ?: error("RELEASE_TAG must be set, for example RELEASE_TAG=v$appVersionName")
        require(releaseTag == "v$appVersionName") {
            "Release tag $releaseTag does not match appVersionName $appVersionName"
        }
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    implementation(project(":feature:appShell"))
    implementation(project(":feature:calls"))
    implementation(project(":feature:callDetail"))
    implementation(project(":feature:contacts"))
    implementation(project(":feature:inCall"))
    implementation(project(":feature:contactsSearch"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:voicemail"))
    implementation(project(":data:calls"))
    implementation(project(":data:voicemail"))
    implementation(project(":core:common"))

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.reactivestreams.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.recyclerview)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.gson)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.compiler)

    androidTestImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.junit)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.viewbinding)
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization)
}

kotlin {
    jvmToolchain(21)
}

kapt {
    correctErrorTypes = true
}

hilt {
    enableAggregatingTask = true
}
