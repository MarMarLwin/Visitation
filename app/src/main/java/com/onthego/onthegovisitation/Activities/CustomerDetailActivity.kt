package com.onthego.onthegovisitation

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.crashlytics.android.Crashlytics
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.onthego.onthegovisitation.Adapters.CustomMapInfoWindowAdapter
import com.onthego.onthegovisitation.Common.BaseActivity
import com.onthego.onthegovisitation.Models.Customer
import kotlinx.android.synthetic.main.activity_customer_location.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import java.io.File

class CustomerDetailActivity : BaseActivity(), OnMapReadyCallback,
        GoogleMap.OnMyLocationClickListener, SignatureDialogFragment.OnInputListener
{
    private lateinit var mMap : GoogleMap
    private lateinit var cameraPhotoByteArr : ByteArray
    private lateinit var signPhotoByteArr : ByteArray
    private lateinit var picture : ImageView
    private lateinit var sign : ImageView
    private lateinit var file : File
    private lateinit var dir : File
    private var photoPath : String? = null
    private var signPath : String? = null
    private var count = 0
    private var  companyId:String?=null
    private var userId:String?=null
    private var isTemp:String?=null
    private var currentLocation:Location?=null
    private lateinit var customer:Customer
    private var isCheckIn:Boolean=false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_location)

        customer=intent.getSerializableExtra("cusObj")as Customer

        supportActionBar !!.title = customer.CustomerName

        val actionbar: androidx.appcompat.app.ActionBar? = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.setHomeAsUpIndicator(R.drawable.otglogo)

        photoPath = null
        signPath = null
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if(Utils.getModelPreference(this,GeneralClass.checkInCustomer,Customer::class.java)!!.CustomerID!=customer.CustomerID)
            checkOutButton.setTextColor(resources.getColor(R.color.grey,null))
        else
            checkInButton.setTextColor(resources.getColor(R.color.grey,null))

        checkInButton.setOnClickListener {

            if(Utils.getModelPreference(this,GeneralClass.checkInCustomer,Customer::class.java)!=null)
            {
                Utils.showToastMessage(this,getString(R.string.checkOutMsg))
                return@setOnClickListener
            }else
            {
                Utils.saveModelPreference(this,GeneralClass.checkInCustomer,customer)

                doAsync {
                     isCheckIn=checkInSync()
                    uiThread {
                        when(isCheckIn)
                        {
                            true->
                            {
                                setUpDialog()
                                checkInButton.isEnabled=false
                                checkInButton.setTextColor(resources.getColor(R.color.grey,null))
                            }
                            else->Utils.showToastMessage(this@CustomerDetailActivity,getString(R.string.tryagain))
                        }
                    }
                }
            }
        }

        checkOutButton.setOnClickListener {
            doAsync {
                if(Utils.getModelPreference(this@CustomerDetailActivity,
                    GeneralClass.checkInCustomer,Customer::class.java)!!.CustomerID==customer.CustomerID)
                {
                    val isSync=checkOutSync()
                    uiThread {
                        when(isSync)
                        {
                            true->
                            {
                                finish()
                                Utils.savePreference(this@CustomerDetailActivity,GeneralClass.checkInId,"")
                                Utils.saveModelPreference(this@CustomerDetailActivity,GeneralClass.checkInCustomer,null)
                            }
                            else->Utils.showToastMessage(this@CustomerDetailActivity,getString(R.string.tryagain))
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initialize()
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            scanIntent.data = Uri.fromFile(file)
            sendBroadcast(scanIntent)
            val width = resources.displayMetrics.widthPixels
            val height = resources.displayMetrics.heightPixels
            val bitmap = Utils.getResizedBitmap(file.path, width, height)
            picture.setImageBitmap(bitmap)
            photoPath = file.path
//            cameraPhotoByteArr = Utils.compressBitmap(bitmap)
        }
        else
            photoPath = null
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap : GoogleMap)
    {
        mMap = googleMap

        mMap.isMyLocationEnabled = true
        mMap.setOnMyLocationClickListener(this)
        val gps=customer.GpsPoint.split(",".toRegex())
        val custLocation = LatLng(gps[0].toDouble(), gps[1].toDouble())

        val myCurrentLocation = LocationServices.getFusedLocationProviderClient(this).lastLocation
        myCurrentLocation.addOnCompleteListener(this)
        { task ->
            if (task.isSuccessful && task.result != null)
            {
                currentLocation = task.result
                mMap.addMarker(
                        MarkerOptions().position(
                                LatLng(
                                    currentLocation !!.latitude,
                                    currentLocation!!.longitude
                                )
                        ).icon(
                                BitmapDescriptorFactory.fromResource(R.drawable.ic_standing_man)
                        )
                )
            }
        }

        mMap.setInfoWindowAdapter(CustomMapInfoWindowAdapter(this))
        val m : Marker = mMap.addMarker(
                MarkerOptions().position(custLocation).title(customer.CustomerName).icon(
                        BitmapDescriptorFactory.fromResource(
                                R.drawable.ic_customer_location
                        )
                )
        )
        m.tag = customer
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(custLocation, 15.0f))
    }

    override fun onMyLocationClick(location : Location)
    {
    }

    override fun sendInput(path : String)
    {
        val bitmap = BitmapFactory.decodeFile(path)
        sign.setImageBitmap(bitmap)
        signPath = path
//        signPhotoByteArr = Utils.compressBitmap(bitmap)
    }

    override fun onCreateOptionsMenu(menu : Menu?) : Boolean
    {
        menuInflater.inflate(R.menu.customer_detail_menu, menu)
        val menuItem = menu !!.findItem(R.id.action_notification)
        menuItem.icon = buildCounterDrawable(count, R.drawable.ic_notification)
        return super.onCreateOptionsMenu(menu)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onOptionsItemSelected(item : MenuItem?) : Boolean
    {
        when (item?.itemId)
        {
            R.id.action_notification ->
            {
                return true
            }
            R.id.action_call ->
            {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.data = Uri.parse("tel:${customer.Phone}")
                startActivity(intent)
                return true
            }
            R.id.action_task->
            {
                if(Utils.getModelPreference(this@CustomerDetailActivity,
                GeneralClass.checkInCustomer,Customer::class.java)?.CustomerID==customer.CustomerID)
                setUpDialog()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    companion object
    {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }

    //---region "Customize Function"

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpDialog()
    {
        try
        {
            checkOutButton.isEnabled=true
            signPhotoByteArr = byteArrayOf()
            cameraPhotoByteArr = byteArrayOf()
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.visit_purpose_dialog, null)
            val dialog : AlertDialog
            val reason = dialogView.findViewById<Spinner>(R.id.reasonSpinner)
            val comment = dialogView.findViewById<EditText>(R.id.commentEditText)
            picture = dialogView.findViewById(R.id.cameraImageView)
            sign = dialogView.findViewById(R.id.signImageView)
            val takePhotoBtn = dialogView.findViewById<Button>(R.id.takePictureBtn)
            val signBtn = dialogView.findViewById<Button>(R.id.takeSignBtn)
            val saveBtn = dialogView.findViewById<Button>(R.id.saveBtn)
            val cancelBtn = dialogView.findViewById<Button>(R.id.cancelBtn)

            builder.setView(dialogView)
            builder.setCancelable(false)
            dialog = builder.create()
            dialog.show()

            cancelBtn.setOnClickListener {
                dialog.dismiss()
            }

            takePhotoBtn.setOnClickListener {
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    takePictureIntent.resolveActivity(packageManager)?.also {
                        dir = File(Environment.getExternalStorageDirectory().absolutePath + "/OnTheGoVisitation/")
                        val storedPath = "myPhoto.png" //to edit
                        file = File(dir, storedPath)
                        if (! dir.exists())
                        {
                            dir.mkdir()
                        }

                        file.createNewFile()
                        val uri = FileProvider.getUriForFile(
                            applicationContext,
                            BuildConfig.APPLICATION_ID,
                            file
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
            }

            signBtn.setOnClickListener {
                val signatureDialogFragment = SignatureDialogFragment()
                signatureDialogFragment.isCancelable = false
                signatureDialogFragment.show(this.supportFragmentManager, "Signature")
            }

            picture.setOnClickListener {
                if (photoPath != null)
                {
                    val intent =
                        Intent(this@CustomerDetailActivity, ImageSplashActivity::class.java)
//                    intent.putExtra("imageByteArr",cameraPhotoByteArr)
//                    var bundle= Bundle()
//                    bundle.putByteArray("imageByteArr",cameraPhotoByteArr)
//                    intent.putExtras(bundle)
                    intent.putExtra("imagePath", photoPath)
                    startActivity(intent)
                }
            }

            sign.setOnClickListener {
                if (signPath != null)
                {
                    val intent = Intent(this@CustomerDetailActivity, ImageSplashActivity::class.java)
//                    intent.putExtra("imageByteArr",signPhotoByteArr)
                    intent.putExtra("imagePath", signPath)
                    startActivity(intent)
                }
            }
        }
        catch (ex : Exception)
        {
            Utils.showToastMessage(this, ex.toString())
            Crashlytics.log(ex.toString())
        }
    }

    private fun buildCounterDrawable(count : Int, backgroundImageId : Int) : Drawable
    {
        val inflater = LayoutInflater.from(this)
        val view = inflater !!.inflate(R.layout.counter_menuitem_layout, null)
        view.setBackgroundResource(backgroundImageId)

        if (count == 0)
        {
            val counterTextPanel = view.findViewById<RelativeLayout>(R.id.counterValuePanel)
            counterTextPanel.visibility = View.GONE
        }
        else
        {
            val textView = view.findViewById<TextView>(R.id.count)
            textView.text = " $count"
        }

        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        return BitmapDrawable(resources, bitmap)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkInSync():Boolean{
        try{
            val SOAP_ACTION = Utils.SOAP_NAMESPACE + Utils.CHECKIN_SYNC
            val soapObject = SoapObject(
                Utils.SOAP_NAMESPACE,
                Utils.CHECKIN_SYNC
            )
            val url = GeneralClass.sync_url + Utils.GPSPOINT_URL
            val checkInID="${companyId}${userId}${Utils.getDeviceTime(Utils.DATE_ID_FORMAT)}"

            soapObject.addProperty("checkInID",checkInID)
            soapObject.addProperty("deviceID",Utils.deviceSerialNum())
            soapObject.addProperty("companyID",companyId)
            soapObject.addProperty("userID",userId)
            soapObject.addProperty("customerID",customer.CustomerID)
            soapObject.addProperty("isTempCustomer",intent.getBooleanExtra(GeneralClass.isTempCustomer,false))
            soapObject.addProperty("checkInGpsPoint", "${currentLocation?.latitude},${currentLocation?.longitude}")

            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
            envelope.implicitTypes = true
            envelope.setOutputSoapObject(soapObject)
            envelope.dotNet = true
            val httpTransportSE = HttpTransportSE(url)
            httpTransportSE.debug = true
            httpTransportSE.call(SOAP_ACTION, envelope)
            //save CheckInID
            Utils.savePreference(this,GeneralClass.checkInId,checkInID)
            return envelope.response.toString().toBoolean()
        }catch (ex: Exception) {
            Utils.makeLog(ex.toString())
            Crashlytics.log(ex.toString())
            return false
        }
    }

    private fun checkOutSync():Boolean{
        try {
            val SOAP_ACTION = Utils.SOAP_NAMESPACE + Utils.CHECKOUT_SYNC
            val soapObject = SoapObject(
                Utils.SOAP_NAMESPACE,
                Utils.CHECKOUT_SYNC)
            val url = GeneralClass.sync_url + Utils.GPSPOINT_URL

            soapObject.addProperty("checkInID",Utils.getPreference(this,GeneralClass.checkInId))
            soapObject.addProperty("checkOutGpsPoint", "${currentLocation?.latitude},${currentLocation?.longitude}")

            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
            envelope.implicitTypes = true
            envelope.setOutputSoapObject(soapObject)
            envelope.dotNet = true
            val httpTransportSE = HttpTransportSE(url)
            httpTransportSE.debug = true
            httpTransportSE.call(SOAP_ACTION, envelope)
            return envelope.response.toString().toBoolean()
        }catch (ex:java.lang.Exception)
        {
            Utils.makeLog(ex.toString())
            Crashlytics.log(ex.toString())
            return false
        }
    }

    private fun initialize(){
         companyId=Utils.getPreference(this,GeneralClass.companyID)
         userId=Utils.getPreference(this,GeneralClass.userName)
         isTemp=this.intent.getStringExtra(GeneralClass.isTempCustomer)
    }
    //endregion
}
