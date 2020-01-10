package com.onthego.onthegovisitation

import android.app.AlertDialog
import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.onthego.onthegovisitation.Common.BaseActivity
import com.onthego.onthegovisitation.Fragments.CustomerListFragment
import com.onthego.onthegovisitation.Fragments.HomeFragment
import com.onthego.onthegovisitation.Fragments.NewCustomerListFragment
import com.onthego.onthegovisitation.Fragments.RouteFragment
import com.onthego.onthegovisitation.Models.Customer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    /*private var doubleBackPressed = false*/
    //Navigation button listener
    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment())
                    supportActionBar!!.title = getString(R.string.app_name)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_route -> {
                    replaceFragment(RouteFragment())
                    supportActionBar!!.title = getString(R.string.title_route)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_customer -> {
                    replaceFragment(CustomerListFragment())
                    supportActionBar!!.title = getString(R.string.title_customer)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_new_customer->{
                    replaceFragment(NewCustomerListFragment())
                    supportActionBar!!.title = getString(R.string.title_new_customer)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.navigation_report -> {
                    supportActionBar!!.title = getString(R.string.title_report)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    //Check session status and control back button
    override fun onBackPressed() {
        /*if(doubleBackPressed)
        {
            super.onBackPressed()

            val currentStatus = Utils.getPreference(this, GeneralClass.userPassword)
            if (currentStatus != null)
            {
                finishAffinity()
            }
        }
        doubleBackPressed = true
        Utils.showToastMessage(this,getString(R.string.back_press_text))

        Handler().postDelayed({ doubleBackPressed = false }, 2000)*/
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Warning!")
        builder.setMessage("Are you sure to exit?")
        builder.setPositiveButton(getString(R.string.yes)) { _, _ -> finishAffinity() }
        builder.setNegativeButton(getString(R.string.no)) { _, _ -> }
        builder.create()
        builder.setCancelable(true)
        builder.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(savedInstanceState == null)
            setUpFragment(HomeFragment())
        val actionbar: androidx.appcompat.app.ActionBar? = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.setHomeAsUpIndicator(R.drawable.otglogo)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onResume() {
        if(Utils.getModelPreference(this@MainActivity,
            GeneralClass.checkInCustomer, Customer::class.java)!=null)
        setUpFragment(CustomerListFragment())
        super.onResume()
    }
    
    private fun setUpFragment(fragment: Fragment) {
        replaceFragment(fragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentFrame, fragment)
        fragmentTransaction.commit()
    }
}
