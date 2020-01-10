package com.onthego.onthegovisitation

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.crashlytics.android.Crashlytics
import com.google.android.gms.location.*
import com.google.gson.GsonBuilder
import com.onthego.onthegovisitation.DataAccess.AppDatabase
import com.onthego.onthegovisitation.Models.BackgroundGPS
import org.jetbrains.anko.doAsync
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("Registered")
class VisitationServices : Service()
{
    val TAG = VisitationServices::class.java.simpleName
    private lateinit var mFusedLocationProviderClient : FusedLocationProviderClient
    private lateinit var mLocationRequest : LocationRequest
    private lateinit var mLocationCallback : LocationCallback
    private lateinit var lastGPSList:MutableList<BackgroundGPS>
    private lateinit var geocode : Geocoder
    private lateinit var appDb:AppDatabase
    override fun onCreate()
    {
        super.onCreate()
        createLocationRequest()
        geocode = Geocoder(
            this,
            Locale.getDefault()
        )
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationCallback = object : LocationCallback()
        {
            override fun onLocationResult(locationResult : LocationResult)
            {
                super.onLocationResult(locationResult)
                if (locationResult.lastLocation.accuracy < 35)
                    sendInfo(locationResult.lastLocation)
                else
                    Log.e(TAG, "Location does not accurate. Its accuracy is grater than 35")
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onStartCommand(intent : Intent, flags : Int, startId : Int) : Int
    {
        startLocationUpdates()
        val starIntent = Intent(this, LoginActivity::class.java)
        starIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val contentIntent = PendingIntent.getActivity(this, 0, starIntent, 0)
        val mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText("OTG Visitation service is running.")
            .setSmallIcon(R.drawable.otg_logo)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        val notification = mBuilder.setContentIntent(contentIntent).build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val serviceChannel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Visitation Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
                )
            serviceChannel.enableLights(true)
            serviceChannel.enableVibration(true)
            serviceChannel.lightColor = Color.GREEN
            serviceChannel.vibrationPattern = longArrayOf(300, 300, 300, 300, 300)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(serviceChannel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
        {
            startForeground(1, notification)
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent : Intent) : IBinder
    {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    @SuppressLint("RestrictedApi")
    private fun createLocationRequest()
    {
        this.mLocationRequest = LocationRequest()
        this.mLocationRequest.interval = INTERVAL
        this.mLocationRequest.fastestInterval = FASTEST_INTERVAL
        //this.mLocationRequest.smallestDisplacement = DISPLACEMENT
        this.mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    private fun startLocationUpdates()
    {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED
        )
        {
            return
        }
        mFusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )

        Log.v(TAG, "Location update started ..............: ")
    }

    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    private fun stopLocationUpdates()
    {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED
        )
        {
            return
        }
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
        Log.v(TAG, "Location update stopped .......................")
    }

    override fun onDestroy()
    {
        super.onDestroy()
        stopLocationUpdates()
        Log.v(TAG, "Service Stopped!")
    }

    @SuppressLint("SimpleDateFormat")
    fun sendInfo(location : Location)
    {
        try {
            var distance = 300.0
            val deviceId = Utils.getPreference(applicationContext, GeneralClass.deviceID)
            val companyId = Utils.getPreference(applicationContext, GeneralClass.companyID)
            val userId = Utils.getPreference(applicationContext, GeneralClass.userName)
            appDb = AppDatabase.getAppDatabase(applicationContext)!!

            val gpsLocations = appDb.backgroundGPSDAO().getLastGPS()
            if(gpsLocations != null)
            {
                val lastLocation = gpsLocations.GpsPoint.split(",").toTypedArray()

                distance = distanceBetweenPoints(
                    lastLocation[0].toDouble(),
                    lastLocation[1].toDouble(),
                    location.latitude,
                    location.longitude)
//                if(distance<300)
//                    return
            }

            val formatter = SimpleDateFormat(Utils.DATETIME_FORMAT).format(Date())

            val gpsPoint = BackgroundGPS(
                "${companyId}${userId}${Utils.getDeviceTime(Utils.DATE_ID_FORMAT)}",
                deviceId.toString(),
                companyId.toString(),
                userId.toString(),
                "${location.latitude},${location.longitude}",
                formatter.toString()
            )

            if (Utils.isNetworkConnected(applicationContext)!!)
            {
                doAsync {
                    gpsPoint.CapturedDateTime = Utils.getServerTime(Utils.DATETIME_FORMAT)
                    lastGPSList=  appDb.backgroundGPSDAO().getAllGPS().toMutableList()
                    lastGPSList.add(gpsPoint)
                    syncGPS()
                }
            }
            else{
                appDb.backgroundGPSDAO().insert(gpsPoint)
            }

            val intent = Intent()
            intent.putExtra("Latitude", location.latitude)
            intent.putExtra("Longitude", location.longitude)
            intent.action = "FILTER"
            sendBroadcast(intent)

            Log.e(TAG, "${location.latitude} and ${location.longitude} and ${location.accuracy} " + formatter.format(Date()))

        } catch (e1: IOException) {
            e1.printStackTrace()
        }
    }

    private fun syncGPS()
    {
        val SOAP_ACTION = Utils.SOAP_NAMESPACE + Utils.GPS_SYNC
        val soapObject = SoapObject(Utils.SOAP_NAMESPACE, Utils.GPS_SYNC)
        val url = GeneralClass.sync_url + Utils.GPSPOINT_URL

        try
        {
            val gson = GsonBuilder().create()
            val jsonString: String = gson.toJson(lastGPSList)
            soapObject.addProperty("backgroundGPSList", jsonString)

            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
            envelope.implicitTypes = true
            envelope.setOutputSoapObject(soapObject)
            envelope.dotNet = true
            val httpTransportSE = HttpTransportSE(url)
            httpTransportSE.debug = true
            httpTransportSE.call(SOAP_ACTION, envelope)
            val gpsResult = envelope.response.toString()
            if(gpsResult=="false")
                appDb.backgroundGPSDAO().insertAll(lastGPSList)
            else
                appDb.backgroundGPSDAO().deleteAll()

            Log.e(TAG, gpsResult)
        }
        catch (ex : Exception)
        {
            Log.e(TAG, ex.toString())
            appDb.backgroundGPSDAO().insertAll(lastGPSList)
            Crashlytics.log(ex.toString())
        }
    }

    private fun distanceBetweenPoints(
        sLat : Double,
        sLong : Double,
        eLat : Double,
        eLong : Double
    ) : Double
    {
        val result = FloatArray(1)
        Location.distanceBetween(sLat, sLong, eLat, eLong, result)
        return result[0].toDouble()
    }

    companion object
    {
        private const val INTERVAL = (1000 * 1 * 60).toLong()
        private const val FASTEST_INTERVAL = (1000 * 40).toLong()
        //private const val DISPLACEMENT = 300f
        private const val CHANNEL_ID = "ServiceChannel"
    }
}
