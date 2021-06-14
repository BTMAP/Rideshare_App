package com.uwi.btmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.uwi.btmap.model.UserType
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : AppCompatActivity() {

    private lateinit var addTestData: FrameLayout
    private lateinit var database: DatabaseReference

    var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setTitle("Test Page")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_test)

        mAuth = FirebaseAuth.getInstance()

        addTestData = findViewById(R.id.addTestData)
        BottomSheetBehavior.from(addTestData).apply {
            peekHeight = 200
        }

        val driverCheckBox = findViewById<CheckBox>(R.id.driverCheckBox)
        driverCheckBox?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                passengerCheckBox.setChecked(false)

                val value = UserType(driver = true, passenger = false)
                database = FirebaseDatabase.getInstance().getReference("CommuteType")
                database.child(mAuth?.currentUser?.uid!!).setValue(value)
            }
        }

        val passengerCheckBox = findViewById<CheckBox>(R.id.passengerCheckBox)
        passengerCheckBox?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                driverCheckBox.setChecked(false)

                val value = UserType(driver = false, passenger = true)
                database = FirebaseDatabase.getInstance().getReference("CommuteType")
                database.child(mAuth?.currentUser?.uid!!).setValue(value)
            }
        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}