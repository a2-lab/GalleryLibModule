package com.lib.kyrva.gallerylib;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.facebook.*;
import com.facebook.widget.FacebookDialog;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class ShareAct extends Activity {

  private UiLifecycleHelper uiHelper;
  private Session.StatusCallback statusCallback =
	  new SessionStatusCallback();
  private Button shareButton;
  private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
  private boolean pendingPublishReauthorization = false;
  private String pathToData;
  private EditText eText;
  private Button shareEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share);
	  shareButton = (Button) findViewById(R.id.shareButton);
	  shareEmail=(Button) findViewById(R.id.shareEmail);
	  eText=(EditText) findViewById(R.id.emailET);
	  uiHelper = new UiLifecycleHelper(this, callback);
	  uiHelper.onCreate(savedInstanceState);
	  Intent intent=getIntent();
	  pathToData=intent.getStringExtra("path");
    }

  private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	if (session.isOpened()) {
	  shareButton.setVisibility(View.VISIBLE);
	} else if (session.isClosed()) {
	  shareButton.setVisibility(View.INVISIBLE);
	}
  }
  private void publishStory() {
	final Session session = Session.getActiveSession();

	if (session != null){

	  // Check for publish permissions
	  	  List<String> permissions = session.getPermissions();
	  	  if (!isSubsetOf(PERMISSIONS, permissions)) {
	  		pendingPublishReauthorization = true;
	  		Session.NewPermissionsRequest newPermissionsRequest = new Session
	  			.NewPermissionsRequest(this, PERMISSIONS);
	  		session.requestNewPublishPermissions(newPermissionsRequest);
	  		return;
	  	  }

	  Bitmap bitmap = BitmapFactory.decodeFile(pathToData);
	  final Bundle postParams = new Bundle();
	  if(bitmap != null){
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		postParams.putByteArray("photo", bytes.toByteArray());
		postParams.putString("message", "Say HALO! to Halepa");
		final Request.Callback callback1 = new Request.Callback(){
		  public void onCompleted(Response response){
		  };

		};
		Request request = new Request(session, "me/photos", postParams, HttpMethod.POST, callback1);
		RequestAsyncTask task = new RequestAsyncTask(request);
		task.execute();
		Toast.makeText(getApplicationContext(), "Image shared", Toast.LENGTH_SHORT).show();
	  }
	}
  }
  private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
	for (String string : subset) {
	  if (!superset.contains(string)) {
		return false;
	  }
	}
	return true;
  }
  private Session.StatusCallback callback = new Session.StatusCallback() {
	@Override
	public void call(Session session, SessionState state, Exception exception) {
	  onSessionStateChange(session, state, exception);
	}
  };
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback(){
	  @Override
	  public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data){
		Log.e("Activity", String.format("Error: %s", error.toString()));
	  }

	  @Override
	  public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data){
		Log.i("Activity", "Success!");
	  }
	});
  }
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
  public void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	uiHelper.onSaveInstanceState(outState);
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

  public void logInTo(View view){
	Session session = Session.getActiveSession();
	if (!session.isOpened() && !session.isClosed()) {
	  session.openForRead(new Session.OpenRequest(this)
		  .setPermissions(Arrays.asList("public_profile"))
		  .setCallback(statusCallback));
	} else {
	  Session.openActiveSession(ShareAct.this, true, statusCallback);
	}
  }

  public void shareData(View view){
	publishStory();
  }

  public void shareToEmail(View view){
	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	emailIntent.setType("application/image");
	emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{eText.getText().toString()});
	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"MegaPhoto");
	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
	emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(pathToData));
	startActivity(Intent.createChooser(emailIntent, "Send mail..."));
  }

  private class SessionStatusCallback implements Session.StatusCallback {
	@Override
	public void call(Session session, SessionState state, Exception exception) {
	  // Respond to session state changes, ex: updating the view
	}
  }
}
