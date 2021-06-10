//package com.uwi.btmap
//
//import android.content.Intent
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.widget.Button
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase
//import com.uwi.btmap.model.User
//import com.uwi.btmap.model.UserType
//
//class CommuteTypeChoice : AppCompatActivity() {
//
//    private lateinit var database: DatabaseReference
//    var mAuth: FirebaseAuth? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_commute_type_choice)
//
//        mAuth = FirebaseAuth.getInstance()
//
//        val driver = findViewById<Button>(R.id.selectDriver)
//        val passenger = findViewById<Button>(R.id.selectPassenger)
//
//
//        driver.setOnClickListener {
//            if (mAuth?.currentUser != null) {
//                database = FirebaseDatabase.getInstance().getReference("UserCommuteType")
//                var mUserType = UserType(userType = "Driver")
//                database
//                    .child(mAuth?.currentUser?.uid!!)
//                    .setValue(mUserType).addOnSuccessListener {
//                    startActivity(Intent(applicationContext, MainActivity::class.java))
//                    finish()
//                }
//            }
//        }
//
//        passenger.setOnClickListener {
//            if (mAuth?.currentUser != null) {
//                database = FirebaseDatabase.getInstance().getReference("UserCommuteType")
//                var mUserType = UserType(userType = "Passenger")
//                database
//                    .child(mAuth?.currentUser?.uid!!)
//                    .setValue(mUserType).addOnSuccessListener {
//                        startActivity(Intent(applicationContext, MainActivity::class.java))
//                        finish()
//                    }
//            }
//        }
//    }
//}