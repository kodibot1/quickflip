package com.recipenotes.ui.recipe.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipenotes.ui.recipe.IngredientFormState

/**
 * A row of input fields for a single ingredient in the recipe edit form.
 * Shows name, quantity, unit fields and a remove button.
 */
@Composable
fun IngredientInput(
    ingredient: IngredientFormState,
    onIngredientChange: (IngredientFormState) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ingredient name (takes most space)
        OutlinedTextField(
            value = ingredient.name,
            onValueChange = { onIngredientChange(ingredient.copy(name = it)) },
            label = { Text("Ingredient") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        // Quantity (small field)
        OutlinedTextField(
            value = ingredient.quantity,
            onValueChange = { onIngredientChange(ingredient.copy(quantity = it)) },
            label = { Text("Qty") },
            singleLine = true,
            modifier = Modifier.width(72.dp)
        )

        // Unit (small field)
        OutlinedTextField(
            value = ingredient.unit,
            onValueChange = { onIngredientChange(ingredient.copy(unit = it)) },
            label = { Text("Unit") },
            singleLine = true,
            modifier = Modifier.width(72.dp)
        )

        // Remove button
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Remove ingredient",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
