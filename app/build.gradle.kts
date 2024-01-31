plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hiddenapi.refine)
}

val androidMinSdkVersion: Int by rootProject.extra
val androidTargetSdkVersion: Int by rootProject.extra
val androidCompileSdkVersion: Int by rootProject.extra

val androidSourceCompatibility: JavaVersion by rootProject.extra
val androidTargetCompatibility: JavaVersion by rootProject.extra
val androidKotlinJvmTarget: String by rootProject.extra

android {
    namespace = "xyz.mufanc.imagedumper"
    compileSdk = androidCompileSdkVersion

    defaultConfig {
        applicationId = "xyz.mufanc.imagedumper"
        minSdk = androidMinSdkVersion
        targetSdk = androidTargetSdkVersion
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = androidSourceCompatibility
        targetCompatibility = androidTargetCompatibility
    }

    kotlinOptions {
        jvmTarget = androidKotlinJvmTarget
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    compileOnly(project(":api-stub"))
    compileOnly(libs.xposed.api)
    implementation(libs.xposed.service)
    ksp(libs.autox.ksp)
    implementation(libs.autox.annotation)
    implementation(libs.joor)
    compileOnly(libs.hiddenapi.stub)
    implementation(libs.hiddenapi.compat)
    implementation(libs.simple.storage)
    implementation(libs.material)
}
