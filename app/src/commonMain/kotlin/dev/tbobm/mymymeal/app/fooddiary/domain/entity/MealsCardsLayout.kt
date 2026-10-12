package dev.tbobm.mymymeal.app.fooddiary.domain.entity

enum class MealsCardsLayout {
    Horizontal,
    Vertical;

    companion object {
        val default = Vertical
    }
}
