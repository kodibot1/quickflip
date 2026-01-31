package com.recipenotes.ui.mealplan.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.recipenotes.domain.model.MealPlan
import com.recipenotes.domain.model.MealType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * A column representing one day in the weekly meal planner.
 * Contains a header (day name + date) and 4 MealSlot cards (one per meal type).
 *
 * @param dayOfWeek 0 = Monday, 6 = Sunday
 * @param weekStartDate The Monday of the displayed week
 * @param mealPlans The meal plan entries for this specific day
 */
@Composable
fun DayColumn(
    dayOfWeek: Int,
    weekStartDate: LocalDate,
    mealPlans: List<MealPlan>,
    onAddMeal: (Int, String) -> Unit, // (dayOfWeek, mealType) -> show picker
    onRemoveMeal: (Long) -> Unit,
    onRecipeClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val date = weekStartDate.plusDays(dayOfWeek.toLong())
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val isToday = date == LocalDate.now()

    Column(
        modifier = modifier
            .width(140.dp)
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Day header
        Column {
            Text(
                text = dayNames[dayOfWeek],
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = date.format(DateTimeFormatter.ofPattern("d MMM")),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // One slot per meal type
        MealType.ALL.forEach { mealType ->
            val mealPlan = mealPlans.find { it.mealType == mealType }
            MealSlot(
                mealType = mealType,
                mealPlan = mealPlan,
                onAddClick = { onAddMeal(dayOfWeek, mealType) },
                onRemoveClick = { mealPlan?.let { onRemoveMeal(it.id) } },
                onRecipeClick = onRecipeClick
            )
        }
    }
}
