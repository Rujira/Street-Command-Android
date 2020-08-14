package com.codinghub.apps.streetcommand.models.alpr

data class IdentifyALPRResponse(val ret: Int,
                                val msg: String,
                                val alpr: Vehicle)