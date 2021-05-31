package com.uwi.btmap.ui.CommuteList

import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.uwi.btmap.adapter.MyAdapter
import com.uwi.btmap.model.Commutes

class CommuteListViewModel : ViewModel() {
    private lateinit var database: DatabaseReference
    private lateinit var commuteView: RecyclerView
    private lateinit var commuteArrayList: ArrayList<Commutes>

    fun getUserData() {

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