package com.codinghub.apps.streetcommand.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.codinghub.apps.streetcommand.app.Injection
import com.codinghub.apps.streetcommand.models.error.Either
import com.codinghub.apps.streetcommand.models.preferences.AppPreferences
import com.codinghub.apps.streetcommand.models.userinfo.UserInfoResponse

class MainViewModel(application: Application) : AndroidViewModel(application) {

    fun saveLoginStatus(isUserLoggedIn: Boolean) {
        return AppPreferences.saveLoginStatus(isUserLoggedIn)
    }

    fun removeAccessToken() {
        return AppPreferences.deleteAccessToken()
    }

}