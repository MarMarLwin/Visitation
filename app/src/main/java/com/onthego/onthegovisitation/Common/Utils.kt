package com.onthego.onthegovisitation

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationManager
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.os.Build
import android.provider.Settings.Secure
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class Utils : AppCompatActivity()
{
    companion object
    {
        var currentLocation:Location?=null
        const val DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
        const val DATE_FORMAT = "yyyy-MM-dd"
        const val DATE_ID_FORMAT ="yyMMddHHmmss"
        const val SOAP_NAMESPACE = "http://tempuri.org/"
        const val requestCodes = 1
        const val LOGIN_URL = "LoginService.asmx"
        const val TODO_MSG_URL = "ToDoMsgService.asmx"
        const val CUSTOMER_URL = "CustomerService.asmx"
        const val GPSPOINT_URL = "GPSService.asmx"
        const val LOGIN_SYNC = "Login"
        const val ATTENDANCE_IN = "AttendanceIn"
        const val ATTENDANCE_OUT = "AttendanceOut"
        const val TODO_SYNC = "ToDoMessage"
        const val COMPLETE_MSG_SYNC = "ToDoMessage_CompleteUpdate"
        const val READ_UPDATE_SYNC = "ToDoMessage_ReadUpdate"
        const val CUSTOMER_PHOTO_SYNC = "CustomerPhoto_Save"
        const val CUSTOMER_TEMP_SYNC = "CustomerTemp_Save"
        const val CUSTOMER_SYNC = "CustomerSync"
        const val NEWCUST_SYNC="NewCustomerSync"
        const val GPS_SYNC ="BackgroundGPSSync"
        const val CHECKIN_SYNC = "CheckInGpsSync"
        const val CHECKOUT_SYNC ="CheckOutGpsSync"
        val gson = GsonBuilder().create()

        //Get Necessary Information for SYNC Data
        //Make Simple Alert Dialog
        fun alertDialogMaker(
            context : Context,
            dialogTitle : String?,
            dialogMessage : String?,
            dialogOKButton : String?,
            dialogCancelButton : String?
        )
        {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(dialogTitle)
            builder.setMessage(dialogMessage)
            builder.setPositiveButton(dialogOKButton) { _, _ -> }
            builder.setNegativeButton(dialogCancelButton) { _, _ -> }
            builder.create()
            builder.show()
        }

        //Check network connection
        fun isNetworkConnected(context : Context) : Boolean?
        {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnected
        }

        //Save model data in SharedPreferences
        fun <Any>saveModelPreference(context: Context,key: String,obj:Any)
        {
            savePreference(context,key,gson.toJson(obj))
        }

        //Save login data in SharedPreferences
        fun savePreference(context : Context, key : String, value : String?)
        {
            val sharedPref = context.getSharedPreferences(
                    context.getString(R.string.app_name),
                    Context.MODE_PRIVATE
            )
            val editor = sharedPref !!.edit()
            editor.putString(key, value)
            editor.apply()
        }

        //Retrieve model from SharedPreferences
        fun <Any>getModelPreference(context: Context,key: String,obj:Class<Any>):Any?
        {
            return gson.fromJson(getPreference(context,key),obj)
        }

        //Retrieve saved login data from SharedPreferences
        fun getPreference(context : Context, key : String) : String?
        {
            val sharedPref =
                    context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE)
            return sharedPref.getString(key, null)
        }

        //Get App Version
        fun appVersion(context : Context) : String
        {
            return "Version  ${context.packageManager.getPackageInfo(
                    context.packageName,
                    0
            ).versionName}"
        }

        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("MissingPermission", "HardwareIds")
        fun deviceSerialNum() : String
        {
            return if (Build.VERSION.SDK_INT > 26)
                Build.getSerial()
            else
                Build.SERIAL
        }

        @SuppressLint("MissingPermission")
        @RequiresApi(Build.VERSION_CODES.O)
        fun deviceIMEI(context : Context) : String
        {
            val telephonyManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephonyManager.imei
        }

        fun phoneModel() : String
        {
            return Build.MODEL
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun phoneOs() : String
        {
            return Build.VERSION.SDK_INT.toString()
        }

        @SuppressLint("HardwareIds")
        fun androidID(context : Context) : String
        {
            return Secure.getString(context.contentResolver, Secure.ANDROID_ID)
        }

        //Variable array that Stored Permissions
        var myPermissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
        )

        //Log maker
        fun makeLog(info : String)
        {
            Log.i(GeneralClass.log, info)
        }

        //Permission Requester
        @TargetApi(Build.VERSION_CODES.M)
        fun requestAppPermission(context : Context, selectPermission : Array<String>)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                if (! hasPermission(context, *myPermissions))
                {
                    requestPermissions(context as Activity, selectPermission, requestCodes)
                }
            }
        }

        //Permission Checker
        fun hasPermission(context : Context, vararg permissions : String) : Boolean =
                permissions.all()
                {
                    ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }

        //Toast Message Viewer
        fun showToastMessage(context : Context, message : String)
        {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        //Snack bar viewer
        fun snackBarMaker(view : View, string : String)
        {
            Snackbar.make(view, string, Snackbar.LENGTH_SHORT).show()
        }

        //Get image from camera in bitmap
        fun getResizedBitmap(fileName : String, width : Int, height : Int) : Bitmap
        {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            val outHeight = options.outHeight
            val outWidth = options.outWidth
            var inSampleSize = 1

            if (outHeight > height || outWidth > width)
            {
                inSampleSize = if (outWidth > outHeight) outHeight / height else outWidth / width
            }

            options.inSampleSize = inSampleSize
            options.inJustDecodeBounds = false
            val bitmap = BitmapFactory.decodeFile(fileName, options)
            val newImgWidth = 800
            val newImgHeight = 800
            var rotateBitmap = Bitmap.createScaledBitmap(bitmap, newImgWidth, newImgHeight, true)
            val mtx : Matrix? = Matrix()
            val ei = ExifInterface(fileName)

            when (ei.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
            ))
            {
                ExifInterface.ORIENTATION_ROTATE_90 //portrait
                ->
                {
                    mtx !!.setRotate(90.toFloat())
                    rotateBitmap =
                            Bitmap.createBitmap(
                                    rotateBitmap,
                                    0,
                                    0,
                                    rotateBitmap.width,
                                    rotateBitmap.height,
                                    mtx,
                                    true
                            )
                    mtx.reset()
                }
                ExifInterface.ORIENTATION_ROTATE_180 // might need to flip horizontally too...
                ->
                {
                    mtx !!.setRotate(180.toFloat())
                    rotateBitmap =
                            Bitmap.createBitmap(
                                    rotateBitmap,
                                    0,
                                    0,
                                    rotateBitmap.width,
                                    rotateBitmap.height,
                                    mtx,
                                    true
                            )
                    mtx.reset()
                }
                else ->
                {
                    mtx !!.reset()
                }
            }
            return rotateBitmap
        }

        //for custom location marker
        fun bitmapDescriptorFromVector(context : Context, vector : Int) : BitmapDescriptor
        {
            val vectorDrawable = ContextCompat.getDrawable(context, vector)
            vectorDrawable !!.setBounds(
                    0,
                    0,
                    vectorDrawable.intrinsicWidth,
                    vectorDrawable.intrinsicHeight
            )
            val bitmap = Bitmap.createBitmap(
                    vectorDrawable.intrinsicWidth,
                    vectorDrawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }

        //check running service
        private fun isServiceRunning(mContext : Context) : Boolean
        {
            val activityManager =
                    mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service : ActivityManager.RunningServiceInfo in activityManager.getRunningServices(
                    Integer.MAX_VALUE
            ))
            {
                if (VisitationServices::class.java.name == service.service.className)
                {
                    return true
                }
            }
            return false
        }

        //Get internet time from web
        fun getServerTime(format : String) : String
        {
            val httpClient = OkHttpClient()
            val response =
                    httpClient.newCall(Request.Builder().url("http://www.googel.com").build()).execute()
            val todayDate = response.headers("date")[0]
            val date = Date(todayDate)
            return SimpleDateFormat(format, Locale.ENGLISH).format(date)
        }

        fun getDeviceTime(format:String):String{
            return SimpleDateFormat(
                format,
                Locale.ENGLISH
            ).format(Calendar.getInstance().time)
        }

        //show progress dialog
        fun makeProgressDialog(context : Context, msg : String) : AlertDialog
        {
            val llPadding = 30
            val ll = LinearLayout(context)
            ll.orientation = LinearLayout.HORIZONTAL
            ll.setPadding(llPadding, llPadding, llPadding, llPadding)
            ll.gravity = Gravity.CENTER
            var llParam = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )
            llParam.gravity = Gravity.CENTER
            ll.layoutParams = llParam
            val progressBar = ProgressBar(context)
            progressBar.isIndeterminate = true
            progressBar.setPadding(0, 0, llPadding, 0)
            progressBar.layoutParams = llParam
            progressBar.setBackgroundColor(Color.TRANSPARENT)

            llParam =
                    LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    )
            llParam.gravity = Gravity.CENTER
            val tvText = TextView(context)
            tvText.text = msg
            tvText.setTextColor(Color.parseColor("#000000"))
            tvText.textSize = 20f
            tvText.setBackgroundColor(Color.TRANSPARENT)
            tvText.layoutParams = llParam

            ll.addView(progressBar)
            ll.addView(tvText)
            val builder = AlertDialog.Builder(context)
            builder.setCancelable(false)
            builder.setView(ll)
            val dialog = builder.create()
            val window = dialog.window
            if (window != null)
            {
                val layoutParams = WindowManager.LayoutParams()
                layoutParams.copyFrom(dialog.window !!.attributes)
                layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                dialog.window !!.attributes = layoutParams
            }
            return dialog
        }

        fun isEnableGPS(context : Context) : Boolean
        {
            val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        //hide keyboard
        fun hideKeyboard(view : View, context : Context)
        {
            val imm : InputMethodManager =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        //Text input layout Text Changed Listener
        fun layoutEmptyTextListener(editText : EditText, textInputLayout : TextInputLayout)
        {
            editText.addTextChangedListener(object : TextWatcher
            {
                override fun afterTextChanged(s : Editable)
                {
                }

                override fun beforeTextChanged(
                        s : CharSequence,
                        start : Int,
                        count : Int,
                        after : Int
                )
                {
                }

                override fun onTextChanged(s : CharSequence, start : Int, before : Int, count : Int)
                {
                    if (editText.text.count() >= 1)
                    {
                        textInputLayout.isErrorEnabled = false
                    }
                }
            })
        }

        //Convert 24 hr to 12 hr
        @SuppressLint("SimpleDateFormat")
        fun timeFormatConverter(time : String) : String
        {
            val originalFormat = SimpleDateFormat(DATETIME_FORMAT)
            val simpleDateFormat = SimpleDateFormat("hh:mm a")
            return simpleDateFormat.format(originalFormat.parse(time))
        }

        //convert image to byte array
        fun convertImageToByteArray(imageView : ImageView) : ByteArray
        {
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
            bitmap.recycle()
            val byte = byteArrayOutputStream.toByteArray()
            return Base64.encode(byte, Base64.DEFAULT)
        }
    }
}