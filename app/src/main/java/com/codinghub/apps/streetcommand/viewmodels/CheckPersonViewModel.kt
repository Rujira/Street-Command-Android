package com.codinghub.apps.streetcommand.viewmodels

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.codinghub.apps.streetcommand.app.Injection
import com.codinghub.apps.streetcommand.models.error.Either
import com.codinghub.apps.streetcommand.models.person.CheckPersonRequest
import com.codinghub.apps.streetcommand.models.person.CheckPersonResponse
import com.codinghub.apps.streetcommand.models.preferences.AppPreferences

class CheckPersonViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Injection.provideRepository()

    fun checkPerson(citizen_id: String,
                    fullname: String,
                    latitude: Double?,
                    longitude: Double?,
                    address: String?,
                    remark: String?,
                    image: String?,
                    search_type: Int) : LiveData<Either<CheckPersonResponse>> {

        val request = CheckPersonRequest(citizen_id, fullname, latitude, longitude, address, remark, image, search_type)
        return repository.checkPerson(request)

    }

    fun getSnackbarsDuration(): Int {
        return  AppPreferences.getSnackbarsDuration()
    }

    fun modifyOrientation(activity: Activity, bitmap: Bitmap, uri: Uri): Bitmap {
        return repository.modifyOrientation(activity, bitmap, uri)
    }
}