package com.recipenotes.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipenotes.data.repository.MealPlanRepository
import com.recipenotes.data.repository.ShoppingRepository
import com.recipenotes.domain.model.ShoppingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the shopping list screen.
 *
 * Key feature: Auto-generating a shopping list from the weekly meal plan.
 *
 * The generation algorithm (implemented in ShoppingRepository):
 * 1. Get all unique recipe IDs from the meal plan for the given week
 * 2. Collect all ingredients from those recipes
 * 3. Group by (name, unit) case-insensitively
 * 4. Sum quantities within each group
 *    Example: "2 cups flour" + "1 cup flour" = "3 cups flour"
 *    But: "100g butter" and "2 tbsp butter" stay separate (different units)
 * 5. Clear existing auto-generated items (preserves manual additions)
 * 6. Insert the combined ingredient list
 *
 * This approach means regenerating the list is safe and idempotent.
 */
@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingRepository: ShoppingRepository,
    private val mealPlanRepository: MealPlanRepository
) : ViewModel() {

    val items: StateFlow<List<ShoppingItem>> = shoppingRepository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Generate the shopping list from a week's meal plan.
     * Called when navigating from the meal plan screen's "Generate Shopping List" button.
     */
    fun generateFromMealPlan(weekStartDate: String) {
        viewModelScope.launch {
            val recipeIds = mealPlanRepository.getRecipeIdsForWeek(weekStartDate)
            shoppingRepository.generateFromMealPlan(recipeIds)
        }
    }

    fun toggleItem(id: Long) {
        viewModelScope.launch { shoppingRepository.toggleItem(id) }
    }

    fun addManualItem(name: String, quantity: Double, unit: String) {
        viewModelScope.launch { shoppingRepository.addManualItem(name, quantity, unit) }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch { shoppingRepository.deleteItem(id) }
    }

    fun clearChecked() {
        viewModelScope.launch { shoppingRepository.clearChecked() }
    }

    fun clearAll() {
        viewModelScope.launch { shoppingRepository.clearAll() }
    }
}
