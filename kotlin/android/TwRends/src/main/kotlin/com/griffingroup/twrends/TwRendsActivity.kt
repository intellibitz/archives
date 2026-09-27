package com.griffingroup.twrends

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

class TwRendsActivity : AppCompatActivity() {

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

        loadTrends()

        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                findViewById<TextView>(R.id.next)?.text =
                    "Worldwide Twitter Trends - next in: ${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                startActivity(Intent("com.griffingroup.twrends"))
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

    private fun loadTrends() {
        fetchJob?.cancel()
        fetchJob = activityScope.launch {
            val trends = withContext(Dispatchers.IO) {
                getTrends()
            }
            renderTrends(trends)
        }
    }

    private fun getTrends(): String? {
        val url = "http://api.twitter.com/1/trends.json"
        return try {
            HttpManager.execute(url)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting twitter trends: ${e.message}")
            null
        }
    }

    private fun renderTrends(trends: String?) {
        val mimeType = "text/html"
        val encoding = "UTF-8"
        val webView = findViewById<WebView>(R.id.trends) ?: return

        try {
            if (trends == null) {
                webView.loadData("Oops.. API error. Please report by email below", mimeType, encoding)
            } else {
                val jsonObject = JSONObject(trends)
                val asOf = jsonObject.optString("as_of", "")
                findViewById<TextView>(R.id.as_of)?.text = "as of: $asOf"

                val trendsJson = jsonObject.getJSONArray("trends")
                val builder = StringBuilder(
                    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" " +
                        "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"> " +
                        "<html> " +
                        "<head>" +
                        "<style type=\"text/css\">" +
                        "div.ex { padding:5px; border:2px solid gray; margin:0px; }" +
                        "</style>" +
                        "</head>" +
                        "<body> "
                )
                val len = trendsJson.length()
                for (i in 0 until len) {
                    val obj = trendsJson.getJSONObject(i)
                    val url = obj.getString("url")
                    val name = obj.getString("name")
                    builder.append("<div class=\"ex\"> ")
                        .append("<a href=\"").append(url).append("\">")
                        .append(name).append("</a> ")
                        .append("</div> ")
                }
                builder.append(" </body></html>")
                webView.loadData(builder.toString(), mimeType, encoding)
            }
        } catch (e: JSONException) {
            Log.e(TAG, "JSON parsing error: ${e.message}")
            webView.loadData("Oops.. Data load error. Please report by email below", mimeType, encoding)
        }
    }

    companion object {
        private const val TAG = "TwRendsActivity"
    }
}
