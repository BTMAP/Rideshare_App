package com.uwi.btmap.ui.CommuteList

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.uwi.btmap.R

class CommuteListFragment : Fragment() {

    companion object {
        fun newInstance() = CommuteListFragment()
    }

    private lateinit var viewModel: CommuteListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.commute_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CommuteListViewModel::class.java)
        // TODO: Use the ViewModel
    }

}