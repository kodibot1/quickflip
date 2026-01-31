package com.recipenotes.ui.mealplan

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recipenotes.ui.mealplan.components.DayColumn
import com.recipenotes.ui.mealplan.components.RecipePickerDialog

/**
 * Weekly meal planner screen.
 *
 * Displays a horizontally scrollable grid of 7 days, each with 4 meal slots.
 * Users can navigate between weeks and assign recipes to specific meals.
 *
 * The "Generate Shopping List" FAB navigates to the shopping list tab
 * and triggers generation from this week's meal plan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    onRecipeClick: (Long) -> Unit,
    onGenerateShoppingList: (String) -> Unit,
    viewModel: MealPlanViewModel = hiltViewModel()
) {
    val weekStart by viewModel.currentWeekStart.collectAsStateWithLifecycle()
    val weekLabel by viewModel.weekLabel.collectAsStateWithLifecycle()
    val mealPlans by viewModel.weekMealPlans.collectAsStateWithLifecycle()
    val allRecipes by viewModel.allRecipes.collectAsStateWithLifecycle()

    // State for the recipe picker dialog
    var pickerState by remember { mutableStateOf<Pair<Int, String>?>(null) } // (dayOfWeek, mealType) or null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Plan") },
                actions = {
                    // "Today" button to jump back to current week
                    IconButton(onClick = { viewModel.goToCurrentWeek() }) {
                        Icon(Icons.Filled.Today, contentDescription = "Go to current week")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onGenerateShoppingList(viewModel.getCurrentWeekStartDate()) },
                icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null) },
                text = { Text("Shopping List") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Week navigation bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.navigateWeek(-1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous week")
                }
                Text(
                    text = weekLabel,
                    modifier = Modifier.padding(top = 12.dp)
                )
                IconButton(onClick = { viewModel.navigateWeek(1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next week")
                }
            }

            // Horizontally scrollable day columns (7 days)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                (0..6).forEach { dayOfWeek ->
                    val dayMealPlans = mealPlans.filter { it.dayOfWeek == dayOfWeek }
                    DayColumn(
                        dayOfWeek = dayOfWeek,
                        weekStartDate = weekStart,
                        mealPlans = dayMealPlans,
                        onAddMeal = { day, mealType -> pickerState = Pair(day, mealType) },
                        onRemoveMeal = { viewModel.removeEntry(it) },
                        onRecipeClick = onRecipeClick
                    )
                }
            }
        }
    }

    // Recipe picker dialog
    pickerState?.let { (dayOfWeek, mealType) ->
        RecipePickerDialog(
            recipes = allRecipes,
            onRecipeSelected = { recipeId ->
                viewModel.assignRecipe(recipeId, dayOfWeek, mealType)
            },
            onDismiss = { pickerState = null }
        )
    }
}
