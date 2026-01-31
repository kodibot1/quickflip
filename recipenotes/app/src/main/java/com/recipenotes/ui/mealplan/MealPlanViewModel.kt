package com.recipenotes.ui.mealplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipenotes.data.repository.MealPlanRepository
import com.recipenotes.data.repository.RecipeRepository
import com.recipenotes.domain.model.MealPlan
import com.recipenotes.domain.model.MealType
import com.recipenotes.domain.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * ViewModel for the weekly meal planner.
 *
 * Week calculations:
 * - Weeks always start on Monday (ISO standard)
 * - weekStartDate is the Monday of the displayed week as ISO string "2025-01-27"
 * - navigateWeek(+1) goes to next week, (-1) goes to previous
 *
 * The meal plan is stored as a flat list in the database, but we transform it
 * into a nested map (Day -> MealType -> MealPlan?) for easy UI rendering.
 */
@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    /** The Monday of the currently displayed week */
    private val _currentWeekStart = MutableStateFlow(
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    )
    val currentWeekStart: StateFlow<LocalDate> = _currentWeekStart.asStateFlow()

    /** Formatted date range label like "Jan 27 - Feb 2" */
    val weekLabel: StateFlow<String> = _currentWeekStart.let { flow ->
        kotlinx.coroutines.flow.map(flow) { monday ->
            val sunday = monday.plusDays(6)
            val startFormat = DateTimeFormatter.ofPattern("MMM d")
            val endFormat = if (monday.month == sunday.month) {
                DateTimeFormatter.ofPattern("d")
            } else {
                DateTimeFormatter.ofPattern("MMM d")
            }
            "${monday.format(startFormat)} - ${sunday.format(endFormat)}"
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    }

    /**
     * Meal plans for the current week, reactively updating when the week changes.
     * flatMapLatest cancels the previous week's query when navigating to a new week.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val weekMealPlans: StateFlow<List<MealPlan>> = _currentWeekStart
        .flatMapLatest { weekStart ->
            mealPlanRepository.getForWeek(weekStart.toString())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** All recipes available for the picker dialog */
    val allRecipes: StateFlow<List<Recipe>> = recipeRepository.getAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Navigate forward or backward by weeks */
    fun navigateWeek(offset: Int) {
        _currentWeekStart.value = _currentWeekStart.value.plusWeeks(offset.toLong())
    }

    /** Go back to the current week */
    fun goToCurrentWeek() {
        _currentWeekStart.value = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    /** Assign a recipe to a specific day and meal slot */
    fun assignRecipe(recipeId: Long, dayOfWeek: Int, mealType: String) {
        viewModelScope.launch {
            mealPlanRepository.assignRecipe(
                recipeId = recipeId,
                dayOfWeek = dayOfWeek,
                mealType = mealType,
                weekStartDate = _currentWeekStart.value.toString()
            )
        }
    }

    /** Remove a meal plan entry */
    fun removeEntry(id: Long) {
        viewModelScope.launch {
            mealPlanRepository.removeEntry(id)
        }
    }

    /** Get the current week start date as string (for shopping list generation) */
    fun getCurrentWeekStartDate(): String = _currentWeekStart.value.toString()
}
