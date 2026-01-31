package com.quickflip.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Settings : Screen("settings")

    data class Listing(val listingId: Long) : Screen("listing/{listingId}") {
        companion object {
            const val route = "listing/{listingId}"
            fun createRoute(listingId: Long) = "listing/$listingId"
        }
    }
}
