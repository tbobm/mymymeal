package com.maksimowiczm.foodyou.habits.domain.entity

/** A supplement the user takes regularly. Adherence only -- no dose or timestamp. */
data class Supplement(val id: Long, val name: String, val sortOrder: Int)
