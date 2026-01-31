package com.recipenotes.ui.recipe

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.recipenotes.ui.recipe.components.IngredientInput
import com.recipenotes.ui.recipe.components.StepInput

/**
 * Recipe create/edit screen with form fields for all recipe data.
 *
 * Uses the Android Photo Picker (PickVisualMedia) which doesn't require
 * runtime permissions on Android 11+ and provides a consistent, private
 * photo selection experience.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    onNavigateBack: () -> Unit,
    onRecipeSaved: (Long) -> Unit,
    viewModel: RecipeEditViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    // Photo picker launcher - fires a system UI for selecting a photo
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.updatePhotoUri(it.toString()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Edit Recipe" else "New Recipe") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!formState.isSaving) {
                FloatingActionButton(onClick = { viewModel.saveRecipe(onRecipeSaved) }) {
                    Icon(Icons.Filled.Save, contentDescription = "Save recipe")
                }
            }
        }
    ) { padding ->
        if (formState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Photo section
            if (formState.photoUri != null) {
                AsyncImage(
                    model = formState.photoUri,
                    contentDescription = "Recipe photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(MaterialTheme.shapes.medium)
                )
            }
            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.AddAPhoto, contentDescription = null)
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(if (formState.photoUri != null) "Change Photo" else "Add Photo")
            }

            // Title (required)
            OutlinedTextField(
                value = formState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("Title *") },
                isError = formState.titleError != null,
                supportingText = formState.titleError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Description
            OutlinedTextField(
                value = formState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            // Time and servings row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = formState.prepTimeMinutes,
                    onValueChange = viewModel::updatePrepTime,
                    label = { Text("Prep (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.cookTimeMinutes,
                    onValueChange = viewModel::updateCookTime,
                    label = { Text("Cook (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.servings,
                    onValueChange = viewModel::updateServings,
                    label = { Text("Servings") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Ingredients section
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Ingredients", style = MaterialTheme.typography.titleMedium)

            formState.ingredients.forEachIndexed { index, ingredient ->
                IngredientInput(
                    ingredient = ingredient,
                    onIngredientChange = { viewModel.updateIngredient(index, it) },
                    onRemove = { viewModel.removeIngredient(index) }
                )
            }

            Button(onClick = viewModel::addIngredient) {
                Text("+ Add Ingredient")
            }

            // Steps section
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Steps", style = MaterialTheme.typography.titleMedium)

            formState.steps.forEachIndexed { index, step ->
                StepInput(
                    stepNumber = index + 1,
                    instruction = step,
                    onInstructionChange = { viewModel.updateStep(index, it) },
                    onRemove = { viewModel.removeStep(index) }
                )
            }

            Button(onClick = viewModel::addStep) {
                Text("+ Add Step")
            }

            // Bottom spacing for FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
