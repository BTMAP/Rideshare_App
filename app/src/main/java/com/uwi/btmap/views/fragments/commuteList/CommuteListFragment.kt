package com.uwi.btmap.views.fragments.commuteList

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.uwi.btmap.R
import com.uwi.btmap.models.Commute
import com.uwi.btmap.viewmodels.MainViewModel
import com.uwi.btmap.viewmodels.SelectPairViewModel
import com.uwi.btmap.views.activities.MapActivity
import com.uwi.btmap.views.activities.PreviewCommuteActivity
import com.uwi.btmap.views.activities.RegisterCommuteActivity
import kotlinx.android.synthetic.main.fragment_commute_list.*

class CommuteListFragment : Fragment(R.layout.fragment_commute_list) {

    private lateinit var viewModel:MainViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        val mAuth = FirebaseAuth.getInstance()
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            viewModel.getUserCommutes(userId)
        }else{
            //TODO toast stating unable to retrieve
        }

        val addCommuteButton = view.findViewById<Button>(R.id.add_commute_button)
        val recyclerView = view.findViewById<RecyclerView>(R.id.commute_recycler_view)

        val adapter = viewModel.commutes.value?.let { CommutesAdapter(it.commutes) }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        add_commute.setOnClickListener {
            //switch to register commute activity
            val intent: Intent = Intent(requireContext(), RegisterCommuteActivity::class.java)
            startActivity(intent)
        }

        viewModel.getCommutesSuccess.observe(requireActivity(),Observer{ it ->
            if (it){
                val adapter = viewModel.commutes.value?.let { CommutesAdapter(it.commutes) }
                recyclerView.adapter = adapter

            }else{
                //TODO toast?
            }
        })

    }

    class CommutesAdapter(private val commutes: List<Commute>) :
        RecyclerView.Adapter<CommutesAdapter.ViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CommutesAdapter.ViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val commuteView = inflater.inflate(R.layout.commute_list_item, parent, false)
            return ViewHolder(commuteView)
        }

        override fun onBindViewHolder(holder: CommutesAdapter.ViewHolder, position: Int) {
            val commute = commutes[position]
            val textView = holder.textView
            textView.text = commute.commuteId

        }

        override fun getItemCount(): Int {
            return commutes.size
        }

        inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
            val textView: TextView = itemView.findViewById<TextView>(R.id.commute_item_text)

            init {
                listItemView.setOnClickListener {
                    //switch to map activity
                    val commute = commutes[adapterPosition]
                    val intent: Intent = Intent(listItemView.context, PreviewCommuteActivity::class.java)
                        .putExtra("Commute",commute)

                    listItemView.context.startActivity(intent)
                }
            }
        }

    }
}
