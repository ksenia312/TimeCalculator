@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TimeCalculator"
include(":app")
include(":domain")
include(":data")
include(":shared")
include(":di")
include(":feature:home")
include(":feature:landing")
include(":feature:routinescreen")
include(":feature:routineeditor")
include(":feature:routineslist")
include(":feature:taskeditor")
include(":feature:taskslist")
include(":feature:auth")
include(":feature:settings")
