package com.onthego.onthegovisitation.Adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.onthego.onthegovisitation.CustomerDetailActivity
import com.onthego.onthegovisitation.Models.Customer
import com.onthego.onthegovisitation.R

class CustomerDeliRecyclerAdapter(private val customerList : ArrayList<Customer>) :
        RecyclerView.Adapter<CustomerDeliRecyclerAdapter.ViewHolder>()
{
    //the class is holding the list view
    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        lateinit var phoneNo : ImageView
        lateinit var email : ImageView
        @SuppressLint("SetTextI18n")
        fun bindItems(customerInfo : Customer)
        {
            val customerName = itemView.findViewById<TextView>(R.id.customerNameTxtView)
            val customerAddress = itemView.findViewById<TextView>(R.id.customerAddressTxtView)
            val deliveryTime = itemView.findViewById<TextView>(R.id.deliveryTimeTxtView)
            phoneNo = itemView.findViewById(R.id.phoneImageView)
            email = itemView.findViewById(R.id.emailImageView)

            customerName.text = "#${customerInfo.CustomerID} ${customerInfo.CustomerName}"
            customerAddress.text = customerInfo.Addr
//            deliveryTime.text = customerInfo.deliTime

            itemView.setOnClickListener {
                val intent = Intent(it.context, CustomerDetailActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                intent.putExtra("latitude", customerInfo.latitude.toDouble())
//                intent.putExtra("longitude", customerInfo.longitude.toDouble())
//                intent.putExtra("name", customerInfo.customerName)
                it.context.startActivity(intent)
            }
        }
    }

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.customer_deli_detail, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder : ViewHolder, position : Int)
    {
        holder.bindItems(customerList[position])
        holder.phoneNo.setOnClickListener { v ->
            val intent = Intent(Intent.ACTION_DIAL)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.data = Uri.parse("tel:${customerList[position].Phone}")
            v.context.startActivity(intent)
        }

        holder.email.setOnClickListener { v ->
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.data = Uri.parse("mailto:aung.kyawthura.onthego@gmail.com")
            v.context.startActivity(intent)
        }
    }

    //this method is giving the size of the list
    override fun getItemCount() : Int
    {
        return customerList.size
    }
}