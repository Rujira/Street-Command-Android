package com.codinghub.apps.streetcommand.models.repository

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import com.codinghub.apps.streetcommand.models.alpr.IdentifyALPRRequest
import com.codinghub.apps.streetcommand.models.alpr.IdentifyALPRResponse
import com.codinghub.apps.streetcommand.models.alpr.CheckALPRRequest
import com.codinghub.apps.streetcommand.models.alpr.CheckALPRResponse
import com.codinghub.apps.streetcommand.models.error.Either
import com.codinghub.apps.streetcommand.models.login.LoginRequest
import com.codinghub.apps.streetcommand.models.login.LoginResponse
import com.codinghub.apps.streetcommand.models.person.IdentifyPersonRequest
import com.codinghub.apps.streetcommand.models.person.IdentifyPersonResponse
import com.codinghub.apps.streetcommand.models.userinfo.UserInfoResponse

interface Repository {

    fun streetCommandLogin(request: LoginRequest): LiveData<Either<LoginResponse>>
    fun getUserInfo(): LiveData<Either<UserInfoResponse>>

    //Check Vehicle
    fun checkVehicle(request: CheckALPRRequest): LiveData<Either<CheckALPRResponse>>
    fun modifyOrientation(activity: Activity, bitmap: Bitmap, uri: Uri): Bitmap

    //Camera Check ALPR
    fun identifyALPR(request: IdentifyALPRRequest): LiveData<Either<IdentifyALPRResponse>>
    fun identifyPerson(request: IdentifyPersonRequest): LiveData<Either<IdentifyPersonResponse>>

}