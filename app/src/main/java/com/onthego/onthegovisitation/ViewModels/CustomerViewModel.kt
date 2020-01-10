package com.onthego.onthegovisitation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.onthego.onthegovisitation.Models.Customer
import com.onthego.onthegovisitation.repository.CustomerRepository

class CustomerViewModel(application: Application): AndroidViewModel(application) {
    private var customerRepository: CustomerRepository = CustomerRepository(application)

    fun getCustomerList(companyId: String,deviceId: String?, userId: String): LiveData<List<Customer>> {
        return customerRepository.getCustomerList(companyId,deviceId, userId)
    }
}