package com.example.applicationtrackerapprevised

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.games.PlayGames
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date

class ApplicationStatusActivity() : AppCompatActivity() {

    private lateinit var listViewAdapter : ExpandableListViewAdapter
    private lateinit var expListView : ExpandableListView
    private var companiesList : MutableList<String> = mutableListOf<String>()
    private var detailsList : HashMap<String, MutableList<String>> = mutableMapOf<String, MutableList<String>>() as HashMap<String, MutableList<String>>

    // TASK BAR BUTTONS
    private lateinit var calendarNavButton : Button
    private lateinit var addAppButton : Button

    // Shared preferences stuff & context
    private var hasAppStatus : Int = 0
    private var status : Boolean = false

    // Firebase stuff


    // TEST CASE LISTS
    private var companiesListTest : MutableList<String> = arrayListOf("Apple", "Samsung", "Dell", "Intel")
    private var detailsListTest : HashMap<String, MutableList<String>> = mutableMapOf<String, MutableList<String>>() as HashMap<String, MutableList<String>>

    override fun onCreate(savedInstanceState : Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application_status)

        // Firebase stuff
        var fb : FirebaseDatabase = FirebaseDatabase.getInstance()
        var ref : DatabaseReference = fb.getReference()
        Log.w("FIREBASE TESTING", ref.toString())

        var listener : DataListener = DataListener()
        ref.addValueEventListener(listener)
   

        // GET EXPANDABLE LIST VIEW
        expListView = findViewById(R.id.expandable_list)

        // GET BUTTONS
        calendarNavButton = findViewById(R.id.CP_calendar_navigation_button)
        addAppButton = findViewById(R.id.CP_form_navigation_button)

        // HANDLE BUTTONS
        calendarNavButton.setOnClickListener { goToCalendar() }
        addAppButton.setOnClickListener { goToAddApp() }


        // SET list

//        if (companiesList.isNotEmpty() && detailsList.isNotEmpty()) {
//            listViewAdapter = ExpandableListViewAdapter(this, companiesList, detailsList)
//            expListView.setAdapter(listViewAdapter)
//        }
    }

    // GO TO CALENDAR PAGE
    fun goToCalendar() {
        var intent : Intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)    }

    // GO TO ADD APPLICATION PAGE
    fun goToAddApp() {
        var intent : Intent = Intent(this, ApplicationFormActivity::class.java)
        this.startActivity(intent)
    }

    // GO TO LEADERBOARD PAGE
    fun goToLeaderboard(v: View) {
        PlayGames.getLeaderboardsClient(this)
            .getLeaderboardIntent(getString(R.string.leaderboard_id))
            .addOnSuccessListener { intent -> startActivityForResult(intent!!, 9004) }
    }

    // FILL TEST CASE LISTS
    private fun testList() {
        var specsList = mutableListOf<String>("JOB TYPE", "LOCATION", "PAY")

        for (company in companiesListTest) {
            detailsListTest.put(company, specsList)
        }
    }

    private fun displayList () {
//        var pref : SharedPreferences = getSharedPreferences(this.packageName + "_preferences", Context.MODE_PRIVATE)
//        hasAppStatus = pref.getInt("AppNumStatus", -1)

        if (companiesList.isNotEmpty() && detailsList.isNotEmpty()) {
            listViewAdapter = ExpandableListViewAdapter(this, companiesList, detailsList)
            expListView.setAdapter(listViewAdapter)
        }

//        if (!status) {
//            var toast : Toast = Toast.makeText(this, "No applications have been added.", Toast.LENGTH_LONG)
//            toast.show()
//
//        } else {
//            listViewAdapter = ExpandableListViewAdapter(this, companiesList, detailsList)
//            expListView.setAdapter(listViewAdapter)
//
//        }
    }

    inner class DataListener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.value != null){
                status = true

                val snapString = snapshot.value.toString()
                Log.w("FIREBASE DATALISTENER", snapString)

                // PARSE COMPANY NAMES, TYPES
                val pattern = "\"name\":\"([^\"]+)\",\"type\":\"([^\"]+)\",\"pay\":\"([^\"]*)\",\"location\":\"([^\"]*)\",\"date\":\"([^\"]*)\",\"notes\":\"([^\"]*)\"".toRegex()
//                companiesList = mutableListOf<String>()
//                detailsList = mutableMapOf<String, MutableList<String>>() as HashMap<String, MutableList<String>>

                pattern.findAll(snapString).forEach { matchResult ->
                    val name = matchResult.groupValues[1]
                    companiesList.add(name)

                    // INITIALIZE HASHMAP
                    detailsList.put(name, mutableListOf<String>())

                    val type = matchResult.groupValues[2]
                    detailsList.get(name)?.add("TYPE: " + type)

                    var pay = matchResult.groupValues[3]
                    if (pay == null || pay.isEmpty() || pay.isBlank()){
                        pay = "N/A"
                    }
                    detailsList.get(name)?.add("PAY: " + pay)

                    var location = matchResult.groupValues[4]
                    if (location == null || location.isEmpty() || location.isBlank()){
                        location = "N/A"
                    }

                    detailsList.get(name)?.add("LOCATION: " + location)

                    var dueDate = matchResult.groupValues[5]
                    if (dueDate == null || dueDate.isEmpty() || dueDate.isBlank()) {
                        dueDate = "N/A"
                    }
                    detailsList.get(name)?.add("DUE DATE: " + dueDate)

                    var notes = matchResult.groupValues[6]
                    if (notes == null || notes.isEmpty() || notes.isBlank()) {
                        notes = "N/A"
                    }
                    detailsList.get(name)?.add("NOTES: " + notes)

                }
                Log.w("FIREBASE DL", companiesList.joinToString(", "))
                Log.w("FIREBASE DL", detailsList.get("hi")!!.joinToString(", "))

            } else {
                status = false
            }

            listViewAdapter = ExpandableListViewAdapter(this@ApplicationStatusActivity, companiesList, detailsList)
            expListView.setAdapter(listViewAdapter)

        }

        override fun onCancelled(error: DatabaseError) {
            Log.w( "ApplicationStatusActivity", "error: " + error.message )
        }
    }
}