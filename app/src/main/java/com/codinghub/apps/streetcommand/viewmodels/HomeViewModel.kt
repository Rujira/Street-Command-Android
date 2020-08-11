package com.codinghub.apps.streetcommand.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.codinghub.apps.streetcommand.app.Injection
import com.codinghub.apps.streetcommand.models.error.Either
import com.codinghub.apps.streetcommand.models.userinfo.UserInfoResponse

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Injection.provideRepository()

    fun getUserInfo(): LiveData<Either<UserInfoResponse>> {
        return repository.getUserInfo()
    }
}