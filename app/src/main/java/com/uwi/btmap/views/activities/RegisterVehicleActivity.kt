package com.uwi.btmap.views.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.uwi.btmap.MainActivity
import com.uwi.btmap.R
import com.uwi.btmap.databinding.ActivityRegisterVehicleBinding
import com.uwi.btmap.models.UserVehicle
import kotlinx.android.synthetic.main.activity_register_vehicle.*
import kotlinx.android.synthetic.main.activity_update_profile.*
import kotlinx.android.synthetic.main.activity_update_profile.profile_image
import kotlinx.android.synthetic.main.activity_update_profile.selectProfilePhoto_btn
import java.util.*

class RegisterVehicleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterVehicleBinding
    private lateinit var database: DatabaseReference

    var mAuth: FirebaseAuth? = null

    companion object {
        val TAG = "RegisterVehicleActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setTitle("Register Vehicle")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding = ActivityRegisterVehicleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Profile Photo
        selectProfilePhoto_btn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        //Display Phone Number
        mAuth = FirebaseAuth.getInstance()

        if (mAuth?.currentUser != null) {
            val phoneNumber = findViewById<TextView>(R.id.userPhoneNo)
            phoneNumber.text = mAuth?.currentUser?.phoneNumber.toString()
        }

        database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(mAuth?.currentUser?.uid!!).get().addOnSuccessListener {
            if (it.exists()) {
                val name = it.child("name").value
                binding.vehicleUserName.text = name.toString()
            }
        }

        //Update vehicle information
        database = FirebaseDatabase.getInstance().getReference("VehicleData")
        database.child(mAuth?.currentUser?.uid!!).get().addOnSuccessListener {
            if (it.exists()) {
                val vehicleImage = it.child("vehicleImageUrl").value

                Glide.with(this)
                    .load(vehicleImage)
                    .into(profile_image)

                val vehicleModel = it.child("vehicleModel").value
                val vehicleLicensePlate = it.child("vehicleLicensePlate").value
                val vehicleColor = it.child("vehicleColor").value
                val vehicleMake = it.child("vehicleMake").value

                editText_vehicleModel.setText(vehicleModel.toString())
                editText_vehicleColor.setText(vehicleColor.toString())
                editText_vehicleLicensePlateNumber.setText(vehicleLicensePlate.toString())
                editText_vehicleMake.setText(vehicleMake.toString())

            } else {
                Toast.makeText(this, "Vehicle Doesn't Exist", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }

        binding.saveBtn.setOnClickListener {
            showProgressBar()
            database = FirebaseDatabase.getInstance().getReference("VehicleData")
            database.child(mAuth?.currentUser?.uid!!).get().addOnSuccessListener {
                if (it.exists()) {
                    saveVehicleToFirebaseDatabaseWIthExistingPhoto()
                } else {
                    uploadImageToFirebaseStorage()
                }
            }
        }

    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            profile_image.setImageBitmap(bitmap)

            selectProfilePhoto_btn.alpha = 0f
        }
    }

    private fun uploadImageToFirebaseStorage() {

        if (selectedPhotoUri == null)
            return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    saveVehicleToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
            }
    }


    private fun saveVehicleToFirebaseDatabase(vehicleImageUrl: String) {
        val vehicleModel = binding.editTextVehicleModel.text.toString()
        val vehicleColor = binding.editTextVehicleColor.text.toString()
        val vehicleLicensePlate = binding.editTextVehicleLicensePlateNumber.text.toString()
        val vehicleMake = binding.editTextVehicleMake.text.toString()

        database = FirebaseDatabase.getInstance().getReference("VehicleData")
        val vehicleInfo =
            UserVehicle(
                vehicleModel,
                vehicleLicensePlate,
                vehicleImageUrl,
                vehicleColor,
                vehicleMake
            )
        database.child(mAuth?.currentUser?.uid!!).setValue(vehicleInfo).addOnSuccessListener {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener {

            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()

        }
    }

    private fun saveVehicleToFirebaseDatabaseWIthExistingPhoto() {

        val vehicleModel = binding.editTextVehicleModel.text.toString()
        val vehicleColor = binding.editTextVehicleColor.text.toString()
        val vehicleLicensePlate = binding.editTextVehicleLicensePlateNumber.text.toString()
        val vehicleMake = binding.editTextVehicleMake.text.toString()

        database = FirebaseDatabase.getInstance().getReference("VehicleData")
        database.child(mAuth?.currentUser?.uid!!).get().addOnSuccessListener {
            if (it.exists()) {
                val vehicleImage = it.child("vehicleImageUrl").value.toString()
                val vehicleInfo =
                    UserVehicle(
                        vehicleModel,
                        vehicleLicensePlate,
                        vehicleImage,
                        vehicleColor,
                        vehicleMake
                    )
                database.child(mAuth?.currentUser?.uid!!).setValue(vehicleInfo).addOnSuccessListener {

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                    Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show()

                }.addOnFailureListener {

                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()

                }
            }
        }
    }

    private fun showProgressBar() {
        regProgressBar1.visibility = View.VISIBLE
        regProgressBar2.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        regProgressBar1.visibility = View.GONE
        regProgressBar2.visibility = View.GONE
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