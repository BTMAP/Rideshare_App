package com.uwi.btmap.ui.AboutUs

<<<<<<< HEAD
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.uwi.btmap.R

=======
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
<<<<<<<< HEAD:app/src/main/java/com/uwi/btmap/ui/AboutUs/AboutUsFragment.kt
========
import android.widget.Button
>>>>>>>> 205e1c5ae536bde12fd6731b514ccf2f77b2cc09:app/src/main/java/com/uwi/btmap/ui/commute_list/CommuteListFragment.kt
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uwi.btmap.activities.MapActivity
import com.uwi.btmap.activities.RegisterCommuteActivity
import com.uwi.btmap.R

<<<<<<<< HEAD:app/src/main/java/com/uwi/btmap/ui/AboutUs/AboutUsFragment.kt
>>>>>>> 205e1c5ae536bde12fd6731b514ccf2f77b2cc09

class AboutUsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about_us, container, false)

<<<<<<< HEAD
=======
        view?.findViewById(R.id.link) as TextView
========
class CommuteListFragment : Fragment(R.layout.fragment_commute_list) {

    private lateinit var commutes: List<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addCommuteButton = view.findViewById<Button>(R.id.add_commute_button)
        val recyclerView = view.findViewById<RecyclerView>(R.id.commute_recycler_view)
        commutes = listOf("Commute 1","Commute 2","Commute 3")
        Log.d("TAG", "onViewCreated: ${commutes.size}")
        val adapter = CommutesAdapter(commutes)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        addCommuteButton.setOnClickListener{
            //switch to register commute activity
            val intent: Intent = Intent(requireContext(),RegisterCommuteActivity::class.java)
            startActivity(intent)
        }
    }

    class CommutesAdapter (private val commutes:List<String>) :RecyclerView.Adapter<CommutesAdapter.ViewHolder>(){
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CommutesAdapter.ViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val commuteView = inflater.inflate(R.layout.commute_list_item,parent,false)
            return ViewHolder(commuteView)
        }

        override fun onBindViewHolder(holder: CommutesAdapter.ViewHolder, position: Int) {
            val commute = commutes[position]
            val textView = holder.textView
            textView.text = commute

        }

        override fun getItemCount(): Int {
            return commutes.size
        }
        
        inner class ViewHolder(listItemView:View):RecyclerView.ViewHolder(listItemView){
            val textView: TextView = itemView.findViewById<TextView>(R.id.commute_item_text)

            init {
                listItemView.setOnClickListener{
                    //switch to map activity
                    val intent: Intent = Intent(listItemView.context,MapActivity::class.java)
                    listItemView.context.startActivity(intent)
                }
            }
        }

>>>>>>>> 205e1c5ae536bde12fd6731b514ccf2f77b2cc09:app/src/main/java/com/uwi/btmap/ui/commute_list/CommuteListFragment.kt
>>>>>>> 205e1c5ae536bde12fd6731b514ccf2f77b2cc09
    }
}