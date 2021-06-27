//package com.uwi.btmap.views.adapter
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.uwi.btmap.R
//import com.uwi.btmap.models.Trip
//
//class MyAdapter(private val commuteList : ArrayList<Trip>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
//
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
//
//        val itemView = LayoutInflater.from(parent.context).inflate(
//            R.layout.commute_item,
//            parent,false)
//        return MyViewHolder(itemView)
//
//    }
//
//    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//
//        val currentItem = commuteList[position]
//
//        holder.origin.text = currentItem.originName
//        holder.destination.text = currentItem.destinationName
//        holder.date.text = currentItem.dateTime.toString()
//        holder.time.text = currentItem.dateTime.toString()
//
//    }
//
//    override fun getItemCount(): Int {
//
//        return commuteList.size
//    }
//
//
//    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
//
//        val origin : TextView = itemView.findViewById(R.id.originName)
//        val destination : TextView = itemView.findViewById(R.id.destinationName)
//        val date : TextView = itemView.findViewById(R.id.commuteDate)
//        val time : TextView = itemView.findViewById(R.id.commuteTime)
//
//    }
//
//}
