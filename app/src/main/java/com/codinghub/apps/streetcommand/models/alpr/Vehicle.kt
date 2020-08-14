package com.codinghub.apps.streetcommand.models.alpr

data class Vehicle(val plate: String,
                   val province: String,
                   val description: List<String>,
                   val alpr_type: String)
