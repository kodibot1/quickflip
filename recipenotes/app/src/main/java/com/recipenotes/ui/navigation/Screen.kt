package com.recipenotes.ui.navigation

/**
 * Sealed class defining all navigation routes in the app.
 *
 * Why a sealed class instead of plain strings?
 * - Type safety: Compiler ensures you handle all routes in when() expressions
 * - No typos: Route strings are defined once, referenced by class
 * - Arguments: Route parameters are part of the class definition
 * - Discoverability: IDE auto-complete shows all available screens
 *
 * Each screen has a 'route' property that matches the Navigation Compose route pattern.
 * Routes with arguments use {argName} placeholders that Navigation fills at runtime.
 */
sealed class Screen(val route: String) {

    /** Recipe list - the main recipes tab (default start destination) */
    data object RecipeList : Screen("recipe_list")

    /** Recipe detail - shows full recipe info */
    data object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun createRoute(recipeId: Long) = "recipe_detail/$recipeId"
    }

    /** Recipe edit/create - form for editing or creating a recipe */
    data object RecipeEdit : Screen("recipe_edit/{recipeId}") {
        /** Create route: pass 0 for new recipe, actual ID for editing */
        fun createRoute(recipeId: Long = 0L) = "recipe_edit/$recipeId"
    }

    /** Weekly meal planner tab */
    data object MealPlan : Screen("meal_plan")

    /** Shopping list tab */
    data object ShoppingList : Screen("shopping_list?generateFromWeek={generateFromWeek}") {
        fun createRoute(generateFromWeek: String? = null): String {
            return if (generateFromWeek != null) {
                "shopping_list?generateFromWeek=$generateFromWeek"
            } else {
                "shopping_list"
            }
        }
    }
}

/** Bottom navigation tab destinations */
enum class BottomNavTab(val screen: Screen, val label: String, val iconDescription: String) {
    RECIPES(Screen.RecipeList, "Recipes", "Recipes tab"),
    MEAL_PLAN(Screen.MealPlan, "Meal Plan", "Meal plan tab"),
    SHOPPING(Screen.ShoppingList, "Shopping", "Shopping list tab")
}
