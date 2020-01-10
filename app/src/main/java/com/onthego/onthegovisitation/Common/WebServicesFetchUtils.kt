package com.onthego.onthegovisitation.Common

import android.util.Log
import com.crashlytics.android.Crashlytics
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.onthego.onthegovisitation.GeneralClass
import com.onthego.onthegovisitation.Models.Customer
import com.onthego.onthegovisitation.Models.ToDoMessage
import com.onthego.onthegovisitation.Utils
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE

object WebServicesFetchUtils {

    private val LOG_TAG = WebServicesFetchUtils::class.java.simpleName

    fun toDoMsgSync(
        companyId: String,
        deviceId: String?,
        userId: String,
        msgPeriod: String
    ): List<ToDoMessage>? {
        val SOAP_ACTION = Utils.SOAP_NAMESPACE + Utils.TODO_SYNC
        val soapObject = SoapObject(Utils.SOAP_NAMESPACE, Utils.TODO_SYNC)
        val url = GeneralClass.sync_url + Utils.TODO_MSG_URL

        try {
            soapObject.addProperty("companyID", companyId)
            soapObject.addProperty("deviceID", deviceId)
            soapObject.addProperty("userID", userId)
            soapObject.addProperty("msgPeriod", msgPeriod)
            val gson = GsonBuilder().create()
            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
            envelope.implicitTypes = true
            envelope.setOutputSoapObject(soapObject)
            envelope.dotNet = true
            val httpTransportSE = HttpTransportSE(url)
            httpTransportSE.debug = true
            httpTransportSE.call(SOAP_ACTION, envelope)
            val objectResult = envelope.response.toString()
            val collectionType = object : TypeToken<Collection<ToDoMessage>>() {}.type
            return gson.fromJson(objectResult, collectionType)
        } catch (ex: Exception) {
            Log.e(LOG_TAG, ex.toString())
            Utils.makeLog(ex.toString())
            Crashlytics.log(ex.toString())
        }
        return null
    }

    fun syncCustomerList(companyId: String, deviceId: String?, userId: String): List<Customer>? {
        val SOAP_ACTION = Utils.SOAP_NAMESPACE + Utils.CUSTOMER_SYNC
        val soapObject = SoapObject(
            Utils.SOAP_NAMESPACE,
            Utils.CUSTOMER_SYNC
        )
        val url = GeneralClass.sync_url + Utils.CUSTOMER_URL
        try {

            soapObject.addProperty(
                "companyID", companyId
            )
            soapObject.addProperty(
                "deviceID", deviceId
            )
            soapObject.addProperty(
                "userID", userId
            )
            val gson = GsonBuilder().create()
            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
            envelope.implicitTypes = true
            envelope.setOutputSoapObject(soapObject)
            envelope.dotNet = true
            val httpTransportSE = HttpTransportSE(url)
            httpTransportSE.debug = true
            httpTransportSE.call(SOAP_ACTION, envelope)
            val objectResult = envelope.response.toString()
            val collectionType = object : TypeToken<Collection<Customer>>() {}.type
            return gson.fromJson(objectResult, collectionType)


        } catch (ex: java.lang.Exception) {
            Utils.makeLog(ex.toString())
            Crashlytics.log(ex.toString())

        }
        return null
    }

    fun syncNewCustomerList(companyId: String, deviceId: String?, userId: String): List<Customer>? {
        val SOAP_ACTION = Utils.SOAP_NAMESPACE + Utils.NEWCUST_SYNC
        val soapObject = SoapObject(
            Utils.SOAP_NAMESPACE,
            Utils.CUSTOMER_SYNC
        )
        val url = GeneralClass.sync_url + Utils.CUSTOMER_URL
        try {

            soapObject.addProperty(
                "companyID", companyId
            )
            soapObject.addProperty(
                "deviceID", deviceId
            )
            soapObject.addProperty(
                "userID", userId
            )
            val gson = GsonBuilder().create()
            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
            envelope.implicitTypes = true
            envelope.setOutputSoapObject(soapObject)
            envelope.dotNet = true
            val httpTransportSE = HttpTransportSE(url)
            httpTransportSE.debug = true
            httpTransportSE.call(SOAP_ACTION, envelope)
            val objectResult = envelope.response.toString()
            val collectionType = object : TypeToken<Collection<Customer>>() {}.type
            return gson.fromJson(objectResult, collectionType)


        } catch (ex: java.lang.Exception) {
            Utils.makeLog(ex.toString())
            Crashlytics.log(ex.toString())

        }
        return null
    }
}