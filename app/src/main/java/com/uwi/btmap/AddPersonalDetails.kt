package com.uwi.btmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddPersonalDetails : AppCompatActivity() {

    var auth : FirebaseAuth? = null
    var databaseRef : FirebaseDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_personal_details)

        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance()
        val ref = databaseRef?.reference!!.child("users")


        val saveButton : Button = findViewById(R.id.saveButton)
        val firstName : EditText = findViewById(R.id.firstName)
        val lastName : EditText = findViewById(R.id.lastName)
        val emailAddress : EditText = findViewById(R.id.emailAddress)

        saveButton.setOnClickListener{
            val firstName = firstName.text.toString()
            val lastName = lastName.text.toString()
            val emailAddress = emailAddress.text.toString()

            val tableRef = ref.child(auth?.currentUser?.uid!!)
            tableRef?.child("first_name").setValue(firstName)
            tableRef?.child("last_name").setValue(lastName)
            tableRef?.child("email_address").setValue(emailAddress)

            finish()
        }
    }
}