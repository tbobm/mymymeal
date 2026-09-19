package com.maksimowiczm.foodyou.habits.domain.entity

/**
 * Fixed set of quick-log coffee types. [defaultCaffeineMg] is a hardcoded standard value, not
 * derived from any logged food -- edit the constant here if it's wrong, there is no settings UI
 * for it.
 */
enum class CoffeeType(val defaultCaffeineMg: Int) {
    Cup(95),
    Espresso(63),
    Latte(63),
}
