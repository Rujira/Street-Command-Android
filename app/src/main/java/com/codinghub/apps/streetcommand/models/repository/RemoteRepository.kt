package com.codinghub.apps.streetcommand.models.repository

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.codinghub.apps.streetcommand.app.Injection
import com.codinghub.apps.streetcommand.models.alpr.CheckALPRRequest
import com.codinghub.apps.streetcommand.models.alpr.CheckALPRResponse
import com.codinghub.apps.streetcommand.models.alpr.IdentifyALPRRequest
import com.codinghub.apps.streetcommand.models.alpr.IdentifyALPRResponse
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

    override fun checkVehicle(request: CheckALPRRequest): LiveData<Either<CheckALPRResponse>> {

        val liveData = MutableLiveData<Either<CheckALPRResponse>>()
        genericAPI.checkVehicle(request).enqueue(object : Callback<CheckALPRResponse> {

            override fun onResponse(call: Call<CheckALPRResponse>, response: Response<CheckALPRResponse>) {
                if (response.isSuccessful) {
                    liveData.value = Either.success(response.body())
                } else {
                    liveData.value = Either.error(ApiError.CHECKALPR, null)
                }
            }
            override fun onFailure(call: Call<CheckALPRResponse>, t: Throwable) {
                liveData.value = Either.error(ApiError.CHECKALPR, null)
            }
        })
        return liveData
    }

    override fun identifyALPR(request: IdentifyALPRRequest): LiveData<Either<IdentifyALPRResponse>> {

        val liveData = MutableLiveData<Either<IdentifyALPRResponse>>()
        genericAPI.identifyALPR(request).enqueue(object : Callback<IdentifyALPRResponse> {

            override fun onResponse(call: Call<IdentifyALPRResponse>, response: Response<IdentifyALPRResponse>) {
                if (response.isSuccessful) {
                    liveData.value = Either.success(response.body())
                } else {
                    liveData.value = Either.error(ApiError.IDENTIFYALPR, null)
                }
            }
            override fun onFailure(call: Call<IdentifyALPRResponse>, t: Throwable) {
                liveData.value = Either.error(ApiError.IDENTIFYALPR, null)
            }
        })
        return liveData
    }

    @SuppressLint("Recycle")
    override fun modifyOrientation(activity: Activity, bitmap: Bitmap, uri: Uri): Bitmap {

        val columns = arrayOf(MediaStore.MediaColumns.DATA)
        val c = activity.contentResolver.query(uri, columns, null, null, null)
        if (c == null) {
            Log.d("modifyOrientation", "Could not get cursor")
            return bitmap
        }

        c.moveToFirst()
        Log.d("modifyOrientation", c.getColumnName(0))
        val str = c.getString(0)
        if (str == null) {
            Log.d("modifyOrientation", "Could not get exif")
            return bitmap
        }
        Log.d("modifyOrientation", "get cursor");
        val exifInterface = ExifInterface(c.getString(0)!!)
        val exifR : Int = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        val orientation : Float =
            when (exifR) {
                ExifInterface.ORIENTATION_ROTATE_90 ->  90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

        val mat : Matrix? = Matrix()
        mat?.postRotate(orientation)
        return Bitmap.createBitmap(bitmap as Bitmap, 0, 0, bitmap?.width as Int,
            bitmap.height as Int, mat, true)
    }

}