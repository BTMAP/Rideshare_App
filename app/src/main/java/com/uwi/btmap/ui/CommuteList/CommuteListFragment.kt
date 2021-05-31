package com.uwi.btmap.ui.CommuteList

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.uwi.btmap.R
import com.uwi.btmap.adapter.MyAdapter
import com.uwi.btmap.model.Commutes

class CommuteListFragment : Fragment() {

    companion object {
        fun newInstance() = CommuteListFragment()
    }

    private lateinit var viewModel: CommuteListViewModel
    private lateinit var commuteView: RecyclerView
    private lateinit var commuteArrayList: ArrayList<Commutes>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.commute_list_fragment, container, false)

        view.findViewById<RecyclerView>(R.id.recyclerCommuteList)
        commuteView.layoutManager = LinearLayoutManager(this.activity)
        commuteView.setHasFixedSize(true)
        commuteArrayList = arrayListOf<Commutes>()
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CommuteListViewModel::class.java)
        // TODO: Use the ViewModel

        viewModel.getUserData()
    }
    }