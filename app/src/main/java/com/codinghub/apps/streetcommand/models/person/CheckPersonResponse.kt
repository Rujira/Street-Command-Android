package com.codinghub.apps.streetcommand.models.person

data class CheckPersonResponse(val ret: Int,
                                val msg: String,
                                val person: Person)