package com.onthego.onthegovisitation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.crashlytics.android.Crashlytics
import com.google.gson.GsonBuilder
import com.onthego.onthegovisitation.Common.BaseActivity
import com.onthego.onthegovisitation.Models.Customer
import kotlinx.android.synthetic.main.activity_create_customer_picture.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import java.io.File
import java.io.Serializable

class AddCustomerImageActivity : BaseActivity()
{
    lateinit var file : File
    lateinit var dir : File
    var photoPath : String? = null
    private var REQUEST_IMAGE_CODE = 101
    private var tempCustomerPhoto : Boolean = false
    private lateinit var customer:Customer
    private var photoFlag:Boolean =false
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_customer_picture)

        customerPicture1.setOnClickListener {
            openCameraIntent("cusPic1")
        }

        previousButton.setOnClickListener {
            finish()
        }

        customer = intent.getSerializableExtra("cusObj")as Customer

        nextButton.setOnClickListener {
            try
            {
                when(photoFlag)
                {
                    true->
                    {
                        val tempCustPhoto = Utils.convertImageToByteArray(customerPicture1)
                        val intent = Intent(this, CustomerDetailActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

                        intent.putExtra(GeneralClass.isTempCustomer, true)
                        intent.putExtra("cusObj",customer as Serializable)

                        doAsync {
                            syncCustomerPhoto(tempCustPhoto)
                            uiThread {
                                if (tempCustomerPhoto)
                                {
                                    Utils.showToastMessage(
                                        this@AddCustomerImageActivity,
                                        "Successfully Updated!!"
                                    )
                                    startActivity(intent)
                                    finish()
                                }
                                else
                                    Utils.showToastMessage(this@AddCustomerImageActivity, "Failed")
                            }
                        }
                    }
                    false->
                        Utils.showToastMessage(this,getString(R.string.takePhoto))
                }
            }catch (ex:Exception)
            {
                Utils.showToastMessage(this, ex.toString())
                Crashlytics.log(ex.toString())
            }
        }
    }

    private fun openCameraIntent(picName : String)
    {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { pictureIntent ->
            pictureIntent.resolveActivity(packageManager)?.also {
                dir = File(Environment.getExternalStorageDirectory().absolutePath + "/OnTheGo/")
                val storedFile = "$picName.png"
                file = File(dir, storedFile)

                if (! dir.exists())
                    dir.mkdir()

                file.createNewFile()
                val uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, file)

                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                //set crop properties
//                pictureIntent.putExtra("crop", "true")

                startActivityForResult(pictureIntent, REQUEST_IMAGE_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CODE && resultCode == Activity.RESULT_OK)
        {
            val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            scanIntent.data = Uri.fromFile(file)
            sendBroadcast(scanIntent)
            val width = resources.displayMetrics.widthPixels
            val height = resources.displayMetrics.heightPixels
            val bitmap = Utils.getResizedBitmap(file.path, width, height)
            customerPicture1.setImageBitmap(bitmap)
            photoPath = file.path
            photoFlag=true
        }
        else
            photoPath = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun syncCustomerPhoto(customerPhoto : ByteArray)
    {
        val SOAP_ACTION = Utils.SOAP_NAMESPACE + Utils.CUSTOMER_PHOTO_SYNC
        val soapObject = SoapObject(Utils.SOAP_NAMESPACE, Utils.CUSTOMER_PHOTO_SYNC)
        val url = GeneralClass.sync_url + Utils.CUSTOMER_URL

        try
        {
            val customerTempPhoto = TempCustomerPhoto(customer.CustomerID, customerPhoto)
            val gson = GsonBuilder().create()
            val jsonData = gson.toJson(customerTempPhoto)

            soapObject.addProperty("deviceID", Utils.getPreference(this, GeneralClass.deviceID))
            soapObject.addProperty("companyID", Utils.getPreference(this, GeneralClass.companyID))
            soapObject.addProperty("userID", Utils.getPreference(this, GeneralClass.userName))
            soapObject.addProperty("tempCustPhoto", jsonData)
            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
            envelope.implicitTypes = true
            envelope.setOutputSoapObject(soapObject)
            envelope.dotNet = true
            val httpTransportSE = HttpTransportSE(url)
            httpTransportSE.debug = true
            httpTransportSE.call(SOAP_ACTION, envelope)
            tempCustomerPhoto = envelope.response.toString().toBoolean()
        }
        catch (ex : Exception)
        {
            Utils.showToastMessage(this, ex.toString())
            Crashlytics.log(ex.toString())
        }
    }
}