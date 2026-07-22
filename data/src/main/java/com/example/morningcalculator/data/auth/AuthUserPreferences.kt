package com.example.morningcalculator.data.auth

import android.content.Context

class AuthUserPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLastUserId(): String? = prefs.getString(KEY_LAST_USER_ID, null)

    fun setLastUserId(userId: String?) {
        prefs.edit().putString(KEY_LAST_USER_ID, userId).apply()
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_LAST_USER_ID = "last_user_id"
    }
}
