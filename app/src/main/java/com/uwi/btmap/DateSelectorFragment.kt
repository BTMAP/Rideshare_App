package com.uwi.btmap

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import com.uwi.btmap.BLL.CommuteViewModel
import java.util.*

private const val TAG = "DateSelectorFragment"

class DateSelectorFragment : Fragment() {
    private lateinit var viewModel : CommuteViewModel

    private lateinit var dateButton: Button

    private lateinit var datePickerDialog: DatePickerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //init vars from bundle
        }

        viewModel = ViewModelProvider(requireActivity()).get(CommuteViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_date_selector, container, false)

        initDatePicker()

        dateButton = view.findViewById(R.id.date_picker_button)
        dateButton.setOnClickListener {
            datePickerDialog.show()
        }

        viewModel.dateString().observe(requireActivity(),Observer{
            dateButton.text = it
        })

        return view
    }

    private fun initDatePicker(){
        var dateSetListener = DatePickerDialog.OnDateSetListener{ datePicker: DatePicker, day: Int, month: Int, year: Int ->
            viewModel.setDate(year,month,day)
        }

        var year = viewModel.calendar.get(Calendar.YEAR)
        var month = viewModel.calendar.get(Calendar.MONTH)
        var day = viewModel.calendar.get(Calendar.DAY_OF_MONTH)

        var style = AlertDialog.THEME_HOLO_LIGHT

        datePickerDialog = DatePickerDialog(requireContext(),style,dateSetListener,year,month,day)
    }
}