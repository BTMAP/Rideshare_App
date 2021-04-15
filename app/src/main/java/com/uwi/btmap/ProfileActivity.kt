package com.uwi.btmap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.StringBuilder

class ProfileActivity : AppCompatActivity() {

    lateinit var auth : FirebaseAuth
    var databaseRef : FirebaseDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance()
        val ref = databaseRef?.reference!!.child("users")

//        var currentUser = auth.currentUser

//        Reference
        var phoneNumber = findViewById<TextView>(R.id.displayPhoneNumber)
        val update = findViewById<Button>(R.id.updateProfile)
        val logout = findViewById<Button>(R.id.logout)

//        if(currentUser == null){
//            startActivity(Intent(this, PhoneAuthActivity::class.java))
//            finish()
//        }

        update.setOnClickListener{
            startActivity(Intent(this, AddPersonalDetails::class.java))
        }

        logout.setOnClickListener{
            auth.signOut()
            startActivity(Intent(this, PhoneAuthActivity::class.java))
            finish()
        }

//      Display Phone Number
        val sb = StringBuilder()
        sb.append("Logged in with: " + auth.currentUser?.phoneNumber.toString())
        sb.append("\n")

        var tableRef = ref.child(auth?.currentUser?.uid!!)

        tableRef.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                sb.append("First Name: ", snapshot.child("first_name").value.toString())
                sb.append("\n")
                sb.append("Last Name: ", snapshot.child("last_name").value.toString())
                sb.append("\n")
                sb.append("Email: ", snapshot.child("email_address").value.toString())
                sb.append("\n")
                phoneNumber.text = sb.toString()
            }
        })


    }
}