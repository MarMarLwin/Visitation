package com.onthego.onthegovisitation.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.onthego.onthegovisitation.Common.WebServicesFetchUtils
import com.onthego.onthegovisitation.Models.ToDoMessage
import java.util.concurrent.Executors

class ToDoMessageRepository(application: Application) {
    private var toDoMessages: List<ToDoMessage> = listOf()
    private var mutableLiveData: MutableLiveData<List<ToDoMessage>> = MutableLiveData()

    fun getToDoMessages(companyId: String,deviceId: String?, userId: String, msgPeriod: String): MutableLiveData<List<ToDoMessage>> {
        val service = Executors.newSingleThreadExecutor()
        service.submit {
            toDoMessages = WebServicesFetchUtils.toDoMsgSync(companyId, deviceId, userId, msgPeriod)!!
            mutableLiveData.postValue(toDoMessages)
        }
        return mutableLiveData
    }
}