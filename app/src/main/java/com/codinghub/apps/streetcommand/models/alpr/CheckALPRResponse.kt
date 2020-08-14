package com.codinghub.apps.streetcommand.models.alpr

data class CheckALPRResponse(val ret: Int,
                             val msg: String,
                             val alpr: Vehicle)