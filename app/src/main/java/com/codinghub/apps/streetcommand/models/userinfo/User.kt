package com.codinghub.apps.streetcommand.models.userinfo

data class User(val user_id: Int,
                val username: String,
                val user_type: String,
                val full_name: String,
                val profile_picture: String?,
                val work_description: String?,
                val area_of_responsibility: String?)
