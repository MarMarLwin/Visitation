package com.onthego.onthegovisitation.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout
import com.onthego.onthegovisitation.CustomerDeliFragment
import com.onthego.onthegovisitation.MapFragment
import com.onthego.onthegovisitation.R
import com.onthego.onthegovisitation.ViewPagerAdapter

/**
 * created by AKThura
 * 11.6.2019
 */
class RouteFragment : androidx.fragment.app.Fragment()
{
    private lateinit var tabLayout : TabLayout
    private lateinit var viewPager : androidx.viewpager.widget.ViewPager
    override fun onCreateView(
            inflater : LayoutInflater, container : ViewGroup?,
            savedInstanceState : Bundle?
    ) : View?
    {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_route, container, false)

        viewPager = rootView.findViewById(R.id.viewpager)
        setupViewPager(viewPager)
        tabLayout = rootView.findViewById(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)

        return rootView
    }

    private fun setupViewPager(viewPager : androidx.viewpager.widget.ViewPager)
    {
        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(CustomerDeliFragment(), getString(R.string.list))
        adapter.addFragment(MapFragment(), getString(R.string.map))
        viewPager.adapter = adapter
    }
}
