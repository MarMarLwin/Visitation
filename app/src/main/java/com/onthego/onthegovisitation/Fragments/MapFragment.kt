package com.onthego.onthegovisitation

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import com.onthego.onthegovisitation.Models.Customer

/**
 * created by AKThura
 * 11.6.2019
 *
 */
class MapFragment : Fragment(), OnMapReadyCallback,
        GoogleMap.OnMyLocationClickListener
{
    private lateinit var mMap : GoogleMap
    override fun onCreateView(
            inflater : LayoutInflater, container : ViewGroup?,
            savedInstanceState : Bundle?
    ) : View?
    {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return rootView
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap : GoogleMap)
    {
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        mMap.setOnMyLocationClickListener(this)

        mMap.setInfoWindowAdapter(CustomMapInfoWindowAdapter(context !!.applicationContext))
        val customer = Customer(
            "001",
            "100001",
            "JC",
            "abc",
            "Bokyote Street, La Thar, Yangon",
            "00232",
            "Ygn",
            "Myanmar",
            "09400123456",
            "ddd@gmail.com",
            "www.ggg.com",
            "16.7783552,96.1412033",
            "true"
        )
        val m1 : Marker = mMap.addMarker(
                MarkerOptions().position(
                        LatLng(
                                16.77515504,
                                96.14208646
                        )
                ).title(customer.CustomerName).icon(
                        BitmapDescriptorFactory.fromResource(
                                R.drawable.ic_customer_location
                        )
                )
        )
        m1.tag = customer
        val customer1 = Customer(
            "001",
            "100001",
            "JC",
            "abc",
            "Bokyoe Street, La Thar, Yangon",
            "00232",
            "Ygn",
            "Myanmar",
            "09400123456",
            "ddd@gmail.com",
            "www.ggg.com",
            "16.7783552,97.1412033",
            "true"
        )
        val m2 : Marker = mMap.addMarker(
                MarkerOptions().position(
                        LatLng(
                                16.77231051,
                                96.15306924
                        )
                ).title(customer1.CustomerName).icon(
                        BitmapDescriptorFactory.fromResource(
                                R.drawable.ic_customer_location
                        )
                )
        )
        m2.tag = customer1
        val location = LocationServices.getFusedLocationProviderClient(this.activity !!).lastLocation
        location.addOnCompleteListener {
            if (it.isSuccessful && it.result != null)
            {
                val lastLocation = it.result

                mMap.addMarker(
                        MarkerOptions().position(
                                LatLng(
                                        lastLocation !!.latitude,
                                        lastLocation.longitude
                                )
                        ).icon(

                                BitmapDescriptorFactory.fromResource(R.drawable.ic_standing_man)
                        )
                )

                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                        lastLocation!!.latitude,
                                        lastLocation.longitude
                                ), 14.0f
                        )
                )
            }
        }
    }

    override fun onMyLocationClick(location : Location)
    {
        Toast.makeText(context, "Current location:\n $location", Toast.LENGTH_LONG).show()
    }
}
