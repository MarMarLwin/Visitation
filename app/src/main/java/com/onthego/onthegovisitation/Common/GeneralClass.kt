package com.onthego.onthegovisitation

import android.graphics.Color

class GeneralClass
{
    companion object
    {
        //Login Screen Related
        const val deviceID = "Device ID"
        const val companyID = "Company ID"
        const val userName = "User Name"
        const val isTempCustomer="IsTempCustomer"
        const val userPassword = "User Password"
        const val checkInId="CheckInID"
        const val checkInCustomer="CheckInCustomer"
        const val checkInTime = "Check In Time"
        const val checkOutTime = "Check Out Time"
        const val selectedLanguage = "USER_LANGUAGE"
        const val selectedLanguageID="0"
        const val sync_url = "http://192.168.1.181/visitationws/"//"http://www.onthego.cloud/visitationws/"
        const val log = "LOG"
    }

    enum class TODOStatus(val status : String)
    {
        Complete("D"),
        FollowUp("F"),
        Read("R"),
        None("N")
    }

    enum class TODOStatusImage(val image : Int)
    {
        Complete(R.drawable.ic_task_complete),
        FollowUp(R.drawable.ic_task_follow),
    }

    enum class TODOStatusText(val text : String)
    {
        Complete("Done"),
        FollowUp("Followup"),
        Read("Seen"),
    }

    enum class TODOStatusColor(val color : Int)
    {
        Complete(Color.parseColor("#2e7d32")),
        FollowUp(Color.parseColor("#f1c40f")),
        Read(Color.parseColor("#3F51B5")),
    }

    enum class TODOMsgPeriod(val period:String){
        Past("P"),
        Today("C"),
        Future("F")
    }
}