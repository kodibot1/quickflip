// settings.gradle.kts - Configures the Gradle build for the RecipeNotes project.
// This file tells Gradle which modules to include and where to find plugins/dependencies.

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

dependencyResolution {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "RecipeNotes"
include(":app")
