package com.onthego.onthegovisitation.Models

import androidx.room.Entity

@Entity(tableName = "BackgroundGPS", primaryKeys = ["BackgroundGpsID"])
data class BackgroundGPS (
    var BackgroundGpsID:String,
    var DeviceID :String,
    var CompanyCode :String,
    var UserID:String,
    var GpsPoint:String,
    var CapturedDateTime:String
)
