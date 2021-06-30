package com.uwi.btmap.views.fragments.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.uwi.btmap.R
import com.uwi.btmap.viewmodels.RegisterCommuteViewModel
import com.uwi.btmap.views.activities.RegisterCommuteActivity

private const val TAG = "TypeSelectorFragment"

class TypeSelectorFragment : Fragment() {

    private lateinit var nextButton: Button

    private lateinit var viewModel: RegisterCommuteViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
        viewModel = ViewModelProvider(requireActivity()).get(RegisterCommuteViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_type_selector, container, false)

        nextButton = view.findViewById(R.id.sel_next_btn)

        val driverCheckBox = view.findViewById<CheckBox>(R.id.driverCheckBox)
        val passengerCheckBox = view.findViewById<CheckBox>(R.id.passengerCheckBox)

        driverCheckBox?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                passengerCheckBox.isChecked = false
                viewModel.setCommuteType(0)

            }
        }

        passengerCheckBox?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                driverCheckBox.isChecked = false
                viewModel.setCommuteType(1)

            }
        }

        nextButton.setOnClickListener{
            (activity as RegisterCommuteActivity?)?.setNextPage()
        }

        return view
    }

}