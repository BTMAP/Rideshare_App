package com.uwi.btmap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.uwi.btmap.activities.MapActivity
import com.uwi.btmap.activities.PhoneAuthActivity
import com.uwi.btmap.activities.ProfileActivity
import com.uwi.btmap.activities.UpdateProfileActivity
import kotlinx.android.synthetic.main.activity_update_profile.*


class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    private lateinit var database: DatabaseReference
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        mAuth = FirebaseAuth.getInstance()

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val headerView: View = navView.getHeaderView(0)
        val profileNumber: TextView = headerView.findViewById(R.id.nav_view_number)
        val profileName: TextView = headerView.findViewById(R.id.nav_view_name)
        val profileImage: ImageView = headerView.findViewById(R.id.nav_view_image)

        if (mAuth?.currentUser != null) {
            profileNumber.text = mAuth?.currentUser?.phoneNumber.toString()

            database = FirebaseDatabase.getInstance().getReference("Users")
            database.child(mAuth?.currentUser?.uid!!).get().addOnSuccessListener {
                if (it.exists()) {
                    val profilePhoto = it.child("profileImageUrl").value

                    Glide.with(this)
                        .load(profilePhoto)
                        .into(profileImage)

                    val name = it.child("name").value

                    profileName.text = name.toString()
                } else {
                    Toast.makeText(this, "User Doesn't Exist", Toast.LENGTH_SHORT).show()

                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Update Profile")
                    builder.setMessage("Please update profile information to continue.")

                    builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                        val intent = Intent(this@MainActivity, UpdateProfileActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                    builder.setNegativeButton(android.R.string.no) { dialog, which ->
                        mAuth!!.signOut()
                        startActivity(Intent(this, PhoneAuthActivity::class.java))
                        finish()
                    }

                    builder.show()

                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_commute, R.id.nav_about
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.menu.findItem(R.id.nav_profile).setCheckable(false)
        navView.menu.findItem(R.id.nav_profile).setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    drawerLayout.close()
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        navView.menu.findItem(R.id.nav_map).setCheckable(false)
        navView.menu.findItem(R.id.nav_map).setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    drawerLayout.close()
                    val intent = Intent(this, MapActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
<<<<<<< HEAD
//        navView.menu.findItem(R.id.nav_commute_list).setCheckable(false)
//        navView.menu.findItem(R.id.nav_commute_list).setOnMenuItemClickListener { item ->
//            when (item.itemId) {
//                R.id.nav_commute_list -> {
//                    drawerLayout.close()
//                    val intent = Intent(this, CommuteListActivity::class.java)
//                    startActivity(intent)
//                    true
//                }
//                else -> false
//            }
//        }
=======
        navView.menu.findItem(R.id.nav_commute_list).setCheckable(false)
        navView.menu.findItem(R.id.nav_commute_list).setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.nav_commute_list -> {
                    drawerLayout.close()
                    val intent = Intent(this, CommuteListActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
>>>>>>> 205e1c5ae536bde12fd6731b514ccf2f77b2cc09
        navView.menu.findItem(R.id.nav_logout).setCheckable(false)
        navView.menu.findItem(R.id.nav_logout).setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.nav_logout -> {
                    mAuth!!.signOut()
                    startActivity(Intent(this, PhoneAuthActivity::class.java))
                    true
                }

                else -> super.onOptionsItemSelected(item)
            }
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_logout -> {
//                mAuth!!.signOut()
//                startActivity(Intent(this, PhoneAuthActivity::class.java))
//                true
//            }
//
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        if (mAuth?.currentUser == null) {
            val intent = Intent(this@MainActivity, PhoneAuthActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}