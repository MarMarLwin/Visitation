package com.onthego.onthegovisitation.Common

import android.content.Context
import android.content.IntentSender
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes

abstract class BaseActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{
    override fun attachBaseContext(newBase : Context?)
    {
        super.attachBaseContext(LocaleHelper.onAttach(newBase !!))
    }

    fun enableGPS()
    {
        val googleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build()
        googleApiClient.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5 * 1000
        locationRequest.fastestInterval = 2 * 1000
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        builder.setAlwaysShow(true)
        val result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback {
            val status = it.status

            when (status.statusCode)
            {
                LocationSettingsStatusCodes.SUCCESS ->
                    return@setResultCallback
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                    try
                    {
                        status.startResolutionForResult(this, 1000)
                    }
                    catch (ex : IntentSender.SendIntentException)
                    {
                        Crashlytics.log(ex.toString())
                    }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                    return@setResultCallback
            }
        }
    }

    override fun onConnected(p0 : Bundle?)
    {
    }

    override fun onConnectionSuspended(p0 : Int)
    {
    }

    override fun onConnectionFailed(p0 : ConnectionResult)
    {
    }
}