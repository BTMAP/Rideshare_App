package com.uwi.btmap.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uwi.btmap.R
import com.uwi.btmap.model.Commutes

class MyAdapter(private val commuteList : ArrayList<Commutes>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.commute_item,
            parent,false)
        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentItem = commuteList[position]

        holder.driverName.text = currentItem.driverName
        holder.passengerName.text = currentItem.passengerName
        holder.date.text = currentItem.commuteDate
        holder.time.text = currentItem.commuteTime

    }

    override fun getItemCount(): Int {

        return commuteList.size
    }


    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val driverName : TextView = itemView.findViewById(R.id.driverName)
        val passengerName : TextView = itemView.findViewById(R.id.passengerName)
        val date : TextView = itemView.findViewById(R.id.commuteDate)
        val time : TextView = itemView.findViewById(R.id.commuteTime)

    }

}
