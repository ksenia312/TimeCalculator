package com.xenikii.timecalculator.data.mapper

import com.xenikii.timecalculator.domain.model.User
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo

fun UserInfo.mapToUser(): User {
    return User(
        id = this.id,
        email = this.email,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun SessionStatus.mapToUser(): User? {
    return when (this) {
        is SessionStatus.Authenticated -> this.session.user?.mapToUser()
        else -> null
    }
}