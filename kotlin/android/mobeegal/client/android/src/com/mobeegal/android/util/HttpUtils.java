package com.mobeegal.android.util;

/*
<!--
$Id:: HttpUtils.java 6 2008-08-12 16:41:53Z muthu.ramadoss                      $: Id of last commit
$Rev:: 6                                                                        $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-12 22:11:53 +0530 (Tue, 12 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class HttpUtils
{

    public static String httpGet(String baseUri, String uriPath)
    {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 4000);
        HttpConnectionParams.setSoTimeout(params, 4000);
        return httpGet(baseUri, uriPath, new DefaultHttpClient(params));
    }

    public static String httpGet(String baseUri, String uriPath,
            HttpClient client)
    {

        String responseString = "";
        try
        {
            HttpGet httpget = new HttpGet(
                    baseUri + uriPath);
            HttpResponse response = client.execute(httpget);

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()), 8192);
            String line;


            while ((line = in.readLine()) != null)
            {
                responseString += line;
                //Log.d("serveResponse", line);

            }
        }
        catch (Exception e)
        {
            Log.e("HttpUtils", e + " " + e.getMessage());
        }
        return responseString;
    }

    public static String getResponseString(HttpResponse response)
    {
        String rResult = null;
        if (response != null)
        {
            try
            {
                StringBuilder rStr;
                rStr = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent()), 8192);
                String line;
                while ((line = in.readLine()) != null)
                {
                    rStr.append(line);
                }
                rResult = rStr.toString();
                in.close();
            }
            catch (Exception ex)
            {
                Log.e("FindAndInstall", "Exception:", ex);
            }
        }
        //Log.d(TAG, "getResponseString: " + rResult);
        return rResult;
    }
}