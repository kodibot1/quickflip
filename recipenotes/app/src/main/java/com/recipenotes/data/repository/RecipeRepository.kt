package com.recipenotes.data.repository

import com.recipenotes.data.local.dao.RecipeDao
import com.recipenotes.data.local.entity.IngredientEntity
import com.recipenotes.data.local.entity.RecipeEntity
import com.recipenotes.data.local.entity.StepEntity
import com.recipenotes.domain.model.Ingredient
import com.recipenotes.domain.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository interface for recipe operations.
 *
 * Why use an interface + implementation?
 * - Testability: In tests, you can create a FakeRecipeRepository that returns
 *   hardcoded data without needing a real database
 * - Flexibility: Could swap Room for an API backend without changing ViewModels
 * - Clean Architecture: ViewModels depend on the interface (abstraction),
 *   not the concrete implementation (Room details)
 */
interface RecipeRepository {
    fun getAllRecipes(): Flow<List<Recipe>>
    fun getRecipeById(id: Long): Flow<Recipe?>
    fun searchRecipes(query: String): Flow<List<Recipe>>
    fun getFavouriteRecipes(): Flow<List<Recipe>>
    suspend fun saveRecipe(recipe: Recipe): Long
    suspend fun deleteRecipe(id: Long)
    suspend fun toggleFavourite(id: Long)
}

/**
 * Implementation that maps between Room entities and domain models.
 *
 * The mapping happens here so that:
 * - ViewModels only see clean domain models (no Room dependency)
 * - Database schema changes are isolated to this layer
 * - Complex joins (recipe + ingredients + steps) are assembled here
 */
class RecipeRepositoryImpl @Inject constructor(
    private val recipeDao: RecipeDao
) : RecipeRepository {

    override fun getAllRecipes(): Flow<List<Recipe>> =
        recipeDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun getRecipeById(id: Long): Flow<Recipe?> =
        recipeDao.getById(id).map { entity ->
            entity?.let {
                val ingredients = recipeDao.getIngredientsForRecipeOnce(id)
                val steps = recipeDao.getStepsForRecipeOnce(id)
                it.toDomain(ingredients, steps)
            }
        }

    override fun searchRecipes(query: String): Flow<List<Recipe>> =
        recipeDao.search(query).map { entities -> entities.map { it.toDomain() } }

    override fun getFavouriteRecipes(): Flow<List<Recipe>> =
        recipeDao.getFavourites().map { entities -> entities.map { it.toDomain() } }

    override suspend fun saveRecipe(recipe: Recipe): Long {
        val now = System.currentTimeMillis()
        val entity = RecipeEntity(
            id = recipe.id,
            title = recipe.title,
            description = recipe.description,
            prepTimeMinutes = recipe.prepTimeMinutes,
            cookTimeMinutes = recipe.cookTimeMinutes,
            servings = recipe.servings,
            photoUri = recipe.photoUri,
            isFavourite = recipe.isFavourite,
            createdAt = if (recipe.id == 0L) now else recipe.createdAt,
            updatedAt = now
        )

        val recipeId = if (recipe.id == 0L) {
            recipeDao.insert(entity)
        } else {
            recipeDao.update(entity)
            recipe.id
        }

        // Replace ingredients and steps (delete old, insert new)
        recipeDao.deleteIngredientsForRecipe(recipeId)
        recipeDao.deleteStepsForRecipe(recipeId)

        val ingredientEntities = recipe.ingredients.mapIndexed { index, ingredient ->
            IngredientEntity(
                recipeId = recipeId,
                name = ingredient.name,
                quantity = ingredient.quantity,
                unit = ingredient.unit,
                sortOrder = index
            )
        }
        recipeDao.insertIngredients(ingredientEntities)

        val stepEntities = recipe.steps.mapIndexed { index, instruction ->
            StepEntity(
                recipeId = recipeId,
                stepNumber = index + 1,
                instruction = instruction
            )
        }
        recipeDao.insertSteps(stepEntities)

        return recipeId
    }

    override suspend fun deleteRecipe(id: Long) {
        recipeDao.deleteById(id)
    }

    override suspend fun toggleFavourite(id: Long) {
        recipeDao.toggleFavourite(id)
    }

    // --- Mapping functions ---

    /** Convert entity to domain model (without ingredients/steps - for list views) */
    private fun RecipeEntity.toDomain() = Recipe(
        id = id,
        title = title,
        description = description,
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        servings = servings,
        photoUri = photoUri,
        isFavourite = isFavourite,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    /** Convert entity to domain model with ingredients and steps (for detail view) */
    private fun RecipeEntity.toDomain(
        ingredients: List<IngredientEntity>,
        steps: List<StepEntity>
    ) = Recipe(
        id = id,
        title = title,
        description = description,
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        servings = servings,
        photoUri = photoUri,
        isFavourite = isFavourite,
        ingredients = ingredients.map { Ingredient(it.id, it.name, it.quantity, it.unit) },
        steps = steps.map { it.instruction },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
