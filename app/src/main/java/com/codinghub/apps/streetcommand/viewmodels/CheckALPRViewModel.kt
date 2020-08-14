package com.codinghub.apps.streetcommand.viewmodels

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.codinghub.apps.streetcommand.app.Injection
import com.codinghub.apps.streetcommand.models.alpr.CheckALPRRequest
import com.codinghub.apps.streetcommand.models.alpr.CheckALPRResponse
import com.codinghub.apps.streetcommand.models.error.Either

class CheckALPRViewModel (application: Application) : AndroidViewModel(application) {

    private val repository = Injection.provideRepository()

    fun checkVehicle(plate: String, province: String, latitude: Double?, longitude: Double?, address: String?, remark: String?, image: String?) : LiveData<Either<CheckALPRResponse>> {

        val request = CheckALPRRequest(plate, province, latitude, longitude, address, remark, image)
        return repository.checkVehicle(request)
    }

    fun modifyOrientation(activity: Activity, bitmap: Bitmap, uri: Uri): Bitmap {
        return repository.modifyOrientation(activity, bitmap, uri)
    }
}