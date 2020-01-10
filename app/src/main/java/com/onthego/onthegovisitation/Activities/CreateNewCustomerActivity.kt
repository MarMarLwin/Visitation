package com.onthego.onthegovisitation

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import androidx.annotation.RequiresApi
import com.crashlytics.android.Crashlytics
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.onthego.onthegovisitation.Common.BaseActivity
import com.onthego.onthegovisitation.Models.Customer
import kotlinx.android.synthetic.main.activity_create_customer.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import java.io.Serializable

/**
 * created by AKThura
 * 24.7.2019
 */
class CreateNewCustomerActivity : BaseActivity(), OnMapReadyCallback,
        GoogleMap.OnMyLocationClickListener
{
    private lateinit var mMap : GoogleMap
    private lateinit var latitude : String
    private lateinit var longitude : String
    private lateinit var customer: Customer
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_customer)
        val actionbar: androidx.appcompat.app.ActionBar? = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.setHomeAsUpIndicator(R.drawable.otglogo)
        val mapFragment =
                supportFragmentManager.findFragmentById(R.id.customerLocationMapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        createCustomerNextButton.setOnClickListener {
            if (! checkIsEmptyOrValidate())
                return@setOnClickListener
            val intent = Intent(this, AddCustomerImageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            doAsync {
                val tempCusID = syncCustomerInfo()
                uiThread {
                    if (tempCusID != "fail")
                    {
//                        resetEditText()
                        setCustomerObj(tempCusID)
                        Utils.showToastMessage(
                                this@CreateNewCustomerActivity,
                                "Successfully Updated!!"
                        )
                        intent.putExtra("cusObj",customer  as Serializable)
                        startActivity(intent)
                        finish()
                    }
                    else
                        Utils.showToastMessage(this@CreateNewCustomerActivity, "Failed")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map : GoogleMap)
    {
        mMap = map

        mMap.isMyLocationEnabled = true
        mMap.setOnMyLocationClickListener(this)
        val currentLocation = LocationServices.getFusedLocationProviderClient(this).lastLocation

        currentLocation.addOnCompleteListener {
            if (it.isSuccessful && it.result != null)
            {
                val myLocation = it.result
                latitude = myLocation !!.latitude.toString()
                longitude = myLocation.longitude.toString()
                mMap.addMarker(
                        MarkerOptions().position(
                                LatLng(
                                        myLocation.latitude,
                                        myLocation.longitude
                                )
                        ).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_standing_man))
                )
                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                                LatLng(myLocation.latitude, myLocation.longitude),
                                15f
                        )
                )
            }
        }
    }

    override fun onMyLocationClick(location : Location)
    {
    }

    //region "custom function"
    @RequiresApi(Build.VERSION_CODES.O)
    fun syncCustomerInfo() : String
    {
        val SOAP_ACTION = Utils.SOAP_NAMESPACE + Utils.CUSTOMER_TEMP_SYNC
        val soapObject = SoapObject(Utils.SOAP_NAMESPACE, Utils.CUSTOMER_TEMP_SYNC)
        val url = GeneralClass.sync_url + Utils.CUSTOMER_URL

        try
        {
            soapObject.addProperty("deviceID", Utils.getPreference(this, GeneralClass.deviceID))
            soapObject.addProperty("companyID", Utils.getPreference(this, GeneralClass.companyID))
            soapObject.addProperty("userID", Utils.getPreference(this, GeneralClass.userName))
            soapObject.addProperty("tempCustomerName", customerNameEditText.text.toString())
            soapObject.addProperty("contactName", customerContactNameEditText.text.toString())
            soapObject.addProperty("addr", customerAddressEditText.text.toString())
            soapObject.addProperty("phone", customerPhoneEditText.text.toString())
            soapObject.addProperty("email", customerEmailEditText.text.toString())
            soapObject.addProperty("website", customerWebsiteEditText.text.toString())
            soapObject.addProperty("gpsPoint", "$latitude,$longitude")
            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
            envelope.implicitTypes = true
            envelope.setOutputSoapObject(soapObject)
            envelope.dotNet = true
            val httpTransportSE = HttpTransportSE(url)
            httpTransportSE.debug = true
            httpTransportSE.call(SOAP_ACTION, envelope)
            return envelope.response.toString()
        }
        catch (ex : Exception)
        {
            Utils.showToastMessage(this, ex.toString())
            Crashlytics.log(ex.toString())
        }
        return "fail"
    }

    private fun resetEditText()
    {
        customerNameEditText.setText("")
        customerContactNameEditText.setText("")
        customerPhoneEditText.setText("")
        customerEmailEditText.setText("")
        customerWebsiteEditText.setText("")
        customerAddressEditText.setText("")
    }

    private fun setCustomerObj(customerId:String)
    {
        customer= Customer(customerId,Utils.getPreference(this,GeneralClass.companyID)!!,
            customerNameEditText.text.toString(),customerContactNameEditText.text.toString(),
            customerAddressEditText.text.toString(),"","","",
            customerPhoneEditText.text.toString(),customerEmailEditText.text.toString(),
            customerWebsiteEditText.text.toString(),"${latitude},${longitude}",
            "true")
    }
    private fun checkIsEmptyOrValidate() : Boolean
    {
        if (customerNameEditText.text.toString().isEmpty())
        {
            customerNameTextInputLayout.error = null
            customerNameTextInputLayout.error = getString(R.string.error_field_required)
            return false
        }
        else
            customerNameTextInputLayout.error = null

        if (customerContactNameEditText.text.toString().isEmpty())
        {
            customerContactNameTextInputLayout.error = null
            customerContactNameTextInputLayout.error = getString(R.string.error_field_required)
            return false
        }
        else
            customerContactNameTextInputLayout.error = null

        if (customerPhoneEditText.text.toString().isEmpty())
        {
            customerPhoneTextInputLayout.error = null
            customerPhoneTextInputLayout.error = getString(R.string.error_field_required)
            return false
        }
        else
            customerPhoneTextInputLayout.error = null

        if (customerEmailEditText.text.toString().isNotEmpty())
        {
            if (! Patterns.EMAIL_ADDRESS.toRegex().matches(customerEmailEditText.text.toString().trim()))
            {
                customerEmailTextInputLayout.error = null
                customerEmailTextInputLayout.error = getString(R.string.invalid_email)
                return false
            }
            else
                customerEmailTextInputLayout.error = null
        }

        if (customerWebsiteEditText.text.toString().isNotEmpty())
        {
            if (! Patterns.WEB_URL.toRegex().matches(customerWebsiteEditText.text.toString().trim()))
            {
                customerWebsiteTextInputLayout.error = null
                customerWebsiteTextInputLayout.error = getString(R.string.invalid_domain)
                return false
            }
            else customerWebsiteTextInputLayout.error = null
        }

        return true
    }
    //endregion
}