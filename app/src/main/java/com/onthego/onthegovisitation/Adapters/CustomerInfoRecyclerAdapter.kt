package com.onthego.onthegovisitation.Adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SectionIndexer
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.onthego.onthegovisitation.CustomerDetailActivity
import com.onthego.onthegovisitation.GeneralClass
import com.onthego.onthegovisitation.Models.Customer
import com.onthego.onthegovisitation.R
import com.onthego.onthegovisitation.Utils
import java.io.Serializable

class CustomerInfoRecyclerAdapter(customerLists : MutableList<Customer>) :
        RecyclerView.Adapter<CustomerInfoRecyclerAdapter.ViewHolder>(), SectionIndexer
{
    private var sectionPositions : ArrayList<Int>? = null
    private var customerList = customerLists.sortedWith(compareBy { it.CustomerName })

    //the class is holding the list view
    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        lateinit var phoneNo : ImageView
        lateinit var email : ImageView
        lateinit var cardView: CardView
        @SuppressLint("SetTextI18n")
        fun bindItems(customerInfo : Customer)
        {
            val customerName = itemView.findViewById<TextView>(R.id.customerNameTxtView)
            val customerAddress = itemView.findViewById<TextView>(R.id.customerAddressTxtView)
            phoneNo = itemView.findViewById(R.id.phoneImageView)
            email = itemView.findViewById(R.id.emailImageView)
            cardView =itemView.findViewById(R.id.cv)

            customerName.text = "#${customerInfo.CustomerID} ${customerInfo.CustomerName}"
            customerAddress.text = customerInfo.Addr

            itemView.setOnClickListener {
                val intent = Intent(it.context, CustomerDetailActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.putExtra("cusObj",customerInfo as Serializable)
                it.context.startActivity(intent)
            }
            if(Utils.getModelPreference(itemView.context,
                    GeneralClass.checkInCustomer,Customer::class.java)?.CustomerID==customerInfo.CustomerID)

            cardView.setCardBackgroundColor(Color.LTGRAY)
        }
    }

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.customer_info_detail, parent, false)
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
            intent.data = Uri.parse("mailto:${customerList[position].Email}")
            v.context.startActivity(intent)
        }
    }

    //this method is giving the size of the list
    override fun getItemCount() : Int
    {
        return customerList.size
    }

    override fun getSections() : Array<Any>
    {
        val sections = ArrayList<String>()
        sectionPositions = ArrayList()
        var i = 0
        val size = customerList.size
        while (i < size)
        {
            val section = customerList[i].CustomerName.substring(0, 1).toUpperCase()
            if (! sections.contains(section))
            {
                sections.add(section)
                sectionPositions !!.add(i)
            }
            i ++
        }
        return sections.toArray(arrayOf(String()))
    }

    override fun getSectionForPosition(p0 : Int) : Int
    {
        return 0
    }

    override fun getPositionForSection(p0 : Int) : Int
    {
        return sectionPositions !![p0]
    }
}