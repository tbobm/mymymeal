package dev.tbobm.mymymeal.app.app.ui.food.diary.component

import androidx.compose.runtime.*
import dev.tbobm.mymymeal.app.app.ui.food.component.MeasurementPickerState
import dev.tbobm.mymymeal.app.app.ui.food.component.rememberMeasurementPickerState
import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement
import dev.tbobm.mymymeal.app.common.domain.measurement.MeasurementType
import kotlinx.datetime.LocalDate

@Composable
fun rememberFoodMeasurementFormState(
    today: LocalDate,
    possibleDates: List<LocalDate>,
    selectedDate: LocalDate,
    meals: List<String>,
    selectedMeal: String?,
    suggestions: List<Measurement>,
    possibleTypes: List<MeasurementType>,
    selectedMeasurement: Measurement,
): FoodMeasurementFormState {
    val dateState =
        rememberChipsDatePickerState(
            today = today,
            initialDates = possibleDates,
            selectedDate = selectedDate,
        )
    val mealsState = rememberChipsMealPickerState(meals = meals, selectedMeal = selectedMeal)

    val measurementState =
        rememberMeasurementPickerState(
            suggestions = suggestions,
            possibleTypes = possibleTypes,
            selectedMeasurement = selectedMeasurement,
        )

    return remember(dateState, mealsState, measurementState) {
        FoodMeasurementFormState(dateState, mealsState, measurementState)
    }
}

@Stable
class FoodMeasurementFormState(
    val dateState: ChipsDatePickerState,
    val mealsState: ChipsMealPickerState,
    val measurementState: MeasurementPickerState,
) {
    val isValid by derivedStateOf {
        measurementState.inputField.error == null && mealsState.selectedMeal != null
    }
}
