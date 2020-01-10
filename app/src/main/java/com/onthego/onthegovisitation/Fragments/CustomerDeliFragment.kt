package com.onthego.onthegovisitation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.onthego.onthegovisitation.Adapters.CustomerDeliRecyclerAdapter
import com.onthego.onthegovisitation.CustomRecyclerView.IndexBarRecyclerView
import com.onthego.onthegovisitation.Models.Customer
import kotlinx.android.synthetic.main.fragment_customer_info.*
import kotlinx.android.synthetic.main.fragment_customer_todeliver.*

/**
 * Created by AKThura
 * 11.6.2019
 */
class CustomerDeliFragment : androidx.fragment.app.Fragment() {
    private lateinit var customerList: ArrayList<Customer>
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var mCustomerDeliRecyclerAdapter: CustomerDeliRecyclerAdapter
    private lateinit var recyclerView: IndexBarRecyclerView
    private lateinit var textViewTotalCustomerDeli: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_customer_todeliver, container, false)
        recyclerView = rootView.findViewById(R.id.recyclerViewCustomerToDeliver)
        refreshLayout = rootView.findViewById(R.id.refreshLayout)
        textViewTotalCustomerDeli = rootView.findViewById(R.id.textviewCustomerDeli)

        prepareCustomerRecyclerView()
        refreshLayout.setOnRefreshListener {
            Utils.showToastMessage(context!!, "Refreshing....")//to edit
            prepareCustomerRecyclerView()
            refreshLayout.isRefreshing = false
        }
        return rootView
    }

    private fun prepareCustomerRecyclerView(){
        val layoutManger = LinearLayoutManager(activity)
        customerList = ArrayList()
        recyclerView.layoutManager = layoutManger
        val customer = Customer(
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
            "16.7783552,96.1412033",
            "true"
        )
        customerList.add(customer)
        mCustomerDeliRecyclerAdapter = CustomerDeliRecyclerAdapter(customerList)
        recyclerView.adapter = mCustomerDeliRecyclerAdapter

        recyclerView.setIndexTextSize(13)
        recyclerView.setIndexBarTextColor("#5D4BD2")
        recyclerView.setIndexBarColor("#33334c")
        recyclerView.setIndexbarMargin(0F)
        recyclerView.setIndexbarMarginTop(0f)
        recyclerView.setIndexbarMarginBottom(0f)
        recyclerView.setIndexbarWidth(30f)
        recyclerView.setIndexBarTransparentValue(0.2.toFloat())
        textViewTotalCustomerDeli.text = getString(R.string.totalCus)+ mCustomerDeliRecyclerAdapter.itemCount.toString()
    }
}
