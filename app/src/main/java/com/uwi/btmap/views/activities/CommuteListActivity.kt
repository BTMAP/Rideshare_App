//package com.uwi.btmap.views.activities
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.firebase.database.*
//import com.uwi.btmap.R
//import com.uwi.btmap.models.Trip
//import com.uwi.btmap.views.adapter.MyAdapter
//
//class CommuteListActivity : AppCompatActivity() {
//    private lateinit var database: DatabaseReference
//    private lateinit var commuteView: RecyclerView
//    private lateinit var commuteArrayList: ArrayList<Trip>
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_commute_list)
//
//        commuteView = findViewById(R.id.recyclerCommuteList)
//        commuteView.layoutManager = LinearLayoutManager(this)
//        commuteView.setHasFixedSize(true)
//
//        commuteArrayList = arrayListOf<Trip>()
//        getUserData()
//    }
//
//    private fun getUserData() {
//
//        database = FirebaseDatabase.getInstance().getReference("Commute Collection")
//        database.addValueEventListener(object : ValueEventListener {
//
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    for (userSnapshot in snapshot.children) {
//                        val commuteList = userSnapshot.getValue(Trip::class.java)
//                        commuteArrayList.add(commuteList!!)
//                    }
//                    commuteView.adapter = MyAdapter(commuteArrayList)
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//        })
//    }
//}