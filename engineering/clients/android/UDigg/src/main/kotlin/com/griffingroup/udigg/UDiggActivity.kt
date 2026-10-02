package com.griffingroup.udigg

import android.content.Intent
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

class UDiggActivity : AppCompatActivity() {

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
                    "Digg most popular right now - next in: ${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                startActivity(Intent("com.griffingroup.udigg"))
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

    private fun getEvents(): String? {
        val url = "http://services.digg.com/2.0/digg.getAll?count=10"
        return try {
            HttpManager.execute(url)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting digg events: ${e.message}")
            null
        }
    }

    private fun renderEvents(events: String?) {
        val mimeType = "text/html"
        val encoding = "UTF-8"
        val webView = findViewById<WebView>(R.id.events) ?: return

        try {
            if (events == null) {
                webView.loadData("Oops.. API error. Please report by email below", mimeType, encoding)
            } else {
                val jsonObject = JSONObject(events)
                val asOf = Calendar.getInstance().time.toString()
                findViewById<TextView>(R.id.as_of)?.text = "as of: $asOf"

                val diggsJsonArray = jsonObject.getJSONArray("diggs")
                val builder = StringBuilder("<html><body><p> ")
                val len = diggsJsonArray.length()

                for (i in 0 until len) {
                    val obj = diggsJsonArray.getJSONObject(i)
                    val itemJson = obj.getJSONObject("item")
                    val url = itemJson.getString("href")
                    val name = itemJson.getString("title")
                    val thumbnail = itemJson.optJSONObject("thumbnail")
                    val img = thumbnail?.optString("src", "") ?: ""
                    val ht = thumbnail?.optString("height", "50") ?: "50"
                    val wd = thumbnail?.optString("width", "50") ?: "50"

                    builder.append("<a href=\"").append(url).append("\">")
                    if (img.isNotEmpty()) {
                        builder.append("<img src=\"").append(img).append("\"")
                            .append(" height=").append(ht)
                            .append(" width=").append(wd)
                            .append(" /> ")
                    }
                    builder.append(name).append("</a><br>")
                    if (i < len - 1) {
                        builder.append("<br>")
                    }
                }
                builder.append(" </p></body></html>")
                webView.loadData(builder.toString(), mimeType, encoding)
            }
        } catch (e: JSONException) {
            Log.e(TAG, "JSON parsing error: ${e.message}")
            webView.loadData("Oops.. Data load error. Please report by email below", mimeType, encoding)
        }
    }

    companion object {
        private const val TAG = "UDiggActivity"
    }
}
