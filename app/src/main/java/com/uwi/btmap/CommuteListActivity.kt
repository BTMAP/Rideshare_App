package com.uwi.btmap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.uwi.btmap.adapter.MyAdapter
import com.uwi.btmap.model.Commutes

class CommuteListActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var commuteView: RecyclerView
    private lateinit var commuteArrayList: ArrayList<Commutes>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_commute_list)

        commuteView = findViewById(R.id.recyclerCommuteList)
        commuteView.layoutManager = LinearLayoutManager(this)
        commuteView.setHasFixedSize(true)

        commuteArrayList = arrayListOf<Commutes>()
        getUserData()
    }

    private fun getUserData() {

        database = FirebaseDatabase.getInstance().getReference("Commutes")
        database.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val commuteList = userSnapshot.getValue(Commutes::class.java)
                        commuteArrayList.add(commuteList!!)
                    }
                    commuteView.adapter = MyAdapter(commuteArrayList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}