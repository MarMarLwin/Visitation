package com.onthego.onthegovisitation.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.crashlytics.android.Crashlytics
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.onthego.onthegovisitation.Models.Customer
import com.onthego.onthegovisitation.R
import com.onthego.onthegovisitation.Utils

class CustomMapInfoWindowAdapter(context : Context) : GoogleMap.InfoWindowAdapter
{
    private val cxt = context
    override fun getInfoContents(marker : Marker?) : View?
    {
        val view = LayoutInflater.from(cxt).inflate(R.layout.map_info_window, null)

        try
        {
            val customerName = view.findViewById<TextView>(R.id.customerNameTextView)
            val customerPhone = view.findViewById<TextView>(R.id.customerPhoneTextView)
            val customerEmail = view.findViewById<TextView>(R.id.customerEmailTextView)
            val customerAddress = view.findViewById<TextView>(R.id.customerAddressTextView)

            if (marker?.title == null)
                return null

            customerName.text = marker.title
            val infoWindowData = marker.tag as Customer

            customerPhone.text = infoWindowData.Phone
            customerEmail.text = infoWindowData.Email
            customerAddress.text = infoWindowData.Addr
        }
        catch (ex : Exception)
        {
            Utils.showToastMessage(cxt, ex.toString())
            Crashlytics.log(ex.toString())
        }
        return view
    }

    override fun getInfoWindow(marker : Marker?) : View?
    {
        return null
    }
}