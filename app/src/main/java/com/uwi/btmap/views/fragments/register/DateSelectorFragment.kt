package com.uwi.btmap.views.fragments.register

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import com.uwi.btmap.R
import com.uwi.btmap.viewmodels.RegisterCommuteViewModel
import com.uwi.btmap.views.activities.RegisterCommuteActivity
import java.util.*

private const val TAG = "DateSelectorFragment"

@Suppress("DEPRECATION")
class DateSelectorFragment : Fragment() {
    private lateinit var viewModel : RegisterCommuteViewModel

    private lateinit var dateButton: Button
    private lateinit var nextButton:Button
    private lateinit var prevButton: Button

    private lateinit var datePickerDialog: DatePickerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //init vars from bundle
        }

        viewModel = ViewModelProvider(requireActivity()).get(RegisterCommuteViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_date_selector, container, false)

        initDatePicker()

        dateButton = view.findViewById(R.id.date_picker_button)
        dateButton.setOnClickListener {
            datePickerDialog.show()
        }

        nextButton = view.findViewById(R.id.date_next_btn)
        prevButton = view.findViewById(R.id.date_prev_btn)

        nextButton.setOnClickListener{
            (activity as RegisterCommuteActivity?)?.setNextPage()
        }

        prevButton.setOnClickListener{
            (activity as RegisterCommuteActivity?)?.setPrevPage()
        }

        viewModel.dateString().observe(requireActivity(),Observer{
            dateButton.text = it
        })

        return view
    }

    private fun initDatePicker(){
        val dateSetListener = DatePickerDialog.OnDateSetListener{ _: DatePicker, year: Int, month: Int, day: Int ->
            viewModel.setDate(year,month,day)
        }

        val year = viewModel.calendar.get(Calendar.YEAR)
        val month = viewModel.calendar.get(Calendar.MONTH)
        val day = viewModel.calendar.get(Calendar.DAY_OF_MONTH)

        val style = AlertDialog.THEME_HOLO_LIGHT

        datePickerDialog = DatePickerDialog(requireContext(),style,dateSetListener,year,month,day)
    }
}