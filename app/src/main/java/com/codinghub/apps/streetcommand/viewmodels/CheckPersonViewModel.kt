package com.codinghub.apps.streetcommand.viewmodels

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.codinghub.apps.streetcommand.app.Injection

class CheckPersonViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Injection.provideRepository()

    fun modifyOrientation(activity: Activity, bitmap: Bitmap, uri: Uri): Bitmap {
        return repository.modifyOrientation(activity, bitmap, uri)
    }
}