package com.uwi.btmap.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.uwi.btmap.R
import com.uwi.btmap.databinding.ActivityProfileBinding
import kotlinx.android.synthetic.main.activity_update_profile.*


class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var database: DatabaseReference

    var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar!!.setTitle("Profile")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        val phoneNumber = findViewById<TextView>(R.id.userPhoneNo)
        phoneNumber.text = mAuth?.currentUser?.phoneNumber.toString()

        database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(mAuth?.currentUser?.uid!!).get().addOnSuccessListener {
            if (it.exists()) {
                val name = it.child("name").value
                val occupation = it.child("occupation").value
                val address = it.child("address").value
                val email = it.child("email").value


                binding.userName.text = name.toString()
                binding.userOccupation.text = occupation.toString()
                binding.userAddress.text = address.toString()
                binding.userEmail.text = email.toString()
            }
            else {
                Toast.makeText(this, "Please update profile", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }

        //Profile Photo
        database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(mAuth?.currentUser?.uid!!).get().addOnSuccessListener {
            if (it.exists()) {
                val profilePhoto = it.child("profileImageUrl").value

                Glide.with(this)
                    .load(profilePhoto)
                    .into(profile_image)
            } else {
                Toast.makeText(this, "User Doesn't Exist", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }

        binding.updateProfile.setOnClickListener {
            val intent = Intent(this, UpdateProfileActivity::class.java)
            startActivity(intent)
        }

        binding.registerVehicleBtn.setOnClickListener {
            val intent = Intent(this, RegisterVehicleActivity::class.java)
            startActivity(intent)
        }

//        binding.button.setOnClickListener{
//            database = FirebaseDatabase.getInstance().getReference("Commutes")
//            val commuteInfo = Commutes("Chloe Sullivan", "Oliver Queen", "10-28-2021", "7:55 AM")
//            database.push().setValue(commuteInfo)
//        }
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