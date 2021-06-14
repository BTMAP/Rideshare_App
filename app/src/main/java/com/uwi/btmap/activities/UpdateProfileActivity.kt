package com.uwi.btmap.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.uwi.btmap.MainActivity
import com.uwi.btmap.R
import com.uwi.btmap.databinding.ActivityUpdateProfileBinding
import com.uwi.btmap.model.User
import kotlinx.android.synthetic.main.activity_update_profile.*
import java.util.*

class UpdateProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateProfileBinding
    private lateinit var database: DatabaseReference

    var mAuth: FirebaseAuth? = null


    companion object {
        val TAG = "UpdateProfileActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setTitle("Update Profile")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Profile Photo
        selectphoto_btn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }


        //Display Phone Number
        mAuth = FirebaseAuth.getInstance()

        if(mAuth?.currentUser != null) {
            val phoneNumber = findViewById<TextView>(R.id.userPhoneNo)
            phoneNumber.text = mAuth?.currentUser?.phoneNumber.toString()
        }

        //Update user information
        database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(mAuth?.currentUser?.uid!!).get().addOnSuccessListener {
            if (it.exists()) {
                val profilePhoto = it.child("profileImageUrl").value

                Glide.with(this)
                    .load(profilePhoto)
                    .into(profile_image)

                val name = it.child("name").value
                val bio = it.child("bio").value
                val address = it.child("address").value
                val email = it.child("email").value

                binding.userName.text = name.toString()
                editText_name.setText(name.toString())
                editText_address.setText(address.toString())
                editText_bio.setText(bio.toString())
                editText_email.setText(email.toString())
            } else {
                Toast.makeText(this, "User Doesn't Exist", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }

        binding.saveBtn.setOnClickListener {
            uploadImageToFirebaseStorage()
        }

    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            profile_image.setImageBitmap(bitmap)

            selectphoto_btn.alpha = 0f
        }
    }

    private fun uploadImageToFirebaseStorage() {

        if (selectedPhotoUri == null)
            return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to upload image to storage: ${it.message}")
            }
    }



    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val name = binding.editTextName.text.toString()
        val bio = binding.editTextBio.text.toString()
        val address = binding.editTextAddress.text.toString()
        val email = binding.editTextEmail.text.toString()

        database = FirebaseDatabase.getInstance().getReference("Users")
        val userInfo = User(name, bio, address, email, profileImageUrl)
        database.child(mAuth?.currentUser?.uid!!).setValue(userInfo).addOnSuccessListener {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener {

            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onStart() {
        super.onStart()
        if (profile_image == null){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Required")
            builder.setMessage("A profile photo is required. For more information, visit the about page.")

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            }
            builder.show()
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