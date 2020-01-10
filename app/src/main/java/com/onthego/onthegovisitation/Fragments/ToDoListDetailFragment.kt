package com.onthego.onthegovisitation

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.crashlytics.android.Crashlytics
import kotlinx.android.synthetic.main.fragment_to_do_list_detail.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE

class ToDoListDetailFragment : DialogFragment() {
    private lateinit var getToDoMessage: String
    private lateinit var getAssignBy: String
    private lateinit var getAssignDateTime: String
    private lateinit var getToDoMessageID: String
    private lateinit var getToDoComment: String
    private lateinit var getToDoStatus: String
    private lateinit var rootViewDF: View
    private var todoMessageCompleteUpdate: Boolean = false
    private lateinit var todoListStats: String
    private lateinit var onDismissListener: DialogInterface.OnDismissListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootViewDF = inflater.inflate(R.layout.fragment_to_do_list_detail, container, false)

        val tdmMessage = rootViewDF.findViewById<TextView>(R.id.todoMessageDetailTextView)
        val tdmAssignBy = rootViewDF.findViewById<TextView>(R.id.todoMessageDetailAssignBy)
        val tdmDate = rootViewDF.findViewById<TextView>(R.id.todoMessageDetailDateTime)
        val tdmComment = rootViewDF.findViewById<EditText>(R.id.todoListDetailCommentEditText)
        val tdlSubmit = rootViewDF.findViewById<Button>(R.id.todoMessageSummitButton)
        val tdlComplete = rootViewDF.findViewById<RadioButton>(R.id.completeRadioButton)
        val tdlFollowUp = rootViewDF.findViewById<RadioButton>(R.id.followUpRadioButton)
        val tdlRadioButtonGroup = rootViewDF.findViewById<RadioGroup>(R.id.todoListStatusRadioButtonGroup)
        val tdlCloseImage = rootViewDF.findViewById<ImageView>(R.id.tdlDetailCloseImageView)

        val getArgs = arguments!!
        getToDoMessage = getArgs.getString("toDoMessage")!!
        getAssignBy = getArgs.getString("assignBy")!!
        getAssignDateTime = getArgs.getString("assignDateTime")!!
        getToDoMessageID = getArgs.getString("toDoMessageID")!!
        getToDoComment = getArgs.getString("toDoComment")!!
        getToDoStatus = getArgs.getString("toDoStatus")!!

        tdmMessage.text = getToDoMessage
        tdmAssignBy.text = getAssignBy
        tdmComment.setText(getToDoMessage)
        tdmDate.text = getAssignDateTime.substring(0, 10)

        doAsync {
            syncToDoListStatus()
        }

        when (getToDoStatus) {
            "D" -> {
                tdlSubmit.visibility = View.GONE
                tdlComplete.isChecked = true
                tdlFollowUp.keyListener = null
                tdmComment.keyListener = null
                todoListStats = "D"
            }
            "F" -> {
                tdlFollowUp.isChecked = true
                tdlComplete.keyListener = null
                tdmComment.keyListener = null
                tdlSubmit.visibility = View.GONE
                todoListStats = "F"
            }
        }

        tdlSubmit.setOnClickListener {
            doAsync {
                syncToDoListCompleteUpdate()
                uiThread {
                    if (todoMessageCompleteUpdate) {
                        Utils.showToastMessage(
                            context!!.applicationContext,
                            getString(R.string.operation_success)
                        )
                        dialog?.dismiss()
                    } else Utils.showToastMessage(
                        context!!.applicationContext,
                        "Error"
                    )
                }
            }
        }

        tdlRadioButtonGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.completeRadioButton -> todoListStats = "D"
                R.id.followUpRadioButton -> todoListStats = "F"
            }
        }

        tdlCloseImage.setOnClickListener {
            dialog?.dismiss()
        }

        return rootViewDF
    }

    fun setOnDismissListener(onDismissListener: DialogInterface.OnDismissListener) {
        this.onDismissListener = onDismissListener
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        onDismissListener.onDismiss(dialog)
    }

    override fun onStart()
    {
        super.onStart()
        val dialog = dialog
        if (dialog != null)
        {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window!!.setLayout(width, height)
        }
    }

    private fun syncToDoListStatus() {
        val SOAP_ACTION = Utils.SOAP_NAMESPACE + Utils.READ_UPDATE_SYNC
        val soapObject = SoapObject(
            Utils.SOAP_NAMESPACE,
            Utils.READ_UPDATE_SYNC
        )
        val url = GeneralClass.sync_url + Utils.TODO_MSG_URL

        try {
            soapObject.addProperty("toDoID", getToDoMessageID)
            soapObject.addProperty("companyID", "OTG")
            soapObject.addProperty("readGpsPoint", "16.7768085,96.1399496")
            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
            envelope.implicitTypes = true
            envelope.setOutputSoapObject(soapObject)
            envelope.dotNet = true
            val httpTransportSE = HttpTransportSE(url)
            httpTransportSE.debug = true
            httpTransportSE.call(SOAP_ACTION, envelope)
        } catch (ex: Exception) {
            Utils.makeLog(ex.toString())
            Crashlytics.log(ex.toString())
        }
    }

    private fun syncToDoListCompleteUpdate() {
        val SOAP_ACTION = Utils.SOAP_NAMESPACE + Utils.COMPLETE_MSG_SYNC
        val soapObject = SoapObject(
            Utils.SOAP_NAMESPACE,
            Utils.COMPLETE_MSG_SYNC
        )
        val url = GeneralClass.sync_url + Utils.TODO_MSG_URL

        try {
            soapObject.addProperty("toDoID", getToDoMessageID)
            soapObject.addProperty("companyID", "OTG")
            soapObject.addProperty("completeMessage", todoListDetailCommentEditText.text.toString())
            soapObject.addProperty("toDoStatus", todoListStats)
            soapObject.addProperty("completeGpsPoint", "16.7768085,96.1399496")
            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
            envelope.implicitTypes = true
            envelope.setOutputSoapObject(soapObject)
            envelope.dotNet = true
            val httpTransportSE = HttpTransportSE(url)
            httpTransportSE.debug = true
            httpTransportSE.call(SOAP_ACTION, envelope)
            todoMessageCompleteUpdate = envelope.response.toString().toBoolean()
        } catch (ex: Exception) {
            Utils.makeLog(ex.toString())
            Crashlytics.log(ex.toString())
        }
    }
}
