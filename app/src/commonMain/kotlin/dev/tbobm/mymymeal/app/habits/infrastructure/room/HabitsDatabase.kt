package dev.tbobm.mymymeal.app.habits.infrastructure.room

interface HabitsDatabase {
    val supplementDao: SupplementDao
    val coffeeIntakeDao: CoffeeIntakeDao
}
