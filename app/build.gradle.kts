import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.xenikii.timecalculator.apphost"
    compileSdk = 37

    val secrets = Properties()
    val secretsFile = project.rootProject.file("secrets.properties")

    if (secretsFile.exists()) {
        secretsFile.inputStream().use { secrets.load(it) }
    }

    defaultConfig {
        applicationId = "com.xenikii.timecalculator"
        minSdk = 30
        targetSdk = 36
        versionCode = 19
        versionName = "4.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${secrets.getProperty("SUPABASE_URL", "")}\"",
        )
        buildConfigField(
            "String",
            "SUPABASE_KEY",
            "\"${secrets.getProperty("SUPABASE_KEY", "")}\"",
        )
    }

    signingConfigs {
        create("upload") {
            val retrievedStorePassword = secrets.getProperty("STORE_PASSWORD")
                ?: System.getenv("STORE_PASSWORD")
            val retrievedKeyAlias = secrets.getProperty("KEY_ALIAS")
                ?: System.getenv("KEY_ALIAS")
            val retrievedKeyPassword = secrets.getProperty("KEY_PASSWORD")
                ?: System.getenv("KEY_PASSWORD")

            if (retrievedStorePassword.isNullOrBlank() || retrievedKeyAlias.isNullOrBlank() || retrievedKeyPassword.isNullOrBlank()) {
                println(
                    "WARNING: Missing signing configuration for 'upload' signing config. " +
                        "Provide STORE_PASSWORD, KEY_ALIAS and KEY_PASSWORD " +
                        "via secrets.properties or environment variables."
                )
            }

            storeFile = file("../release-keystore.jks")
            storePassword = retrievedStorePassword
            keyAlias = retrievedKeyAlias
            keyPassword = retrievedKeyPassword
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("upload")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.androidx.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(project(":di"))
    implementation(project(":data"))
    implementation(project(":shared"))
    implementation(project(":domain"))
    implementation(project(":feature:home"))
    implementation(project(":feature:routinescreen"))
    implementation(project(":feature:routineeditor"))
    implementation(project(":feature:taskeditor"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:settings"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}