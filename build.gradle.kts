// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.devtools.ksp") version "1.9.23-1.0.20" apply false
}

buildscript{
    repositories {
        google()  // Asegúrate de que Google está en la lista
        mavenCentral()
    }

}

