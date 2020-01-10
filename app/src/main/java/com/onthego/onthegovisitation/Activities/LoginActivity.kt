package com.onthego.onthegovisitation

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.annotation.RequiresApi
import com.crashlytics.android.Crashlytics
import com.onthego.onthegovisitation.Common.BaseActivity
import com.onthego.onthegovisitation.Common.LocaleHelper
import com.onthego.onthegovisitation.Fragments.AttendanceDialogFragment
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE

class LoginActivity : BaseActivity()
{
    private var deviceID : String? = null
    private var companyID : String? = null
    private var userName : String? = null
    private var checkInTime : String? = null
    private var deviceSerialNo : String? = null
    private var userNameCheck : String = ""
    private var passwordCheck : String = ""
    private var companyIDCheck : String = ""
    private var loginResult : String? = null
    private lateinit var selectLanguageID:String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
        selectLanguageID=Utils.getPreference(this,GeneralClass.selectedLanguageID)?:"0"

        languageSetting.setOnClickListener {
            val languageList = arrayOf("English", "မြန်မာဘာသာ")
            val mBuilder = AlertDialog.Builder(this)
            mBuilder.setTitle(getString(R.string.choose_language))
            mBuilder.setSingleChoiceItems(languageList, selectLanguageID.toInt()) { dialog, which ->
                when (which)
                {
                    0 ->
                    {
                        LocaleHelper.setLocale(this, LocaleHelper.mEnglish,"0")
                        selectLanguageID = "0"
                    }
                    1 ->
                    {
                        LocaleHelper.setLocale(this, LocaleHelper.mBurmaUnicode,"1")
                        selectLanguageID = "1"
                    }
                }
                dialog.dismiss()
                val i = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
                i !!.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
            }
            mBuilder.create().show()
        }

        versionTextView.text = Utils.appVersion(this)

        if (Build.VERSION.SDK_INT >= 23)
        {
            Utils.requestAppPermission(this@LoginActivity, Utils.myPermissions)
        }

        deviceID = Utils.getPreference(this@LoginActivity, GeneralClass.deviceID)
        companyID = Utils.getPreference(this@LoginActivity, GeneralClass.companyID)
        userName = Utils.getPreference(this@LoginActivity, GeneralClass.userName)
        checkInTime = Utils.getPreference(this@LoginActivity, GeneralClass.checkInTime)

        val progressDialog = Utils.makeProgressDialog(this, getString(R.string.loading_msg))

        if (checkInTime.isNullOrEmpty())
        {
            checkInTime = Utils.DATETIME_FORMAT
        }

        logInButton.setOnClickListener {
            try
            {
                if (! Utils.isEnableGPS(this) || ! Utils.isNetworkConnected(this) !!)
                {
                    val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                    builder.setTitle("Warning!")
                    builder.setMessage("Internet connection and GPS required.")
                    builder.setPositiveButton("OK") { _, _ -> enableGPS() }
                    builder.create()
                    builder.show()
                    progressDialog.dismiss()
                    return@setOnClickListener
                }
                else
                {
                    progressDialog.show()
                    when
                    {
                        attemptLogin() ->
                        {
//                            progressDialog.show()
                            doAsync {
                                val requestTime = Utils.getServerTime(Utils.DATE_FORMAT)
                                login()
                                uiThread {
                                    when
                                    {
                                        requestTime != checkInTime !!.split(" ".toRegex())[0] && loginResult == "LOGIN" ->
                                        {
                                            AttendanceDialogFragment().show(
                                                    this@LoginActivity.supportFragmentManager,
                                                    resources.getString(R.string.attendance_in)
                                            )
                                            // change webservice return deviceID
                                            Utils.savePreference(
                                                this@LoginActivity,
                                                GeneralClass.deviceID,
                                                Utils.deviceSerialNum()
                                            )
                                            Utils.savePreference(
                                                    this@LoginActivity,
                                                    GeneralClass.companyID,
                                                    companyIDCheck
                                            )
                                            Utils.savePreference(
                                                    this@LoginActivity,
                                                    GeneralClass.userName,
                                                    userNameCheck
                                            )
                                            Utils.savePreference(
                                                    this@LoginActivity,
                                                    GeneralClass.checkOutTime,
                                                    ""
                                            )
                                        }
                                        requestTime == checkInTime !!.split(" ".toRegex())[0] && loginResult == "LOGIN" ->
                                        {
                                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                            startActivity(intent)
                                        }
                                        else ->
                                        {
                                            passwordEditTextLayout.error = null
                                            passwordEditTextLayout.error =
                                                    getString(R.string.error_login)
                                        }
                                    }
                                    progressDialog.dismiss()
                                    passwordEditText.text = null
                                }
                            }
                        }
                        else ->
                        {
                            Utils.showToastMessage(
                                    this,
                                    getString(R.string.error_authentication_fail)
                            )
                            progressDialog.dismiss()
                        }
                    }
                }
            }
            catch (ex : Exception)
            {
                Utils.showToastMessage(this, ex.message !!)
                progressDialog.dismiss()
            }
            finally
            {
            }
        }

