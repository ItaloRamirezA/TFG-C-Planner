// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.3" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false

}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        //classpath(libs.google.services)
        classpath("com.google.gms:google-services:4.4.2")
    }
}