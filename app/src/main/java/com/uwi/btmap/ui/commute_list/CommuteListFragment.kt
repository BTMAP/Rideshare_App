package com.uwi.btmap.ui.commute_list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uwi.btmap.Activities.MapActivity
import com.uwi.btmap.Activities.RegisterCommuteActivity
import com.uwi.btmap.R

class CommuteListFragment : Fragment(R.layout.fragment_commute_list) {

    private lateinit var commutes: List<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addCommuteButton = view.findViewById<Button>(R.id.add_commute_button)
        val recyclerView = view.findViewById<RecyclerView>(R.id.commute_recycler_view)
        commutes = listOf("Commute 1","Commute 2","Commute 3")
        Log.d("TAG", "onViewCreated: ${commutes.size}")
        val adapter = CommutesAdapter(commutes)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        addCommuteButton.setOnClickListener{
            //switch to register commute activity
            val intent: Intent = Intent(requireContext(),RegisterCommuteActivity::class.java)
            startActivity(intent)
        }
    }

    class CommutesAdapter (private val commutes:List<String>) :RecyclerView.Adapter<CommutesAdapter.ViewHolder>(){
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CommutesAdapter.ViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val commuteView = inflater.inflate(R.layout.commute_list_item,parent,false)
            return ViewHolder(commuteView)
        }

        override fun onBindViewHolder(holder: CommutesAdapter.ViewHolder, position: Int) {
            val commute = commutes[position]
            val textView = holder.textView
            textView.text = commute

        }

        override fun getItemCount(): Int {
            return commutes.size
        }
        
        inner class ViewHolder(listItemView:View):RecyclerView.ViewHolder(listItemView){
            val textView: TextView = itemView.findViewById<TextView>(R.id.commute_item_text)

            init {
                listItemView.setOnClickListener{
                    //switch to map activity
                    val intent: Intent = Intent(listItemView.context,MapActivity::class.java)
                    listItemView.context.startActivity(intent)
                }
            }
        }

    }
}