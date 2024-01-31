// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.agp.app) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.agp.lib) apply false
}

val androidMinSdkVersion by extra(26)
val androidTargetSdkVersion by extra(34)
val androidCompileSdkVersion by extra(34)

val androidSourceCompatibility by extra(JavaVersion.VERSION_17)
val androidTargetCompatibility by extra(JavaVersion.VERSION_17)
val androidKotlinJvmTarget by extra("17")
