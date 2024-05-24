package com.example.applicationtrackerapprevised

import android.content.Context
import android.content.Intent

import android.content.SharedPreferences
import android.os.Build

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.games.PlayGames
import org.json.JSONObject
import java.util.Calendar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import com.google.firebase.database.*

class ApplicationFormActivity: AppCompatActivity() {

    lateinit var jobType: String
    var num_applied: Int = 0
    var applicationGoal : Int = 10
    lateinit var progressBar : ProgressBar
    lateinit var prefs : SharedPreferences
    override fun onCreate(savedInstanceState : Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_application_form)
        prefs = getSharedPreferences(this.packageName + "_preferences", Context.MODE_PRIVATE)
        // CREATING THE PROGRESS BAR
        progressBar = findViewById(R.id.progress_bar)
        progressBar.max = applicationGoal
        num_applied = prefs.getInt("num_applied", 0)
        progressBar.progress = num_applied

        //dealing with the spinner (for the type of job)
        val spinner: Spinner = findViewById(R.id.type)
        val jobs = arrayOf("Full Time", "Part Time")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, jobs)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                jobType = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        val datePicker = findViewById<DatePicker>(R.id.date)
        val today = Calendar.getInstance()
        datePicker.updateDate(Calendar.YEAR, today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))

        // APPLICATION STATUS BUTTON CLICKED -> CHANGE VIEW
        var applicationStatusButton: Button = findViewById(R.id.CP_applications_navigation_button)
        applicationStatusButton.setOnClickListener { goToApplicationStatus() }

        // calendarForm BUTTON CLICKED -> CHANGE VIEW
        var calendarFormButton: Button = findViewById(R.id.CP_calendar_navigation_button)
        calendarFormButton.setOnClickListener { goToCalendarForm() }

    }

    fun goToApplicationStatus() {
        var intent : Intent = Intent(this, ApplicationStatusActivity::class.java)
        this.startActivity(intent)
    }

    fun goToCalendarForm() {
        var intent : Intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addApplication(v: View)  {

        val editor = prefs.edit()

        var nameView: EditText = findViewById(R.id.name)
        var payView: EditText = findViewById(R.id.pay)
        var locationView: EditText = findViewById(R.id.location)
        var dateView: DatePicker = findViewById(R.id.date)
        var notesView: EditText = findViewById(R.id.notes)

        var name = nameView.text
        var pay = payView.text
        var location = locationView.text
        var month = dateView.month + 1
        var day = dateView.dayOfMonth
        var year = dateView.year
        var date = "$month/$day/$year"
        var notes = notesView.text

        var application = JSONObject()
        application.put("name", name)
        application.put("type", jobType)
        application.put("pay", pay)
        application.put("location", location)
        application.put("date", date)
        application.put("notes", notes)

        try {
            //get applications list array
//            var gson = Gson()
//            var jsonListString = prefs.getString("appList", null)
//            lateinit var jsonList: ArrayList<String>
//            if(jsonListString == null) {
//                jsonList = ArrayList<String>()
//            } else {
//                var type = object : TypeToken<ArrayList<String>>() {}.type
//                jsonList = gson.fromJson(jsonListString, type)
//            }

            var firebase : FirebaseDatabase =
                FirebaseDatabase.getInstance()

            var id_generator_ref: DatabaseReference = firebase.getReference("id_gen")
            var id_fetch: String? = ""

            val application_string = application.toString()

            id_generator_ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    id_fetch = dataSnapshot.getValue(String::class.java)
                    if(id_fetch == null) {
                        var id_value = "0"
                        id_generator_ref.setValue(id_value)
                        var application_reference: DatabaseReference = firebase.getReference(id_value)
                        application_reference.setValue(application_string)
                    } else {
                        Log.w("ELSE BLOCK ENTERED?", "YES")
                        var id_value = (id_fetch.toString().toInt() + 1).toString()
                        Log.w("id_value in else block: ", id_value)
                        id_generator_ref.setValue(id_value)
                        var application_reference: DatabaseReference = firebase.getReference(id_value)
                        application_reference.setValue(application_string)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })

            Log.w("id_value after function: ", "WHATTTTT")
//            Log.w("id_value after function: ", "$id_value bootsy")

            //add application to list and save to editor
//            jsonList.add(applicationString)
//            var json = gson.toJson(jsonList) //turns this into json string
//            editor.putString("appList", json)
//            editor.apply()

            var current_streak = prefs.getInt("streak", 0)
            var last_updated = prefs.getString("last_updated", "01-01-1999")

            val formatter = SimpleDateFormat("MM-dd-yyyy")
            val last_updated_date : Date = formatter.parse(last_updated)

            val date = Calendar.getInstance()
            date.add(Calendar.DATE, -1)
            if (last_updated_date < date.time) {
                var editor = prefs.edit()
                editor.putInt("streak", 1)
                editor.putString(
                    "last_updated",
                    LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
                )
                editor.apply()

                PlayGames.getLeaderboardsClient(this)
                    .submitScore(getString(R.string.leaderboard_id), 1)
            }

            else {
                var editor = prefs.edit()
                editor.putInt("streak", current_streak + 1)
                editor.putString("last_updated", formatter.format(Calendar.DATE))
                editor.apply()

                PlayGames.getLeaderboardsClient(this)
                    .submitScore(getString(R.string.leaderboard_id), (current_streak + 1).toLong())
            }

            if (num_applied < 15) {
                num_applied = prefs.getInt("num_applied", 0) + 1
                var editor = prefs.edit()
                editor.putInt("num_applied", num_applied)
                editor.apply()
                progressBar.progress = num_applied
            }


        } catch (e: Exception) {
            Log.w("ERROR WITH SHARED PREFS", e)
        }

        Log.w("JSON OBJECT CHECK!", application.toString())
    }

    override fun onResume() {
        super.onResume()
        num_applied = prefs.getInt("num_applied", 0)
        progressBar.progress = num_applied
    }

    override fun onStart() {
        super.onStart()
        num_applied = prefs.getInt("num_applied", 0)
        progressBar.progress = num_applied
    }

    override fun onRestart() {
        super.onRestart()
        num_applied = prefs.getInt("num_applied", 0)
        progressBar.progress = num_applied
    }


    fun goToLeaderboard(v: View) {
        PlayGames.getLeaderboardsClient(this)
            .getLeaderboardIntent(getString(R.string.leaderboard_id))
            .addOnSuccessListener { intent -> startActivityForResult(intent!!, 9004) }
    }

}
