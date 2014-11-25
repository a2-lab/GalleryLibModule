package com.lib.kyrva.gallerylib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


public class ShareEmailAct extends Activity {

    private SharedPreferences sPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_email);
    }

  public void showContacts(View view){
	startActivity(new Intent(ShareEmailAct.this, ContactAct.class));
  }

  public void enterEmail(View view){
	sPrefs=getApplicationContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
	String path=sPrefs.getString("path", "a");
	Log.d("PATH", path);
	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	emailIntent.setType("application/image");
	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"MegaPhoto");
	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
	emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path));
	startActivity(Intent.createChooser(emailIntent, "Send mail..."));
  }
}
