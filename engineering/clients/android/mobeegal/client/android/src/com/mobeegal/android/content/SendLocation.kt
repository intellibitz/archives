package com.mobeegal.android.content

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import java.io.IOException

class SendLocation : BroadcastReceiver() {

    var latitude: String? = null
    var longitude: String? = null

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val httpclient: HttpClient = DefaultHttpClient()
            val bundle = intent.extras
            if (bundle != null) {
                latitude = bundle.getString("latitude")
                longitude = bundle.getString("longitude")
            }
            val data = arrayOf<NameValuePair>(
                BasicNameValuePair("id", "3334"),
                BasicNameValuePair("latitude", latitude),
                BasicNameValuePair("longitude", longitude)
            )
            val httpPost =
                HttpPost("http://38.105.84.198/controller.php")
            //            httpPost.setQueryString(data);
            val httpResponse: HttpResponse = httpclient.execute(httpPost)
            Toast.makeText(
                context,
                "latitude : " + latitude + " Longitude : " + longitude,
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: IOException) {
            e.printStackTrace() //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
