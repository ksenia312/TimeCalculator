plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.xenikii.timecalculator.di"
    compileSdk = 37

    defaultConfig {
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":shared"))
    implementation(project(":feature:home"))
    implementation(project(":feature:landing"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:routinescreen"))
    implementation(project(":feature:routineeditor"))
    implementation(project(":feature:routineslist"))
    implementation(project(":feature:taskeditor"))
    implementation(project(":feature:taskslist"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:settings"))
    implementation(libs.koin.core)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
}
