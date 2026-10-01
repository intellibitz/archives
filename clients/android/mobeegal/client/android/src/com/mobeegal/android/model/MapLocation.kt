/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mobeegal.android.model

import com.google.android.maps.GeoPoint

/**
 * @author jailani
 */
class MapLocation(private val name: String, latitude: Double, longitude: Double) {
    private val point: GeoPoint =
        GeoPoint((latitude * 1e6).toInt(), (longitude * 1e6).toInt())

    fun getPoint(): GeoPoint {
        return point
    }

    fun getName(): String {
        return name
    }
}
