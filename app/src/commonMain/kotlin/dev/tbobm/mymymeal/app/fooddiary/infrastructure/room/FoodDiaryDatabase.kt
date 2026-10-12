package dev.tbobm.mymymeal.app.fooddiary.infrastructure.room

interface FoodDiaryDatabase {
    val manualDiaryEntryDao: ManualDiaryEntryDao
    val measurementDao: MeasurementDao
    val mealDao: MealDao
}
