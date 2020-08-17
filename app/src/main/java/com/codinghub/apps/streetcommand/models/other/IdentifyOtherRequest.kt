package com.codinghub.apps.streetcommand.models.other

data class IdentifyOtherRequest(val image: String,
                                val latitude: Double?,
                                val longitude: Double?,
                                val address: String?,
                                val remark: String?)
