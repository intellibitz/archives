package com.mobeegal.android.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;

public class SendLocation
        extends BroadcastReceiver
{

    String latitude;
    String longitude;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        try
        {
            HttpClient httpclient;

            httpclient = new DefaultHttpClient();
            Bundle bundle = intent.getExtras();
            if (bundle != null)
            {
                latitude = bundle.getString("latitude");
                longitude = bundle.getString("longitude");
            }
            NameValuePair[] data = {new BasicNameValuePair("id", "3334"),
                    new BasicNameValuePair("latitude", latitude),
                    new BasicNameValuePair("longitude", longitude),
            };
            HttpPost httpPost =
                    new HttpPost("http://38.105.84.198/controller.php");
//            httpPost.setQueryString(data);
            HttpResponse httpResponse = httpclient.execute(httpPost);
            Toast.makeText(context,
                    "latitude : " + latitude + " Longitude : " + longitude,
                    Toast.LENGTH_SHORT).show();
        }
        catch (IOException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
