package com.codinghub.apps.streetcommand.models.preferences

import androidx.preference.PreferenceManager
import com.codinghub.apps.streetcommand.app.StreetCommandApplication
import com.google.gson.Gson

object AppPreferences {

    private const val KEY_SERVICE_URL = "KEY_SERVICE_URL"
    private const val KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN"
    private const val KEY_IS_USER_LOGGED_IN = "KEY_IS_USER_LOGGED_IN"
    private const val KEY_SNACKBARS_DURATION = "KEY_SNACKBARS_DURATION"

    private val gson = Gson()

    private fun sharedPrefs() = PreferenceManager.getDefaultSharedPreferences(StreetCommandApplication.getAppContext())

    fun getServiceURL(): String? = sharedPrefs().getString(KEY_SERVICE_URL, "http://115.31.144.251:9102/")

    fun saveAccessToken(accessToken: String) {
        sharedPrefs().edit().putString(KEY_ACCESS_TOKEN, accessToken).apply()
    }
    fun getAccessToken(): String? = sharedPrefs().getString(KEY_ACCESS_TOKEN, "invalid_token")
    fun deleteAccessToken() {
        sharedPrefs().edit().remove(KEY_ACCESS_TOKEN).apply()
    }

    fun saveLoginStatus(isUserLoggedIn: Boolean) {
        sharedPrefs().edit().putBoolean(KEY_IS_USER_LOGGED_IN, isUserLoggedIn).apply()
    }

    fun getLoginStatus(): Boolean = sharedPrefs().getBoolean(KEY_IS_USER_LOGGED_IN, false)

    fun getSnackbarsDuration(): Int = sharedPrefs().getInt(KEY_SNACKBARS_DURATION, 5000)

}