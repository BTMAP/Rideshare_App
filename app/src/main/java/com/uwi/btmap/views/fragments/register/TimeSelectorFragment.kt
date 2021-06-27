package com.uwi.btmap.views.fragments.register

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.uwi.btmap.R
import com.uwi.btmap.viewmodels.RegisterCommuteViewModel
import com.uwi.btmap.views.activities.RegisterCommuteActivity
import java.util.*

private const val TAG = "TimeSelectorFragment"

class TimeSelectorFragment : Fragment() {

    private lateinit var viewModel: RegisterCommuteViewModel

    private lateinit var typeDisplay: TextView

    private lateinit var timeButton: Button
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button

    private lateinit var timePickerDialog: TimePickerDialog

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
        var view = inflater.inflate(R.layout.fragment_time_selector, container, false)
        typeDisplay = view.findViewById(R.id.commute_time_frag)

        initTimePicker()

        timeButton = view.findViewById(R.id.time_picker_button)

        timeButton.setOnClickListener {
            timePickerDialog.show()
        }

        nextButton = view.findViewById(R.id.time_next_btn)
        prevButton = view.findViewById(R.id.time_prev_btn)

        nextButton.setOnClickListener {
            (activity as RegisterCommuteActivity?)?.setNextPage()
        }

        prevButton.setOnClickListener {
            (activity as RegisterCommuteActivity?)?.setPrevPage()
        }

        viewModel.timeString().observe(requireActivity(), Observer {
            timeButton.text = it
        })

        return view
    }

    private fun initTimePicker() {
        val timeSetListener =
            TimePickerDialog.OnTimeSetListener { timePicker: TimePicker, hour: Int, minute: Int ->
                viewModel.setTime(hour, minute)
            }

        val hour = viewModel.calendar.get(Calendar.HOUR_OF_DAY)
        val minute = viewModel.calendar.get(Calendar.MINUTE)

        val style = AlertDialog.THEME_HOLO_LIGHT

        timePickerDialog =
            TimePickerDialog(requireContext(), style, timeSetListener, hour, minute, true)
    }
}