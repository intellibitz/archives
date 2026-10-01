/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobeegal.android.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.maps.MapView
import com.mobeegal.android.model.MapLocation
import com.mobeegal.android.view.maps.MapLocationOverlay
import java.util.ArrayList

/**
 * @author jailani
 */
class MapLocationViewer : MapView {

    private var overlay: MapLocationOverlay? = null

    //  Known latitude/longitude coordinates that we'll be using.
    private var mapLocations: MutableList<MapLocation>? = null

    constructor(context: Context?) : super(context, "apisamples") {
        // todo: get apikey for maps
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        overlay = MapLocationOverlay(this)
        /*
                OverlayController oController = createOverlayController();
                oController.add(overlay, true);
        */

        controller.setZoom(13)
        //        getController().centerMapTo(getMapLocations().get(0).getPoint(), false);
    }

    fun getMapLocations(): List<MapLocation> {
        if (mapLocations == null) {
            mapLocations = ArrayList()

            mapLocations!!.add(
                MapLocation(
                    "Intellibitz Technologies",
                    12.961539, 80.186860
                )
            )
            mapLocations!!
                .add(MapLocation("Madipakkam", 12.983242, 80.197945))
            mapLocations!!.add(
                MapLocation(
                    "St.ThomasMount", 12.994577,
                    80.199297
                )
            )
            mapLocations!!.add(MapLocation("Guindy", 13.009590, 80.211818))
            mapLocations!!.add(MapLocation("Saidapet", 13.019908, 80.224771))
            mapLocations!!.add(
                MapLocation(
                    "TNagar-Renganathan Street ",
                    13.036939, 80.230285
                )
            )
            mapLocations!!.add(
                MapLocation(
                    "TNagar, Natesan Street ",
                    13.035791, 80.231888
                )
            )
            mapLocations!!.add(
                MapLocation(
                    "TNagar-Panagalpark ", 13.042928,
                    80.232570
                )
            )
            mapLocations!!.add(MapLocation("Adyar", 13.003145, 80.253532))
        }
        return mapLocations!!
    }
}
