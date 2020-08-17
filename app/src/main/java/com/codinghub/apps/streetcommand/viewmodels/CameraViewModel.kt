package com.codinghub.apps.streetcommand.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.codinghub.apps.streetcommand.app.Injection
import com.codinghub.apps.streetcommand.models.alpr.IdentifyALPRRequest
import com.codinghub.apps.streetcommand.models.alpr.IdentifyALPRResponse
import com.codinghub.apps.streetcommand.models.error.Either
import com.codinghub.apps.streetcommand.models.other.IdentifyOtherRequest
import com.codinghub.apps.streetcommand.models.other.IdentifyOtherResponse
import com.codinghub.apps.streetcommand.models.person.IdentifyPersonRequest
import com.codinghub.apps.streetcommand.models.person.IdentifyPersonResponse
import com.codinghub.apps.streetcommand.models.preferences.AppPreferences

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Injection.provideRepository()

    fun identifyALPR(image: String, latitude: Double?, longitude: Double?, address: String?) : LiveData<Either<IdentifyALPRResponse>> {

        val request = IdentifyALPRRequest(image, latitude, longitude, address)
        return repository.identifyALPR(request)
    }

    fun identifyPerson(image: String, latitude: Double?, longitude: Double?, address: String?) : LiveData<Either<IdentifyPersonResponse>> {

        val request = IdentifyPersonRequest(image, latitude, longitude, address)
        return repository.identifyPerson(request)
    }

    fun identifyEnvironment(image: String, latitude: Double?, longitude: Double?, address: String?, remark: String?) : LiveData<Either<IdentifyOtherResponse>> {

        val request = IdentifyOtherRequest(image, latitude, longitude, address, remark)
        return repository.identifyEnvironment(request)
    }

    fun getSnackbarsDuration(): Int {
        return  AppPreferences.getSnackbarsDuration()
    }

}


