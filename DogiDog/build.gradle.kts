

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
}

buildscript {
    dependencies {
        classpath ("com.google.gms:google-services:4.3.15") // Asegúrate de usar la última versión
        classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
    }
}
