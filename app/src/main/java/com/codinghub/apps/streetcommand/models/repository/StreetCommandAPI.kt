package com.codinghub.apps.streetcommand.models.repository


import com.codinghub.apps.streetcommand.models.alpr.CheckALPRRequest
import com.codinghub.apps.streetcommand.models.alpr.CheckALPRResponse
import com.codinghub.apps.streetcommand.models.alpr.IdentifyALPRRequest
import com.codinghub.apps.streetcommand.models.alpr.IdentifyALPRResponse
import com.codinghub.apps.streetcommand.models.login.LoginRequest
import com.codinghub.apps.streetcommand.models.login.LoginResponse
import com.codinghub.apps.streetcommand.models.person.IdentifyPersonRequest
import com.codinghub.apps.streetcommand.models.person.IdentifyPersonResponse
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

        @Headers("Accept: application/json")
        @POST("check/alpr")
        fun checkVehicle(@Body body: CheckALPRRequest): Call<CheckALPRResponse>

        @Headers("Accept: application/json")
        @POST("identify/alpr")
        fun identifyALPR(@Body body: IdentifyALPRRequest): Call<IdentifyALPRResponse>

        @Headers("Accept: application/json")
        @POST("identify/person")
        fun identifyPerson(@Body body: IdentifyPersonRequest): Call<IdentifyPersonResponse>

}