package com.griffingroup.mevents

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.util.Calendar

class MEventsActivity : AppCompatActivity() {

    private var countDownTimer: CountDownTimer? = null
    private val activityScope = CoroutineScope(Dispatchers.Main + Job())
    private var fetchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.app_desc)
        setContentView(R.layout.main)
    }

    override fun onResume() {
        super.onResume()

        loadEvents()

        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                findViewById<TextView>(R.id.next)?.text =
                    "My Yahoo Upcoming Events - next in: ${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                startActivity(Intent("com.griffingroup.mevents"))
            }
        }.start()
    }

    override fun onPause() {
        countDownTimer?.cancel()
        fetchJob?.cancel()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        countDownTimer = null
        fetchJob = null
    }

    private fun loadEvents() {
        fetchJob?.cancel()
        fetchJob = activityScope.launch {
            val events = withContext(Dispatchers.IO) {
                getEvents()
            }
            renderEvents(events)
        }
    }

    private fun getEvents(): String {
        return try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val lastKnownLocation: Location? = try {
                locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (e: SecurityException) {
                Log.w(TAG, "Location permission not granted: ${e.message}")
                null
            }

            val loc = if (lastKnownLocation != null) {
                "${lastKnownLocation.latitude},${lastKnownLocation.longitude}"
            } else {
                ""
            }

            val url = "http://upcoming.yahooapis.com/services/rest/?method=event.search&api_key=0cb4d3fd50&format=json&per_page=10&location=$loc"
            HttpManager.execute(url) ?: "Could not get Upcoming Events"
        } catch (e: Exception) {
            Log.e(TAG, "Error getting events: ${e.message}")
            "Could not get Upcoming Events"
        }
    }

    private fun renderEvents(events: String) {
        val mimeType = "text/html"
        val encoding = "UTF-8"
        val trendsWebView = findViewById<WebView>(R.id.events) ?: return

        try {
            val jsonObject = JSONObject(events)
            val asOf = Calendar.getInstance().time.toString()
            findViewById<TextView>(R.id.as_of)?.text = "as of: $asOf"

            val rsp = jsonObject.getJSONObject("rsp")
            val trendsJson = rsp.getJSONArray("event")
            val builder = StringBuilder("<p> ")
            val len = trendsJson.length()

            for (i in 0 until len) {
                val obj = trendsJson.getJSONObject(i)
                val url = obj.getString("url")
                val name = obj.getString("name")
                builder.append("<a href=\"")
                    .append(url).append("\">")
                    .append(name).append("</a><br>")
                if (i < len - 1) {
                    builder.append("<br>")
                }
            }
            builder.append(" </p>")
            trendsWebView.loadData(builder.toString(), mimeType, encoding)
            trendsWebView.reload()
        } catch (e: JSONException) {
            Log.e(TAG, "JSON parsing error: ${e.message}")
            trendsWebView.loadData("Oops.. Data load error. Please report by email below", mimeType, encoding)
            trendsWebView.reload()
        }
    }

    companion object {
        private const val TAG = "MEventsActivity"
    }
}
