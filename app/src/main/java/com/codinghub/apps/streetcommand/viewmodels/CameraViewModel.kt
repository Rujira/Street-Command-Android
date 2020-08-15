package com.codinghub.apps.streetcommand.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.codinghub.apps.streetcommand.app.Injection
import com.codinghub.apps.streetcommand.models.alpr.IdentifyALPRRequest
import com.codinghub.apps.streetcommand.models.alpr.IdentifyALPRResponse
import com.codinghub.apps.streetcommand.models.error.Either
import com.codinghub.apps.streetcommand.models.person.IdentifyPersonRequest
import com.codinghub.apps.streetcommand.models.person.IdentifyPersonResponse

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

}


