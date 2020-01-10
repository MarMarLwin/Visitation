package com.onthego.onthegovisitation.Fragments

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.crashlytics.android.Crashlytics
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.onthego.onthegovisitation.*
import com.onthego.onthegovisitation.Common.LocaleHelper
import com.onthego.onthegovisitation.Models.ToDoMessage
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE

/**
 * created by AKThura
 * 11.6.2019
 */
class HomeFragment : Fragment()
{
    private lateinit var rootView : View
    private lateinit var recView : RecyclerView
    private lateinit var todoListManager : RecyclerView.LayoutManager
    private var toDoMessage : List<ToDoMessage> = listOf()
    lateinit var todoListAdapter : ToDoListAdapter
    private var filterStatus : String = "C"


    //for language change
    override fun onAttach(context : Context)
    {
        super.onAttach(LocaleHelper.onAttach(context))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View?
    {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)
        val currentCheckInTime = rootView.findViewById<TextView>(R.id.checkInTimeTxtView)
        val currentCheckOutTime = rootView.findViewById<TextView>(R.id.checkOutTimeTxtView)
        val todoListRefreshLayout = rootView.findViewById<SwipeRefreshLayout>(R.id.todoListRecyclerViewRefreshLayout)
        val getCheckInTime = activity !!.intent.getStringExtra(GeneralClass.checkInTime)
        val getCheckOutTime = Utils.getPreference(context !!.applicationContext, GeneralClass.checkOutTime)
        val spinnerFilter = rootView.findViewById<Spinner>(R.id.filterSpinner)

        ArrayAdapter.createFromResource(context!!, R.array.todo_list_filter, android.R.layout.simple_spinner_dropdown_item).also()
        {
            adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFilter.adapter = adapter
        }

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onNothingSelected(parent : AdapterView<*>?)
            {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
            override fun onItemSelected(parent : AdapterView<*>?, view : View?, position : Int, id : Long)
            {
                when (spinnerFilter.selectedItem.toString())
                {
                    "Today" -> filterStatus = "C"
                    "Past" -> filterStatus = "P"
                    "Future" -> filterStatus = "F"
                }
                refreshToDoList()
            }
        }

        when
        {
            ! (getCheckInTime.isNullOrEmpty()) ->
            {
                currentCheckInTime.text = Utils.timeFormatConverter(getCheckInTime)
                Utils.savePreference(context !!.applicationContext, GeneralClass.checkInTime, getCheckInTime)
            }
            else -> currentCheckInTime.text = Utils.timeFormatConverter(Utils.getPreference(context !!.applicationContext, GeneralClass.checkInTime) !!)
        }

        //refreshToDoList()

        if (! getCheckOutTime.isNullOrEmpty()) currentCheckOutTime.text = Utils.timeFormatConverter(getCheckOutTime)

        todoListRefreshLayout.setOnRefreshListener {
            refreshToDoList()
            todoListRefreshLayout.isRefreshing = false
        }

        return rootView
    }

    override fun onCreateOptionsMenu(menu : Menu, inflater : MenuInflater)
    {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.attendance_out, menu)
    }

    override fun onOptionsItemSelected(item : MenuItem) : Boolean
    {
        when (item.itemId)
        {
            R.id.attendanceOut ->
            {
                AttendanceDialogFragment().show(activity !!.supportFragmentManager, resources.getString(R.string.attendance_out))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume()
    {
        super.onResume()
        refreshToDoList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshToDoList()
    {
        doAsync {
            syncToDoList(filterStatus)
            uiThread {
                bindAdaptor()
                todoListAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun bindAdaptor()
    {
        todoListManager = LinearLayoutManager(context)
        todoListAdapter = ToDoListAdapter(toDoMessage)
        recView = rootView.findViewById<RecyclerView>(R.id.todoListRecyclerView).apply {
            layoutManager = todoListManager
            adapter = todoListAdapter
        }
        todoListAdapter.setOnItemClickListener(object : ToDoListAdapter.OnItemClickListener
        {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemClick(todoMsg : ToDoMessage)
            {
                val args = Bundle()
                args.putString("toDoMessageID", todoMsg.ToDoID)
                args.putString("assignDateTime", todoMsg.AssignDateTime)
                args.putString("assignBy", todoMsg.AssignByUserID)
                args.putString("toDoMessage", todoMsg.ToDoMsg)
                args.putString("toDoComment", todoMsg.CompleteMessage)
                args.putString("toDoStatus", todoMsg.ToDoStatus)
                val activity = context as AppCompatActivity
                val myFrag = ToDoListDetailFragment()

                myFrag.arguments = args
                myFrag.isCancelable = false
                myFrag.setOnDismissListener(DialogInterface.OnDismissListener {
                    doAsync {
                        syncToDoList(filterStatus)
                        uiThread {
                            bindAdaptor()
                        }
                    }
                })
                myFrag.show(activity.supportFragmentManager, "Dialog")
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun syncToDoList(messagePeriod : String)
    {
        val SOAP_ACTION = Utils.SOAP_NAMESPACE + Utils.TODO_SYNC
        val soapObject = SoapObject(Utils.SOAP_NAMESPACE, Utils.TODO_SYNC)
        val url = GeneralClass.sync_url + Utils.TODO_MSG_URL

        try
        {
            soapObject.addProperty("companyID", Utils.getPreference(context !!.applicationContext, GeneralClass.companyID))
            soapObject.addProperty("deviceID", Utils.deviceSerialNum())
            soapObject.addProperty("userID", Utils.getPreference(context !!.applicationContext, GeneralClass.userName))
            soapObject.addProperty("msgPeriod", messagePeriod)
            val gson = GsonBuilder().create()
            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
            envelope.implicitTypes = true
            envelope.setOutputSoapObject(soapObject)
            envelope.dotNet = true
            val httpTransportSE = HttpTransportSE(url)
            httpTransportSE.debug = true
            httpTransportSE.call(SOAP_ACTION, envelope)
            val objectResult = envelope.response.toString()
            val collectionType = object : TypeToken<Collection<ToDoMessage>>()
            {}.type
            toDoMessage = gson.fromJson(objectResult, collectionType)
        }
        catch (ex : Exception)
        {
            Utils.makeLog(ex.toString())
            Crashlytics.log(ex.toString())
        }
    }
}
