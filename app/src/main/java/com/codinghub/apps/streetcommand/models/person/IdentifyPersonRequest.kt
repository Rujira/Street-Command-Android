package com.codinghub.apps.streetcommand.models.person

data class IdentifyPersonRequest(val image: String,
                                 val latitude: Double?,
                                 val longitude: Double?,
                                 val address: String?)