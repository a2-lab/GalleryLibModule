package com.lib.kyrva.gallerylib;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;


public class VideoPlayAct extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	  requestWindowFeature(Window.FEATURE_NO_TITLE);
	  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.video);
	  showVideo();
    }
private void showVideo(){
  SharedPreferences sPref=getApplicationContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
  VideoView vd = (VideoView)findViewById(R.id.videoView);
  String videoPath=sPref.getString("video", "no_path");
  Log.d("TAG", videoPath);
  Uri uri = Uri.parse(videoPath);
  MediaController mc = new MediaController(this);
  vd.setMediaController(mc);
  vd.setVideoURI(uri);
  vd.start();
  vd.setOnCompletionListener(new OnCompletionListener(){
	@Override public void onCompletion(final MediaPlayer mediaPlayer){
	  finish();
	}
  });
}
}
