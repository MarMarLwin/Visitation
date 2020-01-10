package com.onthego.onthegovisitation.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.onthego.onthegovisitation.Common.WebServicesFetchUtils
import com.onthego.onthegovisitation.Models.Customer
import java.util.concurrent.Executors

class CustomerRepository(application: Application) {
    private var customerList: List<Customer> = listOf()
    private var mutableLiveData: MutableLiveData<List<Customer>> = MutableLiveData()

    fun getCustomerList(companyId: String,deviceId: String?, userId: String): MutableLiveData<List<Customer>> {
        val service = Executors.newSingleThreadExecutor()
        service.submit {
            customerList = WebServicesFetchUtils.syncCustomerList(companyId, deviceId, userId)!!
            mutableLiveData.postValue(customerList.sortedBy{x -> x.CustomerID})
        }
        return mutableLiveData
    }

    fun getNewCustomerList(companyId: String,deviceId: String?, userId: String): MutableLiveData<List<Customer>> {
        val service = Executors.newSingleThreadExecutor()
        service.submit {
            customerList = WebServicesFetchUtils.syncNewCustomerList(companyId, deviceId, userId)!!
            mutableLiveData.postValue(customerList)
        }
        return mutableLiveData
    }
}