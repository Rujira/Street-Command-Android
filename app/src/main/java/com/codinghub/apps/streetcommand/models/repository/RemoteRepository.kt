package com.codinghub.apps.streetcommand.models.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.codinghub.apps.streetcommand.app.Injection
import com.codinghub.apps.streetcommand.models.error.ApiError
import com.codinghub.apps.streetcommand.models.error.Either
import com.codinghub.apps.streetcommand.models.login.LoginRequest
import com.codinghub.apps.streetcommand.models.login.LoginResponse
import com.codinghub.apps.streetcommand.models.userinfo.UserInfoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object RemoteRepository : Repository {

    private val loginAPI = Injection.provideStreetCommandLoginAPI()
    private val genericAPI = Injection.provideStreetCommandAPI()

    private val TAG = RemoteRepository::class.qualifiedName

    override fun streetCommandLogin(request: LoginRequest): LiveData<Either<LoginResponse>> {

        val liveData = MutableLiveData<Either<LoginResponse>>()
        loginAPI.streetCommandLogin(request.username, request.password).enqueue(object : Callback<LoginResponse> {

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    liveData.value = Either.success(response.body())
                } else {
                    liveData.value = Either.error(ApiError.LOGIN, null)
                }
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                liveData.value = Either.error(ApiError.LOGIN, null)
            }
        })
        return liveData
    }

    override fun getUserInfo(): LiveData<Either<UserInfoResponse>> {

        val liveData = MutableLiveData<Either<UserInfoResponse>>()
        genericAPI.getUserInfo().enqueue(object : Callback<UserInfoResponse> {

            override fun onResponse(call: Call<UserInfoResponse>, response: Response<UserInfoResponse>) {
                if (response.isSuccessful) {
                    liveData.value = Either.success(response.body())
                } else {
                    liveData.value = Either.error(ApiError.USERINFO, null)
                }
            }

            override fun onFailure(call: Call<UserInfoResponse>, t: Throwable) {
                liveData.value = Either.error(ApiError.USERINFO, null)
            }
        })
        return  liveData
    }
}