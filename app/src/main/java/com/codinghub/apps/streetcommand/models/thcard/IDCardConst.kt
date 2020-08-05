package com.codinghub.apps.streetcommand.models.thcard

class IDCardConst {
    var ID_CARD_DATA = arrayOf(REQ_CID, REQ_THAI_NAME, REQ_ENG_NAME, REQ_GENDER, REQ_DOB, REQ_ADDRESS, REQ_ISSUE_EXPIRE)
    var ID_CARD_DATA1 = arrayOf(REQ_CID, REQ_INFO, REQ_ADDRESS, REQ_ISSUE_EXPIRE)
    var ID_CARD_DESC = arrayOf("REQ_CID", "REQ_THAI_NAME", "REQ_ENG_NAME", "REQ_GENDER", "REQ_DOB", "REQ_ADDRESS", "REQ_ISSUE_EXPIRE")
    private val get_response = GET_RESPONSE
    private val CMD_PHOTO1 = arrayOf<String>()
    private val CMD_PHOTO2 = arrayOf<String>()
    private val TH_BYTE = 256

    companion object {
        val SELECT = "00a4040008"
        val THAI_ID_CARD = "a000000054480001"
        val GET_RESPONSE = "00c00000"
        val GET_RESPONSE1 = "00c00001"
        val REQ_CID = "80b0000402000d"
        val GET_CID = "00c000000d"
        val REQ_THAI_NAME = "80b00011020064"
        val REQ_ENG_NAME = "80b00075020064"
        val REQ_GENDER = "80b000e1020001"
        val REQ_DOB = "80b000d9020008"
        val REQ_ADDRESS = "80b01579020064"
        val REQ_ISSUE_EXPIRE = "80b00167020012"
        val REQ_INFO = "80b000110200d1" // Fullname Thai + Eng + BirthDate + Sex
        val GET_INFO = "00c00000d1"
        val REQ_PHOTO = ""
    }
}