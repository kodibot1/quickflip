// Root build.gradle.kts - Declares plugins used across the project.
// The "apply false" means the plugin is available but not applied at root level;
// individual modules (like :app) apply them as needed.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
}
