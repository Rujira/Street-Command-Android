package com.codinghub.apps.streetcommand.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.codinghub.apps.streetcommand.app.Injection
import com.codinghub.apps.streetcommand.models.alpr.IdentifyALPRRequest
import com.codinghub.apps.streetcommand.models.alpr.IdentifyALPRResponse
import com.codinghub.apps.streetcommand.models.error.Either

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Injection.provideRepository()

    fun identifyALPR(image: String, latitude: Double?, longitude: Double?, address: String?) : LiveData<Either<IdentifyALPRResponse>> {

        val request = IdentifyALPRRequest(image, latitude, longitude, address)
        return repository.identifyALPR(request)
    }

}


