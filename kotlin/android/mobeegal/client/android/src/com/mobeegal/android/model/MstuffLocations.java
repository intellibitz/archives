/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mobeegal.android.model;

import com.google.android.maps.GeoPoint;

/**
 * @author gunasekaran
 */
public class MstuffLocations
{
    private GeoPoint point;
    private String name;
    private String location;
    private String catagory;
    private int latitude;
    private int longitude;
    private String userid;

    public MstuffLocations(String userid, String catagory, String name,
            int latitude, int longitude, String location)
    {
        this.name = name;
        this.catagory = catagory;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userid = userid;
        point = new GeoPoint((int) (latitude), (int) (longitude));
        this.location = location;

    }

    public GeoPoint getPoint()
    {
        return point;
    }

    public int getLatitude()
    {
        return latitude;
    }

    public int getLongitude()
    {
        return longitude;
    }

    public String getName()
    {
        return name;
    }

    public String getUserid()
    {
        return userid;
    }

    public String getLocation()
    {
        return location;
    }

    public String getCatagory()
    {
        return catagory;
    }
}
