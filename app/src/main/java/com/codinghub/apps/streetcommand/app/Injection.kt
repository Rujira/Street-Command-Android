package com.codinghub.apps.streetcommand.app

import com.codinghub.apps.streetcommand.models.repository.RemoteRepository
import com.codinghub.apps.streetcommand.models.repository.Repository

object Injection{

    fun provideRepository(): Repository =
        RemoteRepository


}