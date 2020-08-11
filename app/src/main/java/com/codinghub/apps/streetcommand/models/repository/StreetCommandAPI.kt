package com.codinghub.apps.streetcommand.models.repository


import com.codinghub.apps.streetcommand.models.login.LoginRequest
import com.codinghub.apps.streetcommand.models.login.LoginResponse
import com.codinghub.apps.streetcommand.models.userinfo.User
import com.codinghub.apps.streetcommand.models.userinfo.UserInfoResponse
import retrofit2.Call
import retrofit2.http.*

interface StreetCommandAPI {

        @FormUrlEncoded
        @POST("login")
        fun streetCommandLogin(@Field("username") username: String, @Field("password") password: String): Call<LoginResponse>

        @GET("user/information")
        fun getUserInfo(): Call<UserInfoResponse>

}