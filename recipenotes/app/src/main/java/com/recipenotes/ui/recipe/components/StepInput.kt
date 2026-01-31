package com.recipenotes.ui.recipe.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

/**
 * A row for a single preparation step in the recipe edit form.
 * Shows the step number, instruction text field, and a remove button.
 */
@Composable
fun StepInput(
    stepNumber: Int,
    instruction: String,
    onInstructionChange: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step number label
        Text(
            text = "$stepNumber.",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 4.dp)
        )

        // Instruction text field
        OutlinedTextField(
            value = instruction,
            onValueChange = onInstructionChange,
            label = { Text("Step $stepNumber") },
            modifier = Modifier.weight(1f),
            minLines = 2,
            maxLines = 4
        )

        // Remove button
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Remove step",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
