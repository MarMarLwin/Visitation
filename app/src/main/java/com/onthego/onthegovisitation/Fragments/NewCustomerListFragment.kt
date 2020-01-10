package com.onthego.onthegovisitation.Fragments

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.onthego.onthegovisitation.Adapters.CustomerInfoRecyclerAdapter
import com.onthego.onthegovisitation.CreateNewCustomerActivity
import com.onthego.onthegovisitation.CustomRecyclerView.IndexBarRecyclerView
import com.onthego.onthegovisitation.GeneralClass
import com.onthego.onthegovisitation.Models.Customer
import com.onthego.onthegovisitation.R
import com.onthego.onthegovisitation.Utils
import com.onthego.onthegovisitation.viewmodel.NewCustomerViewModel
import kotlinx.android.synthetic.main.fragment_customer_info.*

class NewCustomerListFragment : androidx.fragment.app.Fragment() {
    private lateinit var rootView: View
    private var searchView: SearchView? = null
    private lateinit var refreshLayout: SwipeRefreshLayout
    private var queryTextListener: SearchView.OnQueryTextListener? = null
    private var customerList: MutableList<Customer> = mutableListOf()
    lateinit var customerInfoAdapter: CustomerInfoRecyclerAdapter
    private lateinit var customerViewModel: NewCustomerViewModel

    private lateinit var recyclerView: IndexBarRecyclerView

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
        }
        if (searchView != null) {
            searchView!!.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))

            queryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    if (newText.isNotEmpty()) {
                        val searchList = customerList.filter { x ->
                            x.CustomerName.toLowerCase().contains(newText.toLowerCase()) || x.Addr.toLowerCase().contains(
                                newText.toLowerCase()
                            )
                        }.toMutableList()
                        recyclerView.adapter = CustomerInfoRecyclerAdapter(searchList)
                    } else
                        recyclerView.adapter = CustomerInfoRecyclerAdapter(customerList)
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    return true
                }
            }
            searchView!!.setOnQueryTextListener(queryTextListener)
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_new_customer -> {
                val intent = Intent(activity, CreateNewCustomerActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        customerViewModel = ViewModelProviders.of(activity!!).get(NewCustomerViewModel::class.java)

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_customer_info, container, false)
        recyclerView = rootView.findViewById(R.id.recyclerViewCustomerInfo)
        refreshLayout = rootView.findViewById(R.id.customerInfoRefreshLayout)
        refreshLayout.setOnRefreshListener {
            loadCustomerInfo()
            refreshLayout.isRefreshing = false
        }
        loadCustomerInfo()
        return rootView
    }

    private fun loadCustomerInfo() {

        val companyId = Utils.getPreference( context!!.applicationContext, GeneralClass.companyID )!!
        val deviceId = Utils.getPreference( context!!.applicationContext, GeneralClass.deviceID )
        val userId = Utils.getPreference( context!!.applicationContext, GeneralClass.userName )!!

        refreshLayout.isRefreshing = true
        customerViewModel.getNewCustomerList(companyId, deviceId, userId).observe(activity!!, Observer {
            refreshLayout.isRefreshing = false
            customerList = it as MutableList<Customer>
            prepareCustomerRecyclerView(it)
        })
    }

    //new method for binding customer info
    private fun prepareCustomerRecyclerView(customerList: List<Customer>) {
        customerInfoAdapter = CustomerInfoRecyclerAdapter(customerList as MutableList<Customer>)

        if(activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT)
            recyclerView.layoutManager = LinearLayoutManager(activity)
        else
            recyclerView.layoutManager = GridLayoutManager(activity, 2)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = customerInfoAdapter

        recyclerView.setIndexTextSize(13)
        recyclerView.setIndexBarTextColor("#5D4BD2")
        recyclerView.setIndexBarColor("#33334c")
        recyclerView.setIndexbarMargin(0F)
        recyclerView.setIndexbarMarginTop(0f)
        recyclerView.setIndexbarMarginBottom(0f)
        recyclerView.setIndexbarWidth(30f)
        recyclerView.setIndexBarTransparentValue(0.2.toFloat())

        customerInfoAdapter.notifyDataSetChanged()
        textViewTotalCustomer?.text = getString(R.string.totalCus)+"${customerList.size}"
    }
}
