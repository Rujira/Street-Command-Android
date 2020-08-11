package com.codinghub.apps.streetcommand.models.repository

import androidx.lifecycle.LiveData
import com.codinghub.apps.streetcommand.models.error.Either
import com.codinghub.apps.streetcommand.models.login.LoginRequest
import com.codinghub.apps.streetcommand.models.login.LoginResponse
import com.codinghub.apps.streetcommand.models.userinfo.UserInfoResponse

interface Repository {

    fun streetCommandLogin(request: LoginRequest): LiveData<Either<LoginResponse>>
    fun getUserInfo(): LiveData<Either<UserInfoResponse>>
}