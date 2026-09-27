/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobeegal.android.view;

import android.content.Context;
import android.util.AttributeSet;
import com.google.android.maps.MapView;
import com.mobeegal.android.model.MapLocation;
import com.mobeegal.android.view.maps.MapLocationOverlay;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jailani
 */
public class MapLocationViewer
        extends MapView
{

    private MapLocationOverlay overlay;

    //  Known latitude/longitude coordinates that we'll be using.
    private List<MapLocation> mapLocations;

    public MapLocationViewer(Context context)
    {

// todo: get apikey for maps       
        super(context, "apisamples");

        init();
    }

    public MapLocationViewer(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    private void init()
    {
        overlay = new MapLocationOverlay(this);
/*
        OverlayController oController = createOverlayController();
        oController.add(overlay, true);
*/

        getController().setZoom(13);
//        getController().centerMapTo(getMapLocations().get(0).getPoint(), false);
    }

    public List<MapLocation> getMapLocations()
    {
        if (mapLocations == null)
        {
            mapLocations = new ArrayList<MapLocation>();

            mapLocations.add(new MapLocation("Intellibitz Technologies",
                    12.961539, 80.186860));
            mapLocations
                    .add(new MapLocation("Madipakkam", 12.983242, 80.197945));
            mapLocations.add(new MapLocation("St.ThomasMount", 12.994577,
                    80.199297));
            mapLocations.add(new MapLocation("Guindy", 13.009590, 80.211818));
            mapLocations.add(new MapLocation("Saidapet", 13.019908, 80.224771));
            mapLocations.add(new MapLocation("TNagar-Renganathan Street ",
                    13.036939, 80.230285));
            mapLocations.add(new MapLocation("TNagar, Natesan Street ",
                    13.035791, 80.231888));
            mapLocations.add(new MapLocation("TNagar-Panagalpark ", 13.042928,
                    80.232570));
            mapLocations.add(new MapLocation("Adyar", 13.003145, 80.253532));

        }
        return mapLocations;
    }
}
