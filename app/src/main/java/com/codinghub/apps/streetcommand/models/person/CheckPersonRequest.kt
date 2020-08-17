package com.codinghub.apps.streetcommand.models.person

data class CheckPersonRequest (val citizen_id: String,
                               val fullname: String,
                               val latitude: Double?,
                               val longitude: Double?,
                               val address: String?,
                               val remark: String?,
                               val image: String?,
                               val search_type: Int)


