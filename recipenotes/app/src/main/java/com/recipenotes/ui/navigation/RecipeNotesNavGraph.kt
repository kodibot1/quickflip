package com.recipenotes.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.recipenotes.ui.mealplan.MealPlanScreen
import com.recipenotes.ui.recipe.RecipeDetailScreen
import com.recipenotes.ui.recipe.RecipeEditScreen
import com.recipenotes.ui.recipe.RecipeListScreen
import com.recipenotes.ui.shopping.ShoppingListScreen

/**
 * The main navigation graph for RecipeNotes.
 *
 * Navigation Compose uses a NavHost to define the navigation graph declaratively.
 * Each composable() call registers a screen at a specific route.
 * The NavController manages the back stack and handles transitions.
 *
 * Architecture:
 * - Bottom bar has 3 tabs: Recipes, Meal Plan, Shopping List
 * - Recipe flow: List -> Detail -> Edit (stacks on top of list)
 * - Meal Plan and Shopping List are top-level tabs
 * - Scaffold wraps everything with the bottom navigation bar
 */
@Composable
fun RecipeNotesNavGraph() {
    val navController = rememberNavController()

    // Map of bottom tab to icon
    val tabIcons = mapOf(
        BottomNavTab.RECIPES to Icons.Filled.MenuBook,
        BottomNavTab.MEAL_PLAN to Icons.Outlined.CalendarMonth,
        BottomNavTab.SHOPPING to Icons.Filled.ShoppingCart
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            // Only show bottom bar on main tab screens (not on detail/edit screens)
            val showBottomBar = currentDestination?.route in listOf(
                Screen.RecipeList.route,
                Screen.MealPlan.route,
                Screen.ShoppingList.route
            )

            if (showBottomBar) {
                NavigationBar {
                    BottomNavTab.entries.forEach { tab ->
                        val icon = tabIcons[tab] ?: Icons.Filled.MenuBook
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = tab.iconDescription) },
                            label = { Text(tab.label) },
                            selected = currentDestination?.hierarchy?.any {
                                it.route == tab.screen.route
                            } == true,
                            onClick = {
                                navController.navigate(tab.screen.route) {
                                    // Pop up to the start destination to avoid building up
                                    // a large back stack when switching between tabs
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid creating multiple copies of the same destination
                                    launchSingleTop = true
                                    // Restore state when re-selecting a previously selected tab
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.RecipeList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- Recipe tab flow ---

            composable(Screen.RecipeList.route) {
                RecipeListScreen(
                    onRecipeClick = { recipeId ->
                        navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                    },
                    onAddRecipe = {
                        navController.navigate(Screen.RecipeEdit.createRoute(0L))
                    }
                )
            }

            composable(
                route = Screen.RecipeDetail.route,
                arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
            ) {
                RecipeDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onEditRecipe = { recipeId ->
                        navController.navigate(Screen.RecipeEdit.createRoute(recipeId))
                    }
                )
            }

            composable(
                route = Screen.RecipeEdit.route,
                arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
            ) {
                RecipeEditScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onRecipeSaved = { recipeId ->
                        // After saving, go back to detail (or list if creating new)
                        navController.popBackStack()
                    }
                )
            }

            // --- Meal Plan tab ---

            composable(Screen.MealPlan.route) {
                MealPlanScreen(
                    onRecipeClick = { recipeId ->
                        navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                    },
                    onGenerateShoppingList = { weekStartDate ->
                        navController.navigate(Screen.ShoppingList.createRoute(weekStartDate)) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // --- Shopping List tab ---

            composable(
                route = Screen.ShoppingList.route,
                arguments = listOf(
                    navArgument("generateFromWeek") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val generateFromWeek = backStackEntry.arguments?.getString("generateFromWeek")
                ShoppingListScreen(generateFromWeek = generateFromWeek)
            }
        }
    }
}
