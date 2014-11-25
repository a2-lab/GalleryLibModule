package com.lib.kyrva.gallerylib;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class StartAct extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
	  //adView
//	  LinearLayout layout = (LinearLayout) findViewById(R.id.admob);
//	  adView = new AdView(this);
//	  adView.setAdSize(AdSize.BANNER);
//	  adView.setAdUnitId("ca-app-pub-1059881374474495/4726062560");
//	  layout.addView(adView);
//	  AdRequest adRequest = new AdRequest.Builder()
//		  .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//		  .addTestDevice("**").build();
//	  adView.loadAd(adRequest);
    }
//launching scrollable list with thumbnails
  public void launchExplorer(View view){
	Intent intent=new Intent(StartAct.this, ExplorerAct.class);
	startActivity(intent);
  }
}
