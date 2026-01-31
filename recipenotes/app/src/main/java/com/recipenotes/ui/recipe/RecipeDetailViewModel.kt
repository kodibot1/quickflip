package com.recipenotes.ui.recipe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipenotes.data.repository.RecipeRepository
import com.recipenotes.domain.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the recipe detail screen.
 *
 * SavedStateHandle is a special Hilt/Navigation feature that automatically provides
 * navigation arguments. When you navigate to "recipe/{recipeId}", the recipeId
 * is available in savedStateHandle without manual parsing.
 */
@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val recipeId: Long = savedStateHandle.get<Long>("recipeId") ?: 0L

    /** Observe the recipe reactively - auto-updates if recipe is edited elsewhere */
    val recipe: StateFlow<Recipe?> = recipeRepository.getRecipeById(recipeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun toggleFavourite() {
        viewModelScope.launch {
            recipeRepository.toggleFavourite(recipeId)
        }
    }

    fun deleteRecipe(onDeleted: () -> Unit) {
        viewModelScope.launch {
            recipeRepository.deleteRecipe(recipeId)
            onDeleted()
        }
    }
}
