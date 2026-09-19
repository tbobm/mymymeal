package com.maksimowiczm.foodyou.habits.domain.entity

import kotlin.time.Instant

data class CoffeeLog(
    val id: Long,
    val instant: Instant,
    val type: CoffeeType,
    val caffeineMg: Int,
)
