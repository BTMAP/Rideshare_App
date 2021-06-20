package com.uwi.btmap.views.fragments.selectPair

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.type.DateTime
import com.uwi.btmap.R
import com.uwi.btmap.models.PairableCommute
import com.uwi.btmap.viewmodels.RegisterCommuteViewModel
import com.uwi.btmap.viewmodels.SelectPairViewModel
import com.uwi.btmap.views.activities.MapActivity
import com.uwi.btmap.views.fragments.commuteList.CommuteListFragment
import java.text.SimpleDateFormat
import java.util.*

const val TAG = "ListCommutePair"
class ListCommutePairFragment : Fragment(R.layout.fragment_list_commute_pair_fragement) {

    private lateinit var viewModel: SelectPairViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(SelectPairViewModel::class.java)
        Log.d(TAG, "onCreate: ${viewModel.commuteOptions.value}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.commute_pair_recycler_view)
        val adapter = viewModel.commuteOptions.value?.let { CommutePairAdapter(it.pairs) }
        Log.d(TAG, "onViewCreated: after adapter - ${viewModel.commuteOptions.value}")
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.commuteOptions().observe(requireActivity(),Observer{
            Log.d(TAG, "onViewCreated: observer - ${viewModel.commuteOptions.value}")
            recyclerView.adapter = viewModel.commuteOptions.value?.let { CommutePairAdapter(it.pairs) }
        })
    }
}

class CommutePairAdapter(private val commutes: List<PairableCommute>) :
    RecyclerView.Adapter<CommutePairAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommutePairAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val commuteView = inflater.inflate(R.layout.commute_pair_list_item, parent, false)
        return ViewHolder(commuteView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val commute = commutes[position]
        val indexTextView = holder.indexTextView
        val startTextView = holder.startTextView
        val etaTextView = holder.etaTextView

        val inFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        val outFormat = SimpleDateFormat("hh:mm a")

        val start: Date = inFormat.parse(commute.time)
        val eta: Date = inFormat.parse(commute.eta)

        val startTime = outFormat.format(start)
        val etaTime = outFormat.format(eta)

        indexTextView.text = position.toString()
        startTextView.text = "Driver Start Time: $startTime"
        etaTextView.text = "Driver ETA: $etaTime"
    }

    override fun getItemCount(): Int {
        return commutes.size
    }

    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val indexTextView: TextView = itemView.findViewById(R.id.item_index)
        val startTextView: TextView = itemView.findViewById(R.id.start_time)
        val etaTextView: TextView = itemView.findViewById(R.id.eta)

        init {
            listItemView.setOnClickListener {

            }
        }
    }

}