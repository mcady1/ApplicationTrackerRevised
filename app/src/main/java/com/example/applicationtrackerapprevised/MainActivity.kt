package com.example.applicationtrackerapprevised


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.games.AuthenticationResult
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.tasks.Task

class MainActivity : AppCompatActivity() {

    private lateinit var adView : AdView
    private lateinit var calendarView : CalendarView
    private lateinit var calendar : Calendar

    // TASK BAR BUTTONS
    private lateinit var applicationStatusButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PlayGamesSdk.initialize(this);

        authenticateGoogle()

        // CREATING THE AD
        adView = AdView(this)
        var adSize = AdSize(AdSize.FULL_WIDTH, AdSize.AUTO_HEIGHT)
        adView.setAdSize(adSize)
        var adUnitId = "ca-app-pub-3940256099942544/6300978111"
        adView.adUnitId = adUnitId
        var builder = AdRequest.Builder()
        builder.addKeyword("job").addKeyword("career")
        var request = builder.build()
        var adLayout : LinearLayout = findViewById(R.id.ad_view)
        adLayout.addView(adView)
        adView.loadAd(request)

        // CREATING THE CALENDAR
        calendarView = findViewById(R.id.calendar_view)
        calendar = Calendar.getInstance()
        calendarView.setOnDateChangeListener{
                calendarView, year, month, day ->
            Log.w("MainActivity",
                "SELECTED DATE: $year/$month/$day"
            )
        }



        // APPLICATION STATUS BUTTON CLICKED -> CHANGE VIEW
        applicationStatusButton = findViewById(R.id.CP_applications_navigation_button)
        applicationStatusButton.setOnClickListener { goToApplicationStatus() }

        // applicationForm BUTTON CLICKED -> CHANGE VIEW
        var applicationFormButton: Button = findViewById(R.id.CP_form_navigation_button)
        applicationFormButton.setOnClickListener { goToApplicationForm() }

    }

    fun goToApplicationStatus() {
        var intent : Intent = Intent(this, ApplicationStatusActivity::class.java)
        this.startActivity(intent)
    }

    fun goToApplicationForm() {
        var intent : Intent = Intent(this, ApplicationFormActivity::class.java)
        this.startActivity(intent)
    }

    fun goToLeaderboard(v: View) {
        PlayGames.getLeaderboardsClient(this)
            .getLeaderboardIntent(getString(R.string.leaderboard_id))
            .addOnSuccessListener { intent -> startActivityForResult(intent!!, 9004) }
    }

    fun authenticateGoogle() {
        val gamesSignInClient = PlayGames.getGamesSignInClient(this)
        gamesSignInClient.isAuthenticated()
            .addOnCompleteListener { isAuthenticatedTask: Task<AuthenticationResult> ->
                val isAuthenticated = isAuthenticatedTask.isSuccessful &&
                        isAuthenticatedTask.result.isAuthenticated
                if (isAuthenticated) {
                    // Continue with Play Games Services
                } else {
                    gamesSignInClient.signIn()
                }
            }
    }

    override fun onPause() {
        if(adView != null)
            adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if(adView != null)
            adView.resume()
    }

    override fun onDestroy() {
        if(adView != null)
            adView.destroy()
        super.onDestroy()
    }
}