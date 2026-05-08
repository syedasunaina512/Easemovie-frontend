buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.4")
    }
}
// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
}

