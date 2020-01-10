package com.onthego.onthegovisitation.Models

data class ToDoMessage(
        var ToDoID : String,
        var CompanyID : String,
        var AssignByUserID : String,
        var AssignToUserID : String,
        var ToDoMsg : String,
        var CompleteMessage : String? = null,
        var ToDoStatus : String,
        var AssignDateTime : String,
        var CompleteDateTime : String? = null
)