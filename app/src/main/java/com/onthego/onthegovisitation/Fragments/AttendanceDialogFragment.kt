package com.onthego.onthegovisitation.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.crashlytics.android.Crashlytics
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.onthego.onthegovisitation.*
import com.onthego.onthegovisitation.Common.LocaleHelper
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE

class AttendanceDialogFragment : androidx.fragment.app.DialogFragment(), OnMapReadyCallback
{
    private lateinit var map : GoogleMap
    private var mapView : View? = null
    private lateinit var mapFragment : SupportMapFragment
    private var deviceID : String? = null
    private var companyID : String? = null
    private var userName : String? = null
    private var attendanceResult : Boolean = true
    private var attendanceTime : String? = null
    private var currentLocation : Location? = null
    //for language change
    override fun onAttach(context : Context)
    {
        super.onAttach(LocaleHelper.onAttach(context))
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
            inflater : LayoutInflater,
            container : ViewGroup?,
            savedInstanceState : Bundle?
    ) : View?
    {
        try
        {
            deviceID = Utils.getPreference(
                    context !!.applicationContext,
                    GeneralClass.deviceID
            )
            companyID = Utils.getPreference(
                    context !!.applicationContext,
                    GeneralClass.companyID
            )
            userName = Utils.getPreference(
                    context !!.applicationContext,
                    GeneralClass.userName
            )

            if (mapView != null)
            {
                val parent = mapView !!.parent as ViewGroup
                parent.removeView((mapView))
            }
            mapView = inflater.inflate(R.layout.attendance_fragment, container, false)

            mapFragment =
                    activity !!.supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
            val attendanceButton = mapView !!.findViewById<Button>(R.id.attendanceButton)
            val attendanceComment = mapView !!.findViewById<EditText>(R.id.attendanceComment)

            attendanceButton.text = this.tag

            if (attendanceButton.text == resources.getString(R.string.attendance_out))
            {
                attendanceButton.setBackgroundResource(R.drawable.round_button)
            }

            attendanceButton.setOnClickListener {
                val progressDialog = Utils.makeProgressDialog(
                        context !!,
                        getString(R.string.loading_msg)
                )
                when
                {
                    (attendanceButton.text == resources.getString(R.string.attendance_in))
                    ->
                    {
                        progressDialog.show()
                        val intent = Intent(context, MainActivity::class.java)
                        doAsync {
                            attendanceTime = Utils.getServerTime(Utils.DATETIME_FORMAT)
                            intent.putExtra(GeneralClass.checkInTime, attendanceTime)
                            syncAttendance(
                                    Utils.ATTENDANCE_IN,
                                    attendanceTime !!,
                                    currentLocation !!,
                                    attendanceComment.text.toString()
                            )
                            uiThread {
//                                if(attendanceResult)
//                                {
                                //tempory comment
                                    startService()
                                    startActivity(intent)
                                    fragmentManager !!.beginTransaction()
                                        .remove(this@AttendanceDialogFragment).commit()
                                    progressDialog.dismiss()
//                                }

                            }
                        }
                    }
                    else ->
                    {
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle(getString(R.string.logout_alert_title))
                        builder.setMessage(getString(R.string.attendance_out_alert))
                        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                            progressDialog.show()
                            doAsync {
                                attendanceTime = Utils.getServerTime(Utils.DATETIME_FORMAT)
                                syncAttendance(
                                        Utils.ATTENDANCE_OUT,
                                        attendanceTime !!,
                                        currentLocation !!,
                                        attendanceComment.text.toString()
                                )
                                uiThread {
                                    fragmentManager !!.beginTransaction()
                                            .remove(this@AttendanceDialogFragment).commit()
                                    stopService()
                                    Utils.savePreference(
                                            context !!.applicationContext,
                                            GeneralClass.checkOutTime,
                                            attendanceTime
                                    )
                                    progressDialog.dismiss()
                                }
                                activity !!.finish()
                            }
                        }
                        builder.setNegativeButton(getString(R.string.no)) { _, _ ->
                            progressDialog.dismiss()
                        }
                        builder.create()
                        builder.setCancelable(false)
                        builder.show()
                    }
                }
            }
            return mapView
        }
        catch (e : Exception)
        {
            Log.i("Log", e.toString())
            Crashlytics.log(e.toString())
            return null
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap : GoogleMap)
    {
        map = googleMap
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
        map.isMyLocationEnabled = true
        val location = LocationServices.getFusedLocationProviderClient(this.activity !!).lastLocation
        location.addOnCompleteListener(this.activity !!)
        { task ->
            if (task.isSuccessful && task.result != null)
            {
                currentLocation = task.result
                map.addMarker(
                        MarkerOptions().position(
                                LatLng(
                                        currentLocation !!.latitude,
                                        currentLocation !!.longitude
                                )
                        ).icon(
                                BitmapDescriptorFactory.fromResource(
                                        R.drawable.ic_standing_man
                                )
                        )
                )
                map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                                LatLng(currentLocation !!.latitude, currentLocation !!.longitude),
                                16.0f
                        )
                )
                map.animateCamera(CameraUpdateFactory.zoomTo(16.0f))
            }
        }
    }

    override fun onDestroyView()
    {
        super.onDestroyView()

        fragmentManager !!.beginTransaction().remove(mapFragment).commit()
    }

    private fun startService()
    {
        try
        {
            val serviceIntent = Intent(context !!.applicationContext, VisitationServices::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ContextCompat.startForegroundService(context !!.applicationContext, serviceIntent)
            else
                context !!.startService(serviceIntent)
        }
        catch (ex : Exception)
        {
            Toast.makeText(context !!.applicationContext, ex.toString(), Toast.LENGTH_LONG).show()
            Crashlytics.log(ex.toString())
        }
    }

    //to stop the running services
    private fun stopService()
    {
        val serviceIntent = Intent(context !!.applicationContext, VisitationServices::class.java)
        context !!.stopService(serviceIntent)
    }

    @SuppressLint("SimpleDateFormat")
    fun syncAttendance(
            attendanceType : String,
            attendanceTime : String,
            gpsPoint : Location,
            attendanceRemark : String
    )
    {
        val SOAP_ACTION = Utils.SOAP_NAMESPACE + attendanceType
        val soapObject = SoapObject(Utils.SOAP_NAMESPACE, attendanceType)
        val url = GeneralClass.sync_url + Utils.LOGIN_URL

        try
        {
            soapObject.addProperty("deviceID", deviceID)
            soapObject.addProperty("companyID", companyID)
            soapObject.addProperty("userID", userName)
            when (attendanceType)
            {
                "AttendanceIn" ->
                {
                    soapObject.addProperty("timeInDateTime", attendanceTime)
                    soapObject.addProperty(
                            "timeInGpsPoint",
                            "${gpsPoint.latitude}, ${gpsPoint.longitude}"
                    )
                    soapObject.addProperty("timeInRemark", attendanceRemark)
                }
                else ->
                {
                    soapObject.addProperty("timeOutDateTime", attendanceTime)
                    soapObject.addProperty(
                            "timeOutGpsPoint",
                            "${gpsPoint.latitude}, ${gpsPoint.longitude}"
                    )
                    soapObject.addProperty("timeOutRemark", attendanceRemark)
                }
            }
            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
            envelope.implicitTypes = true
            envelope.setOutputSoapObject(soapObject)
            envelope.dotNet = true
            val httpTransportSE = HttpTransportSE(url)
            httpTransportSE.debug = true
            httpTransportSE.call(SOAP_ACTION, envelope)
            attendanceResult = envelope.response.toString().toBoolean()
        }
        catch (ex : Exception)
        {
            Utils.makeLog(ex.toString())
            Crashlytics.log(ex.toString())
        }
    }
}
