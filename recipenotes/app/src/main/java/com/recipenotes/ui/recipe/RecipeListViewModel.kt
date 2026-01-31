package com.recipenotes.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipenotes.data.repository.RecipeRepository
import com.recipenotes.domain.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the recipe list screen.
 *
 * MVVM Pattern Explained:
 * - Model: Recipe data class + RecipeRepository (data layer)
 * - View: RecipeListScreen composable (UI layer)
 * - ViewModel: This class (business logic + UI state)
 *
 * The ViewModel survives configuration changes (screen rotation) and holds UI state.
 * The View observes StateFlows and re-renders when state changes.
 * The ViewModel never holds a reference to the View (prevents memory leaks).
 *
 * @HiltViewModel tells Hilt to manage this ViewModel's lifecycle and inject dependencies.
 * When a composable calls hiltViewModel(), Hilt creates this with the right repository.
 */
@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    /** Current search query - empty string means no filter */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** Filter mode: false = all recipes, true = favourites only */
    private val _showFavouritesOnly = MutableStateFlow(false)
    val showFavouritesOnly: StateFlow<Boolean> = _showFavouritesOnly.asStateFlow()

    /**
     * The filtered recipe list - combines search query and filter mode reactively.
     *
     * How this works:
     * 1. combine() merges two StateFlows into one — fires whenever either changes
     * 2. flatMapLatest switches to a new Flow each time the combined value changes
     *    (cancels the previous query — important for fast typing in search)
     * 3. stateIn() converts the Flow to a StateFlow with an initial empty list
     *
     * SharingStarted.WhileSubscribed(5000) means:
     * - Start collecting when a composable subscribes
     * - Keep collecting for 5 seconds after the last subscriber leaves
     *   (avoids restarting the query on quick config changes like rotation)
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val recipes: StateFlow<List<Recipe>> = combine(
        _searchQuery,
        _showFavouritesOnly
    ) { query, favouritesOnly ->
        Pair(query, favouritesOnly)
    }.flatMapLatest { (query, favouritesOnly) ->
        when {
            query.isNotBlank() -> recipeRepository.searchRecipes(query)
            favouritesOnly -> recipeRepository.getFavouriteRecipes()
            else -> recipeRepository.getAllRecipes()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavouritesFilter() {
        _showFavouritesOnly.value = !_showFavouritesOnly.value
    }

    fun toggleFavourite(recipeId: Long) {
        viewModelScope.launch {
            recipeRepository.toggleFavourite(recipeId)
        }
    }
}
