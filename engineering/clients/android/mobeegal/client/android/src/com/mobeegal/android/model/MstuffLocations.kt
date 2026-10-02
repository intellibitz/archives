/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mobeegal.android.model

import com.google.android.maps.GeoPoint

/**
 * @author gunasekaran
 */
class MstuffLocations(
    private val userid: String?,
    private val catagory: String?,
    private val name: String?,
    private val latitude: Int,
    private val longitude: Int,
    private val location: String?
) {
    private val point: GeoPoint = GeoPoint(latitude.toInt(), longitude.toInt())

    fun getPoint(): GeoPoint {
        return point
    }

    fun getLatitude(): Int {
        return latitude
    }

    fun getLongitude(): Int {
        return longitude
    }

    fun getName(): String? {
        return name
    }

    fun getUserid(): String? {
        return userid
    }

    fun getLocation(): String? {
        return location
    }

    fun getCatagory(): String? {
        return catagory
    }
}
