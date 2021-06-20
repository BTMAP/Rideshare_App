package com.uwi.btmap.views.fragments.selectPair

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.uwi.btmap.R
import com.uwi.btmap.models.PairableCommute
import com.uwi.btmap.viewmodels.RegisterCommuteViewModel
import com.uwi.btmap.viewmodels.SelectPairViewModel
import com.uwi.btmap.views.activities.MapActivity
import com.uwi.btmap.views.fragments.commuteList.CommuteListFragment

class ListCommutePairFragment : Fragment(R.layout.fragment_list_commute_pair_fragement) {

    private lateinit var viewModel: SelectPairViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(SelectPairViewModel::class.java)
        //get list of commutes out of bundle
//        add to viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.commute_pair_recycler_view)
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
        val commuteView = inflater.inflate(R.layout.commute_list_item, parent, false)
        return ViewHolder(commuteView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val commute = commutes[position]
//        val textView = holder.textView
//        textView.text = commute

    }

    override fun getItemCount(): Int {
        return commutes.size
    }

    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val textView: TextView = itemView.findViewById<TextView>(R.id.commute_item_text)

        init {
            listItemView.setOnClickListener {
                //switch to map activity
//                val intent: Intent = Intent(listItemView.context, MapActivity::class.java)
//                listItemView.context.startActivity(intent)
            }
        }
    }

}