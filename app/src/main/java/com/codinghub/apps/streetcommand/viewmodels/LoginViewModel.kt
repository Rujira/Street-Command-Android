package com.codinghub.apps.streetcommand.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.codinghub.apps.streetcommand.app.Injection
import com.codinghub.apps.streetcommand.models.error.Either
import com.codinghub.apps.streetcommand.models.login.LoginRequest
import com.codinghub.apps.streetcommand.models.login.LoginResponse
import com.codinghub.apps.streetcommand.models.preferences.AppPreferences

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Injection.provideRepository()

    fun streetCommandLogin(username: String, password: String): LiveData<Either<LoginResponse>> {
        val request = LoginRequest(username, password)
        return repository.streetCommandLogin(request)
    }

    fun saveAccessToken(accessToken: String) {
        return AppPreferences.saveAccessToken(accessToken)
    }

    fun saveLoginStatus(isUserLoggedIn: Boolean) {
        return AppPreferences.saveLoginStatus(isUserLoggedIn)
    }

    fun getLoginStatus(): Boolean {
        return AppPreferences.getLoginStatus()
    }
}