package com.intellibitz.mobile.dating;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ViewFlipper;


public class Dating extends Activity {

    private ViewFlipper mFlipper;
    private ViewGroup mContainer;
    private Intent intent;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setTheme(android.R.style.Theme_ContextMenu);
        setContentView(R.layout.welcome);
        mFlipper = ((ViewFlipper) this.findViewById(R.id.flipper));
        mFlipper.startFlipping();
        ImageView icon1=(ImageView) findViewById(R.id.icon1);
        ImageView icon2=(ImageView) findViewById(R.id.icon2);
        ImageView icon3=(ImageView) findViewById(R.id.icon3);
        ImageView icon4=(ImageView) findViewById(R.id.icon4);
        ImageView icon5=(ImageView) findViewById(R.id.icon5);
        ImageView icon6=(ImageView) findViewById(R.id.icon6);
        mContainer = (ViewGroup) findViewById(R.id.container);
        ((ViewGroup) mContainer.getParent()).setKeepAnimations(true);

        icon1.setOnClickListener(new View.OnClickListener() {
        
            public void onClick(View arg0) {
                intent=new Intent(Dating.this,Seeker.class);
                startActivity(intent);
                 
            }
        });
        icon2.setOnClickListener(new View.OnClickListener() {
        
            public void onClick(View arg0) {
                Intent intent1=new Intent(Dating.this,DatingServiceController.class);
                startActivity(intent1);
            }
        });
        icon3.setOnClickListener(new View.OnClickListener() {
        
            public void onClick(View arg0) {
                Intent intent2=new Intent(Dating.this,Map.class);
                startActivity(intent2);
            }
        });
        icon4.setOnClickListener(new View.OnClickListener() {
        
            public void onClick(View arg0) {
                Intent intent3=new Intent(Dating.this,MatchingData.class);
                startActivity(intent3);
            }
        });
        icon5.setOnClickListener(new View.OnClickListener() {
        
            public void onClick(View arg0) {
               Intent intent4=new Intent(Dating.this,Help.class);
                startActivity(intent4);
                
            }
        });
       
        icon6.setOnClickListener(new View.OnClickListener() {
        
            public void onClick(View arg0) {
            finish();
            }
        });
        
    }
  
}
