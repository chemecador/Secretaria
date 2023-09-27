// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:7.2.2")
        classpath ("com.google.gms:google-services:4.4.0")
        // Add the dependency for the Crashlytics Gradle plugin
        classpath ("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
    }
}
