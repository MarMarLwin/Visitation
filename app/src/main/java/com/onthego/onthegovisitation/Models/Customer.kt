package com.onthego.onthegovisitation.Models

import java.io.Serializable

data class Customer(
        var CustomerID : String,
        var CompanyCode: String,
        var CustomerName : String,
        var ContactName :String,
        var Addr :String,
        var PostalCode :String,
        var City : String,
        var Country :String,
        var Phone :String,
        var Email :String,
        var Website :String,
        var GpsPoint :String,
        var IsActive:String
):Serializable