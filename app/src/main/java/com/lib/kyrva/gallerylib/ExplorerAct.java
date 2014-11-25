package com.lib.kyrva.gallerylib;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.provider.MediaStore.Video;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView.LayoutParams;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdSize;
//import com.google.android.gms.ads.AdView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class ExplorerAct extends Activity{

  private GridView gridView;

  private File[] file;
//  private AdView adView;
  private Bitmap icon;
  private List<Bitmap> mediaFiles;
  private String imageId, videoId;
  private ImageAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState){
	super.onCreate(savedInstanceState);
	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	setContentView(R.layout.explorer);
	icon = BitmapFactory.decodeResource(getResources(), R.drawable.play_icona);
	mediaFiles = new ArrayList<Bitmap>();
	new ImageToList().execute();
	//adView
	LinearLayout layout = (LinearLayout) findViewById(R.id.admob);
//	adView = new AdView(this);
//	adView.setAdSize(AdSize.BANNER);
//	adView.setAdUnitId("ca-app-pub-1974179407250396/6082309513");
//	layout.addView(adView);
//	AdRequest adRequest = new AdRequest.Builder()
//		.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//		.addTestDevice("**").build();
//	adView.loadAd(adRequest);
  }

  //get media files
  private void getFiles(){
	getImageThumbnail();
	getVideoThumbnail();
  }

  //method for putting play icon on the video thumbnail
  private Bitmap putOverlay(Bitmap bmp1, Bitmap overlay){
	Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
	Canvas canvas = new Canvas(bmOverlay);
	canvas.drawBitmap(bmp1, new Matrix(), null);
	canvas.drawBitmap(overlay, 50, 50, null);
	return bmOverlay;
  }

  //adding image thumbnail from MediaStore
  private void getImageThumbnail(){
	ContentResolver contentResolver = getContentResolver();
	String[] projection = new String[]{MediaStore.Images.Thumbnails._ID, MediaStore.Images.Thumbnails.IMAGE_ID};
	Cursor thumbnails = contentResolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, null, null, null);

	// Then walk thru result and obtain imageId from records
	for(thumbnails.moveToFirst(); !thumbnails.isAfterLast(); thumbnails.moveToNext()){
	  imageId = thumbnails.getString(thumbnails.getColumnIndex(Thumbnails.IMAGE_ID));
	  Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), Long.valueOf(imageId),
		  Thumbnails.MICRO_KIND, null);
	  if(bitmap!=null)
	  mediaFiles.add(bitmap);
	  System.gc();
	}

  }

  private void getVideoThumbnail(){
	List<String> videoIds=new ArrayList<String>();
	ContentResolver contentResolver = getContentResolver();

	String[] projection = new String[]{MediaStore.Video.Thumbnails._ID, Video.Thumbnails.VIDEO_ID};
	Cursor thumbnails = contentResolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, projection,
		null, null, null);

	final File temp=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/" +
		".cache_MegaPhoto/");
	if(!temp.exists()){
	  temp.mkdirs();
	}
	// Then walk thru result and obtain imageId from records
	for(thumbnails.moveToFirst(); !thumbnails.isAfterLast(); thumbnails.moveToNext()){
	  videoId = thumbnails.getString(thumbnails.getColumnIndex(Video.Thumbnails.VIDEO_ID));
	  if(videoId!=null)
	  videoIds.add(videoId);
	}
	final Timer timer=new Timer();

	final List<String> masId=videoIds;
	timer.scheduleAtFixedRate(new TimerTask(){
	  int count=0;
	  @Override public void run(){
		Bitmap bitmap= Video.Thumbnails.getThumbnail(getContentResolver(),
			Long.valueOf(masId.get(count)),
			Video.Thumbnails.MICRO_KIND, null);
		count++;
		if(bitmap!=null){
		  try{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			bitmap.compress(CompressFormat.PNG, 100, out);
			File temp1 = new File(temp.getAbsolutePath() + "/" + videoId);
			if(!temp1.exists()){
			  temp1.createNewFile();
			}
			FileOutputStream outStream;
			outStream = new FileOutputStream(temp1);
			outStream.write(out.toByteArray());
			out.close();
			outStream.close();
		  }catch(IOException e){
			e.printStackTrace();
		  }
		  mediaFiles.add(putOverlay(bitmap, icon));
		}
		runOnUiThread(new Runnable(){
		  @Override public void run(){
			adapter.notifyDataSetChanged();
		  }
		});
		if(count==masId.size()){
		  timer.cancel();
		}
		System.gc();
	  }
	}, 0, 500);
  }

  //adapter for showing thumbnails
  private class ImageAdapter extends BaseAdapter{

	private Context _context;

	public ImageAdapter(Context context){
	  _context = context;
	}

	@Override public int getCount(){
	  return mediaFiles.size();
	}

	@Override public Object getItem(final int i){
	  return mediaFiles.get(i);
	}

	@Override public long getItemId(final int i){
	  return 0;
	}

	@Override public View getView(final int i, final View view, final ViewGroup viewGroup){
	  ImageView iView = new ImageView(_context);
	  if(i<mediaFiles.size()){
		GridView.LayoutParams layoutParams = new LayoutParams(150, 150);
		iView.setScaleType(ScaleType.CENTER_CROP);
		iView.setLayoutParams(layoutParams);
		iView.setImageBitmap((Bitmap) getItem(i));
	  }
	  return iView;
	}
  }

  private class ImageToList extends AsyncTask<Void, Void, Void>{
	private ProgressDialog progressBar;

	@Override protected Void doInBackground(final Void... voids){
	  getFiles();
	  return null;
	}

	@Override protected void onPreExecute(){
	  super.onPreExecute();
	  progressBar = new ProgressDialog(ExplorerAct.this);
	  progressBar.setMessage("Loading files");
	  progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	  progressBar.setIndeterminate(true);
	  progressBar.show();
	}

	@Override protected void onPostExecute(final Void aVoid){
	  super.onPostExecute(aVoid);
	  Display getOrientation = getWindowManager().getDefaultDisplay();
	  gridView = (GridView) findViewById(R.id.gridView);
	  adapter = new ImageAdapter(ExplorerAct.this);
	  int div=getOrientation.getWidth() > getOrientation.getHeight()?180:160;
	  int width=getOrientation.getWidth();
	  gridView.setNumColumns(width / div);
	  gridView.setAdapter(adapter);
	  gridView.setOnItemClickListener(new OnItemClickListener(){
		@Override
		public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l){
		  Editor editor = getApplicationContext().getSharedPreferences("pref", Context.MODE_PRIVATE).edit();
		  //saving an ID of choosen element
		  editor.putInt("ID", i);
		  editor.commit();
		  Intent intent=new Intent(ExplorerAct.this, GalleryAct.class);
		  startActivity(intent);
		  finish();
		}
	  });
	  progressBar.dismiss();
	}
  }
}
