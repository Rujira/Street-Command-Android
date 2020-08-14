package com.codinghub.apps.streetcommand.models.alpr

data class CheckALPRRequest(val plate: String,
                            val province: String,
                            val latitude: Double?,
                            val longitude: Double?,
                            val address: String?,
                            val remark: String?,
                            val image: String?)