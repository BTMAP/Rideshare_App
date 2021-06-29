package com.uwi.btmap.views.fragments.commuteList

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.uwi.btmap.R
import com.uwi.btmap.models.Commute
import com.uwi.btmap.viewmodels.MainViewModel
import com.uwi.btmap.views.activities.PreviewCommuteActivity
import com.uwi.btmap.views.activities.RegisterCommuteActivity
import kotlinx.android.synthetic.main.fragment_commute_list.*

class CommuteListFragment : Fragment(R.layout.fragment_commute_list) {

    private lateinit var viewModel: MainViewModel
    private lateinit var card : CardView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        val mAuth = FirebaseAuth.getInstance()
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            viewModel.getUserCommutes(userId)
        } else {
            Toast.makeText(requireContext(), "Unable to retrieve data", Toast.LENGTH_SHORT).show()
        }

//        val addCommuteButton = view.findViewById<Button>(R.id.add_commute_button)
        val recyclerView = view.findViewById<RecyclerView>(R.id.commute_recycler_view)

        val adapter = viewModel.commutes.value?.let { CommutesAdapter(it.commutes) }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        add_commute.setOnClickListener {
            //switch to register commute activity
            val intent: Intent = Intent(requireContext(), RegisterCommuteActivity::class.java)
            startActivity(intent)
        }

        viewModel.getCommutesSuccess.observe(requireActivity(), Observer { it ->
            if (it) {
                val adapter = viewModel.commutes.value?.let { CommutesAdapter(it.commutes) }
                recyclerView.adapter = adapter

            } else {
                //TODO toast?
            }
        })

    }

    class CommutesAdapter(private val commutes: List<Commute>) : RecyclerView.Adapter<CommutesAdapter.ViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommutesAdapter.ViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val commuteView = inflater.inflate(R.layout.commute_list_item, parent, false)


            return ViewHolder(commuteView)
        }

        override fun onBindViewHolder(holder: CommutesAdapter.ViewHolder, position: Int) {
            val commute = commutes[position]
            val commuteType = holder.commuteType
            val commuteOrigin = holder.commuteOrigin
            val commuteDestination = holder.commuteDestination
            val commuteTime = holder.commuteTime
            val card = holder.card

            commuteOrigin.text = commute.originAddress
            commuteDestination.text = commute.destinationAddress
            commuteTime.text = commute.time

            val type = commute.commuteType

            if (type == 0){
                commuteType.text = "Driver"

            }else{
                commuteType.text = "Passenger"
            }

            val mCommuteType = commutes[position].getCommuteType()

            if (mCommuteType == 1){
                card.setCardBackgroundColor(Color.rgb(0, 102, 0))
            }

        }

        override fun getItemCount(): Int {
            return commutes.size
        }

        inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
            val commuteType: TextView = itemView.findViewById<TextView>(R.id.commute_type)
            val commuteOrigin: TextView = itemView.findViewById<TextView>(R.id.commute_origin)
            val commuteDestination: TextView = itemView.findViewById<TextView>(R.id.commute_destination)
            val commuteTime: TextView = itemView.findViewById<TextView>(R.id.commute_time)

            val card: CardView = itemView.findViewById<CardView>(R.id.commute_list_card)



            init {
                listItemView.setOnClickListener {
                    //switch to map activity
                    val commute = commutes[adapterPosition]
                    val intent: Intent =
                        Intent(listItemView.context, PreviewCommuteActivity::class.java)
                            .putExtra("Commute", commute)

                    listItemView.context.startActivity(intent)
                }
            }
        }

    }
}
