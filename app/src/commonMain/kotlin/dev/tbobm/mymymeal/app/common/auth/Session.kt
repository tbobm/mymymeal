package dev.tbobm.mymymeal.app.common.auth

import kotlin.time.Instant

data class Session(
    val userId: String,
    val userEmail: String,
    val accessToken: String,
    val expiresAt: Instant,
)
