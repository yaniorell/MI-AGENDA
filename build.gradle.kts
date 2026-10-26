buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // AGP 9 usa Kotlin 2.2.10 si no se indica otra versión.
        // Esta línea sube el compilador a la versión fijada en la Fase 0.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}
