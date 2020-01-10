package com.onthego.onthegovisitation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class ViewPagerAdapter(manager : FragmentManager) :
        androidx.fragment.app.FragmentPagerAdapter(manager)
{
    private var mFragmentList : ArrayList<Fragment> = ArrayList()
    private var mFragmentTitleList : ArrayList<String> = ArrayList()
    override fun getItem(position : Int) : Fragment
    {
        return mFragmentList[position]
    }

    override fun getCount() : Int
    {
        return mFragmentList.size
    }

    fun addFragment(fragment : Fragment, title : String)
    {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    override fun getPageTitle(position : Int) : CharSequence?
    {
        return mFragmentTitleList[position]
    }
}