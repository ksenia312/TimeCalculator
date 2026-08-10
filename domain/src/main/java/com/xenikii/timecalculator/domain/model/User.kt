package com.xenikii.timecalculator.domain.model

import kotlin.time.Instant

data class User(
    val id: String,
    val email: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)