package com.onthego.onthegovisitation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.onthego.onthegovisitation.Models.ToDoMessage
import com.onthego.onthegovisitation.repository.ToDoMessageRepository

class ToDoMessageViewModel(application: Application) : AndroidViewModel(application) {
    private var toDoMsgRepository: ToDoMessageRepository = ToDoMessageRepository(application)

    fun getToDoMessages(companyId: String,deviceId: String?, userId: String, msgPeriod: String): LiveData<List<ToDoMessage>> {
        return toDoMsgRepository.getToDoMessages(companyId,deviceId, userId, msgPeriod)
    }
}