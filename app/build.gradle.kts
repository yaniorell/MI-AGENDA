import org.gradle.api.attributes.Attribute
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "com.miagenda.app"
    // Compose 1.12 exige compilar contra la API 37. En este equipo el paquete
    // instalado es platforms/android-37.0, así que el nivel menor es 0.
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        applicationId = "com.miagenda.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Las pruebas de este equipo corren en Windows. El artefacto de Android trae
// bibliotecas .so del telefono, asi que aqui se extrae la DLL de la variante JVM.
val sqliteJvm = configurations.create("sqliteJvm") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Attribute.of("org.jetbrains.kotlin.platform.type", String::class.java), "jvm")
    }
}

val extractSqliteJvm = tasks.register<Copy>("extractSqliteJvm") {
    val bundledJar = sqliteJvm.elements.map { files ->
        files.map { it.asFile }.single { file ->
            file.extension == "jar" && file.name.contains("sqlite-bundled")
        }
    }
    from(bundledJar.map { zipTree(it) }) {
        include("natives/windows_x64/sqliteJni.dll")
    }
    into(layout.buildDirectory.dir("sqlite-jvm"))
}

tasks.withType<Test>().configureEach {
    dependsOn(extractSqliteJvm)
    val nativeDirectory = layout.buildDirectory.dir("sqlite-jvm/natives/windows_x64").get().asFile
    systemProperty("androidx.sqlite.driver.bundled.path", nativeDirectory.absolutePath)
    systemProperty("androidx.sqlite.driver.bundled.name", "sqliteJni.dll")
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.sqlite.bundled)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.sqlite.bundled)
    add("sqliteJvm", libs.androidx.sqlite.bundled)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
