package com.codinghub.apps.streetcommand.models.person

data class IdentifyPersonResponse(val ret: Int,
                                  val msg: String,
                                  val person: Person)