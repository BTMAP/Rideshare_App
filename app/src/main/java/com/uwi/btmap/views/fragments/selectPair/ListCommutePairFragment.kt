package com.uwi.btmap.views.fragments.selectPair

import android.app.PendingIntent.getActivity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uwi.btmap.MainActivity
import com.uwi.btmap.R
import com.uwi.btmap.models.PairableCommute
import com.uwi.btmap.viewmodels.SelectPairViewModel
import com.uwi.btmap.views.activities.RegisterCommuteActivity
import com.uwi.btmap.views.fragments.commuteList.PreviewCommutePairFragment
import java.security.AccessController.getContext
import java.text.SimpleDateFormat
import java.util.*


const val TAG = "ListCommutePair"
class ListCommutePairFragment : Fragment(R.layout.fragment_list_commute_pair_fragement) {

    private lateinit var viewModel: SelectPairViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(SelectPairViewModel::class.java)
        viewModel.currentFragment.value = 0

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.commute_pair_recycler_view)
        val adapter = viewModel.commuteOptions.value?.let { CommutePairAdapter(it.pairs) }
        Log.d(TAG, "onViewCreated: after adapter - ${viewModel.commuteOptions.value}")
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.commuteOptions().observe(requireActivity(), Observer {
            Log.d(TAG, "onViewCreated: observer - ${viewModel.commuteOptions.value}")
            Log.d(TAG, "onViewCreated: test${viewModel.commuteOptions.value?.pairs}")

            if (viewModel.commuteOptions.value?.pairs.isNullOrEmpty()){
                //Toast.makeText(requireContext(), "No Pairs", Toast.LENGTH_SHORT).show()
                val builder = AlertDialog.Builder(requireContext())
                builder.setCancelable(false)
                builder.setTitle("No Available Pairs")
                builder.setMessage("Do you wish to change your commute details?")

                builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                    val intent = Intent(requireContext(), RegisterCommuteActivity::class.java)
                    startActivity(intent)
                }

                builder.setNegativeButton(android.R.string.no) { dialog, which ->
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                }

                builder.show()
            }
            recyclerView.adapter =
                viewModel.commuteOptions.value?.let { CommutePairAdapter(it.pairs) }
        })
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

            indexTextView.text = (position + 1).toString()
            startTextView.text = "$startTime"
            etaTextView.text = "$etaTime"

            holder.index = position

            holder.itemView.setOnClickListener{
                val activity = it.context as AppCompatActivity
                val fragment = PreviewCommutePairFragment()


                fragment.arguments = bundleOf("PairIndex" to position)
                activity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.pair_select_fragment, fragment)
                    .commit()
            }

            if(commutes.isEmpty())
            {
                val test = commutes.size
                Log.d(TAG, "No commutes: $test")
            }

        }

        override fun getItemCount(): Int {
            return commutes.size
        }

        inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
            val indexTextView: TextView = itemView.findViewById(R.id.item_index)
            val startTextView: TextView = itemView.findViewById(R.id.start_time)
            val etaTextView: TextView = itemView.findViewById(R.id.eta)

            var index: Int = 0
        }
    }

}