        Utils.layoutEmptyTextListener(companyIdEditText, companyIdEditTextLayout)
        Utils.layoutEmptyTextListener(usernameEditText, usernameEditTextLayout)
        Utils.layoutEmptyTextListener(passwordEditText, passwordEditTextLayout)

        changePasswordButton.setOnClickListener()
        {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        loginLayout.setOnClickListener {
            Utils.hideKeyboard(loginLayout, this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
    if(Utils.hasPermission(this,*Utils.myPermissions))
        deviceSerialNo = Utils.deviceSerialNum()
        deviceSerialNoTextView.text = deviceSerialNo

        deviceIDEditText.setText(deviceID)
        companyIdEditText.setText(companyID)
        usernameEditText.setText(userName)
        super.onResume()
    }

    override fun onRequestPermissionsResult(
            requestCode : Int,
            permissions : Array<String>,
            grantResults : IntArray
    )
    {
        when (requestCode)
        {
            Utils.requestCodes ->
            {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Utils.showToastMessage(this, "Permission Granted")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun attemptLogin() : Boolean
    {
        userNameCheck = usernameEditText.text.toString()
        passwordCheck = passwordEditText.text.toString()
        companyIDCheck = companyIdEditText.text.toString()

        passwordEditTextLayout.error = null
        usernameEditTextLayout.error = null
        companyIdEditTextLayout.error = null
        var cancel = false
        var focusView : View? = null

        if (TextUtils.isEmpty(passwordCheck))
        {
            passwordEditTextLayout.error = getString(R.string.error_field_required)
            focusView = passwordEditText
            cancel = true
        }

        if (TextUtils.isEmpty(userNameCheck))
        {
            usernameEditTextLayout.error = getString(R.string.error_field_required)
            focusView = usernameEditText
            cancel = true
        }

        if (TextUtils.isEmpty(companyIDCheck))
        {
            companyIdEditTextLayout.error = getString(R.string.error_field_required)
            focusView = companyIdEditText
            cancel = true
        }

        if (cancel)
        {
            focusView?.requestFocus()
            return false
        }

        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun login()
    {
        val SOAP_ACTION = Utils.SOAP_NAMESPACE + Utils.LOGIN_SYNC
        val soapObject = SoapObject(Utils.SOAP_NAMESPACE, Utils.LOGIN_SYNC)
        val url = GeneralClass.sync_url + Utils.LOGIN_URL

        try
        {
            soapObject.addProperty("userID", userNameCheck)
            soapObject.addProperty("loginPassword", passwordCheck)
            soapObject.addProperty("securityCode", Utils.deviceSerialNum())
            soapObject.addProperty("companyID", companyIDCheck)
            soapObject.addProperty("loginType", "Login")
            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
            envelope.implicitTypes = true
            envelope.setOutputSoapObject(soapObject)
            envelope.dotNet = true
            val httpTransportSE = HttpTransportSE(url)
            httpTransportSE.debug = true
            httpTransportSE.call(SOAP_ACTION, envelope)
            loginResult = envelope.response.toString()
        }
        catch (ex : Exception)
        {
            Utils.makeLog(ex.toString())
            Crashlytics.log(ex.toString())
        }
    }
}
