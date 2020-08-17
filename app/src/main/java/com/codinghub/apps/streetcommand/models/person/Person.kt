package com.codinghub.apps.streetcommand.models.person

data class Person(val fullname: String,
                  val citizen_id: String,
                  val person_type: String,
                  val image_search: String,
                  val image_match: String,
                  val similarity: Double,
                  val remark: String)