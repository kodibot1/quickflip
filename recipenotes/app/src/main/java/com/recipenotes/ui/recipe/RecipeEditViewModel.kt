package com.recipenotes.ui.recipe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipenotes.data.repository.RecipeRepository
import com.recipenotes.domain.model.Ingredient
import com.recipenotes.domain.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Form state for creating/editing a recipe.
 * Using a single data class for all form fields makes state management cleaner
 * than having separate MutableStateFlows for each field.
 */
data class RecipeFormState(
    val title: String = "",
    val description: String = "",
    val prepTimeMinutes: String = "", // String for text field, parsed to Int on save
    val cookTimeMinutes: String = "",
    val servings: String = "1",
    val photoUri: String? = null,
    val ingredients: List<IngredientFormState> = listOf(IngredientFormState()),
    val steps: List<String> = listOf(""),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val titleError: String? = null
)

data class IngredientFormState(
    val name: String = "",
    val quantity: String = "",
    val unit: String = ""
)

/**
 * ViewModel for the recipe create/edit screen.
 *
 * Handles both creating new recipes and editing existing ones.
 * If recipeId is provided via navigation, loads existing recipe data into the form.
 *
 * StateFlow vs mutableStateOf:
 * We use StateFlow here for consistency with other ViewModels, even though
 * mutableStateOf would also work in Compose. StateFlow is the standard Kotlin
 * approach and works outside Compose too (useful for testing).
 */
@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val recipeId: Long = savedStateHandle.get<Long>("recipeId") ?: 0L
    val isEditing: Boolean = recipeId != 0L

    private val _formState = MutableStateFlow(RecipeFormState())
    val formState: StateFlow<RecipeFormState> = _formState.asStateFlow()

    init {
        if (isEditing) {
            loadRecipe()
        }
    }

    private fun loadRecipe() {
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            val recipe = recipeRepository.getRecipeById(recipeId).first()
            if (recipe != null) {
                _formState.update {
                    RecipeFormState(
                        title = recipe.title,
                        description = recipe.description,
                        prepTimeMinutes = if (recipe.prepTimeMinutes > 0) recipe.prepTimeMinutes.toString() else "",
                        cookTimeMinutes = if (recipe.cookTimeMinutes > 0) recipe.cookTimeMinutes.toString() else "",
                        servings = recipe.servings.toString(),
                        photoUri = recipe.photoUri,
                        ingredients = recipe.ingredients.map {
                            IngredientFormState(
                                name = it.name,
                                quantity = if (it.quantity > 0) it.quantity.toString() else "",
                                unit = it.unit
                            )
                        }.ifEmpty { listOf(IngredientFormState()) },
                        steps = recipe.steps.ifEmpty { listOf("") }
                    )
                }
            }
        }
    }

    // --- Form field update functions ---

    fun updateTitle(title: String) {
        _formState.update { it.copy(title = title, titleError = null) }
    }

    fun updateDescription(description: String) {
        _formState.update { it.copy(description = description) }
    }

    fun updatePrepTime(time: String) {
        _formState.update { it.copy(prepTimeMinutes = time.filter { c -> c.isDigit() }) }
    }

    fun updateCookTime(time: String) {
        _formState.update { it.copy(cookTimeMinutes = time.filter { c -> c.isDigit() }) }
    }

    fun updateServings(servings: String) {
        _formState.update { it.copy(servings = servings.filter { c -> c.isDigit() }) }
    }

    fun updatePhotoUri(uri: String?) {
        _formState.update { it.copy(photoUri = uri) }
    }

    // --- Ingredient management ---

    fun updateIngredient(index: Int, ingredient: IngredientFormState) {
        _formState.update { state ->
            state.copy(ingredients = state.ingredients.toMutableList().apply { set(index, ingredient) })
        }
    }

    fun addIngredient() {
        _formState.update { state ->
            state.copy(ingredients = state.ingredients + IngredientFormState())
        }
    }

    fun removeIngredient(index: Int) {
        _formState.update { state ->
            val updated = state.ingredients.toMutableList().apply { removeAt(index) }
            state.copy(ingredients = updated.ifEmpty { listOf(IngredientFormState()) })
        }
    }

    // --- Step management ---

    fun updateStep(index: Int, instruction: String) {
        _formState.update { state ->
            state.copy(steps = state.steps.toMutableList().apply { set(index, instruction) })
        }
    }

    fun addStep() {
        _formState.update { state -> state.copy(steps = state.steps + "") }
    }

    fun removeStep(index: Int) {
        _formState.update { state ->
            val updated = state.steps.toMutableList().apply { removeAt(index) }
            state.copy(steps = updated.ifEmpty { listOf("") })
        }
    }

    // --- Save ---

    fun saveRecipe(onSaved: (Long) -> Unit) {
        val state = _formState.value

        // Validation
        if (state.title.isBlank()) {
            _formState.update { it.copy(titleError = "Title is required") }
            return
        }

        val validIngredients = state.ingredients.filter { it.name.isNotBlank() }
        if (validIngredients.isEmpty()) {
            _formState.update { it.copy(titleError = "Add at least one ingredient") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }

            val recipe = Recipe(
                id = recipeId,
                title = state.title.trim(),
                description = state.description.trim(),
                prepTimeMinutes = state.prepTimeMinutes.toIntOrNull() ?: 0,
                cookTimeMinutes = state.cookTimeMinutes.toIntOrNull() ?: 0,
                servings = state.servings.toIntOrNull() ?: 1,
                photoUri = state.photoUri,
                ingredients = validIngredients.map {
                    Ingredient(
                        name = it.name.trim(),
                        quantity = it.quantity.toDoubleOrNull() ?: 0.0,
                        unit = it.unit.trim()
                    )
                },
                steps = state.steps.filter { it.isNotBlank() }.map { it.trim() }
            )

            val savedId = recipeRepository.saveRecipe(recipe)
            _formState.update { it.copy(isSaving = false) }
            onSaved(savedId)
        }
    }
}
