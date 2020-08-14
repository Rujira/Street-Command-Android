package com.codinghub.apps.streetcommand.models.alpr

data class IdentifyALPRRequest(val image: String,
                               val latitude: Double?,
                               val longitude: Double?,
                               val address: String?)
