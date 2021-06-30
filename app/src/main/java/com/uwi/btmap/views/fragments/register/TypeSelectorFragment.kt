package com.uwi.btmap.views.fragments.register

import android.os.Bundle
import android.util.Log
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
//import kotlinx.android.synthetic.main.activity_test.*

private const val TAG = "TypeSelectorFragment"

class TypeSelectorFragment : Fragment() {
    private lateinit var driverButton: Button
    private lateinit var passengerButton: Button

    private lateinit var nextButton: Button

    private lateinit var viewModel: RegisterCommuteViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //set vars from bundle
        }
        viewModel = ViewModelProvider(requireActivity()).get(RegisterCommuteViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_type_selector, container, false)

//        driverButton = view.findViewById(R.id.driver_button)
//        passengerButton = view.findViewById(R.id.passenger_button)

        nextButton = view.findViewById(R.id.sel_next_btn)

//        driverButton.setOnClickListener {
//            viewModel.setCommuteType(0)
//            Log.d(TAG, "onCreateView: DriverType Set: ${viewModel.commuteType}")
//        }
//
//        passengerButton.setOnClickListener {
//            viewModel.setCommuteType(1)
//            Log.d(TAG, "onCreateView: DriverType Set: ${viewModel.commuteType}")
//        }

        val driverCheckBox = view.findViewById<CheckBox>(R.id.driverCheckBox)
        val passengerCheckBox = view.findViewById<CheckBox>(R.id.passengerCheckBox)

        driverCheckBox?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                passengerCheckBox.setChecked(false)
                viewModel.setCommuteType(0)

//                val value = UserType(driver = true, passenger = false)
//                database = FirebaseDatabase.getInstance().getReference("CommuteType")
//                database.child(mAuth?.currentUser?.uid!!).setValue(value)
            }
        }


        passengerCheckBox?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                driverCheckBox.setChecked(false)
                viewModel.setCommuteType(1)

//                val value = UserType(driver = false, passenger = true)
//                database = FirebaseDatabase.getInstance().getReference("CommuteType")
//                database.child(mAuth?.currentUser?.uid!!).setValue(value)
            }
        }

        nextButton.setOnClickListener{
            (activity as RegisterCommuteActivity?)?.setNextPage()
        }

        return view
    }

}