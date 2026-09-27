package com.intellibitz.mobile.dating;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ViewFlipper;
import com.google.android.maps.MapView;
import com.google.android.maps.Point;

/**
 *
 * @author
 */
public class MatchingData extends Activity {

    private MapView mMapView;
    private ViewFlipper mFlipper;
    private Point p1,  p2,  p3,  p4,  p5;
    private Bitmap bubbleIcon;
    private Bitmap bubbleIcon1;

    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);
        setContentView(R.layout.matchingprofile);
        mFlipper = ((ViewFlipper) this.findViewById(R.id.flipper));
        mFlipper.startFlipping();
        bubbleIcon = BitmapFactory.decodeResource(getResources(), R.drawable.bubble);
        bubbleIcon1 = BitmapFactory.decodeResource(getResources(), R.drawable.bubble1);
        Button button = (Button) findViewById(R.id.chating);
        Button showmap = (Button) findViewById(R.id.viewmap);
        button.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v) {

                Intent intobject = new Intent(MatchingData.this, MatchingProfile.class);
                startActivity(intobject);
            }
            });

        showmap.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v) {

                Intent intobject1 = new Intent(MatchingData.this, Map.class);
                startActivity(intobject1);
            }
            });
            
    }
}
