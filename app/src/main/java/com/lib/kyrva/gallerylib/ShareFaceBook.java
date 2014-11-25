package com.lib.kyrva.gallerylib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.*;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.*;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.UserInfoChangedCallback;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ShareFaceBook extends Activity {

  private Button shareBut;
  private LoginButton loginBut;
  private TextView userName;
  private UiLifecycleHelper uiHelper;
  private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	  this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	  uiHelper = new UiLifecycleHelper(this, statusCallback);
	  uiHelper.onCreate(savedInstanceState);
        setContentView(R.layout.share_facebook);
	  loginBut=(LoginButton) findViewById(R.id.authButton);
	  try {
		PackageInfo info = getPackageManager().getPackageInfo(
			"com.lib.kyrva.gallerylib", PackageManager.GET_SIGNATURES);
		for (Signature signature : info.signatures){
		  MessageDigest md = MessageDigest.getInstance("SHA");
		  md.update(signature.toByteArray());
		  Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
		}
	  } catch (NameNotFoundException e) {
	  } catch (NoSuchAlgorithmException e) {
	  }
	  Log.d("FACEBOOK", String.valueOf(isFacebookAvailable()));
	  loginBut.setUserInfoChangedCallback(new UserInfoChangedCallback(){
		@Override public void onUserInfoFetched(final GraphUser user){
		  if (user != null) {
			Toast.makeText(getApplicationContext(), "Hello, "+user.getName(), Toast.LENGTH_SHORT).show();
		  } else {
			Toast.makeText(getApplicationContext(), "Please press button to login",
				Toast.LENGTH_SHORT).show();
		  }
		}
	  });
    }
  public boolean isFacebookAvailable() {

	Intent intent = new Intent(Intent.ACTION_SEND);
	intent.putExtra(Intent.EXTRA_TEXT, "Test; please ignore");
	intent.setType("text/plain");

	final PackageManager pm = this.getApplicationContext().getPackageManager();
	for(ResolveInfo resolveInfo: pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)){
	  ActivityInfo activity = resolveInfo.activityInfo;
	  if (activity.name.contains("com.facebook.katana")) {
		return true;
	  }
	}
	return false;
  }
  public void postImage() {
	if (checkPermissions()) {
	  SharedPreferences sPrefs=getApplicationContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
	  String path=sPrefs.getString("path", "0");
	  Bitmap img = BitmapFactory.decodeFile(path);
	  Request uploadRequest = Request.newUploadPhotoRequest(Session.getActiveSession(), img, new Request.Callback(){
			@Override
			public void onCompleted(Response response){
			  Toast.makeText(ShareFaceBook.this, "Photo uploaded successfully", Toast.LENGTH_LONG).show();
			  Log.d("FACEBOOK", "image posted");
			}
		  });
	  uploadRequest.executeAsync();
	} else {
	  requestPermissions();
	  Log.d("FACEBOOk", "post image false");
	}
  }

  public boolean checkPermissions() {
	Session s = Session.getActiveSession();
	if (s != null) {
	  return s.getPermissions().contains("publish_actions");
	} else
	  return false;
  }

  public void requestPermissions() {
	Session s = Session.getActiveSession();
	if (s != null)
	  s.requestNewPublishPermissions(new Session.NewPermissionsRequest(
		  this, PERMISSIONS));
  }

  private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	if (session.isOpened()) {
	  Log.d("FacebookSampleActivity", "Facebook session opened");
	} else if (session.isClosed()) {
	  Log.d("FacebookSampleActivity", "Facebook session closed");
	}
  }
  private Session.StatusCallback statusCallback = new Session.StatusCallback() {
	@Override
	public void call(Session session, SessionState state,
					 Exception exception) {
	onSessionStateChange(session, state, exception);
	}
  };
  @Override
  public void onResume() {
	super.onResume();
	Session session = Session.getActiveSession();
	if (session != null &&
		(session.isOpened() || session.isClosed()) ) {
	  onSessionStateChange(session, session.getState(), null);
	}
	uiHelper.onResume();
  }

  @Override
  public void onPause() {
	super.onPause();
	uiHelper.onPause();
  }

  @Override
  public void onDestroy() {
	super.onDestroy();
	uiHelper.onDestroy();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	uiHelper.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public void onSaveInstanceState(Bundle savedState) {
	super.onSaveInstanceState(savedState);
	uiHelper.onSaveInstanceState(savedState);
  }

  public void emailShare(View view){
startActivity(new Intent(ShareFaceBook.this, ShareEmailAct.class));
  }

  public void shareToTwitter(View view){
	SharedPreferences sPrefs=getApplicationContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
	String path=sPrefs.getString("path", "0");
	if (isAppInstalled("com.twitter.android")){
	  List<Intent> targetedShareIntents = new ArrayList<Intent>();
	  Intent share = new Intent(android.content.Intent.ACTION_SEND);
	  share.setType("image/jpeg");
	  List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);
	  if (!resInfo.isEmpty()){
		for (ResolveInfo info : resInfo) {
		  Intent targetedShare = new Intent(android.content.Intent.ACTION_SEND);
		  targetedShare.setType("image/jpeg"); // put here your mime type

		  if (info.activityInfo.packageName.toLowerCase().contains("com.twitter.android") ||
			  info.activityInfo.name.toLowerCase().contains("com.twitter.android")) {
			targetedShare.putExtra(Intent.EXTRA_TEXT,     "MegaPhoto");
			targetedShare.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)) );
			targetedShare.setPackage(info.activityInfo.packageName);
			targetedShareIntents.add(targetedShare);
		  }
		}

		Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Select app to share");
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
		startActivity(chooserIntent);
	  }
	} else {
	  Intent intent = new Intent(Intent.ACTION_VIEW);
	  intent.setData(Uri.parse("market://details?id=com.twitter.android"));
	  startActivity(intent);
	}
  }

  private boolean isAppInstalled(String packageName) {
	PackageManager pm = getPackageManager();
	boolean installed = false;
	try {
	  pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
	  installed = true;
	} catch (PackageManager.NameNotFoundException e) {
	  installed = false;
	}
	return installed;
  }

}
