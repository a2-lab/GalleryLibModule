package com.lib.kyrva.gallerylib;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.provider.MediaStore.Video;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery.LayoutParams;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdSize;
//import com.google.android.gms.ads.AdView;


public class GalleryAct extends Activity{

  private int pos;
//  private AdView adView;
  private boolean masBool[];
  private List<Boolean> isVideo;
  private ImageAdapter adapter;
  private RelativeLayout relativeLayout;
  private MyGallery _gal;
  private List<Mapa> list;
  private List<String> pathToImage;
  private List<String> ids;
  private boolean show, butPlay;
  private int width,height;
  private SharedPreferences sPrefs;
  private int count;
  private ImageLoader imageLoader;
  private DisplayImageOptions imageOptions;

  @Override
  protected void onCreate(Bundle savedInstanceState){
	super.onCreate(savedInstanceState);
	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	setContentView(R.layout.gallery);
	show = true;
	count=0;
	butPlay = true;
	list = new ArrayList<Mapa>();
	ids = new ArrayList<String>();
	pathToImage = new ArrayList<String>();
	isVideo = new ArrayList<Boolean>();
	sPrefs = getApplicationContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
	pos = sPrefs.getInt("ID", 0);
	new start_work().execute();
	Display getOrientation = getWindowManager().getDefaultDisplay();
	width=getOrientation.getWidth();
	height=getOrientation.getHeight();
	//adView
		LinearLayout layout = (LinearLayout) findViewById(R.id.admob);
//		adView = new AdView(this);
//		adView.setAdSize(AdSize.BANNER);
//		adView.setAdUnitId("ca-app-pub-1974179407250396/6082309513");
//		layout.addView(adView);
//		AdRequest adRequest = new AdRequest.Builder()
//			.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//			.addTestDevice("**").build();
//		adView.loadAd(adRequest);
	////////////////////////
	ImageLoaderConfiguration config=new ImageLoaderConfiguration.Builder(this)
		.memoryCache(new LruMemoryCache(10 * 1024 * 1024))
		.memoryCacheSize(50 * 1024 * 1024)
		.memoryCacheSizePercentage(20)
		.diskCache(new UnlimitedDiscCache(this.getCacheDir()))
		.diskCacheSize(300 * 1024 * 1024)
		.diskCacheFileCount(375)
		.diskCacheFileNameGenerator(new Md5FileNameGenerator())
		.defaultDisplayImageOptions(DisplayImageOptions.createSimple())
		.build();

	imageOptions=new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.loading)
		.resetViewBeforeLoading(false)
		.delayBeforeLoading(0)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.bitmapConfig(Config.RGB_565)
		.displayer(new SimpleBitmapDisplayer())
		.handler(new Handler())
		.build();
	imageLoader=ImageLoader.getInstance();
	imageLoader.init(config);

  }

  public void deleteImage(View view){
	final Editor editor = getApplicationContext().getSharedPreferences("pref", Context.MODE_PRIVATE).edit();
	final File sound = new File(pathToImage.get(pos));
	final Builder dialogBuilder = new AlertDialog.Builder(this);
	dialogBuilder.setTitle("Deleting");
	dialogBuilder.setMessage("You realy want to do this?").setCancelable(false).setPositiveButton("Yes", new OnClickListener(){
	  @Override public void onClick(final DialogInterface dialogInterface, final int i){
		sound.delete();
		long id = Long.valueOf(ids.get(pos));
		ContentResolver contentResolver = getContentResolver();
		contentResolver.delete(Thumbnails.EXTERNAL_CONTENT_URI, Thumbnails.IMAGE_ID + " = ?", new String[]{"" + id});
		if(pos <= pathToImage.size()){
		  editor.putInt("ID", pos -= 1);
		  editor.commit();
		}

		startActivity(new Intent(GalleryAct.this, GalleryAct.class));
		finish();
	  }
	}).setNegativeButton("No", new OnClickListener(){
	  @Override public void onClick(final DialogInterface dialogInterface, final int i){
		dialogInterface.cancel();
	  }
	});
	AlertDialog dialog = dialogBuilder.create();
	dialog.show();
  }

  //go to ShareAct
  public void shareImage(View view){
	ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(android.os.Environment
		.getExternalStorageDirectory().getAbsoluteFile() + "test.jpg")
		, 96, 96);
		sPrefs=getApplicationContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
		int sharePos=sPrefs.getInt("ID", 0);
	Intent intent = new Intent(GalleryAct.this, ShareFaceBook.class);
	Editor editor = getApplicationContext().getSharedPreferences("pref", Context.MODE_PRIVATE).edit();
	editor.putString("path", pathToImage.get(sharePos));
	editor.putBoolean("isVideo", isVideo.get(sharePos));
	editor.commit();
	startActivity(intent);
  }

  private class Mapa{
	public Bitmap bitmap;
	public boolean isVideo;
	public String path;
  }

  private void getImage(){
	ContentResolver contentResolver = getContentResolver();
	String filePath = null;
	String[] projection = new String[]{MediaStore.Images.Thumbnails._ID, MediaStore.Images.Thumbnails.IMAGE_ID};
	Cursor thumbnails = contentResolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, null, null, null);

	int i=0;
	// Then walk thru result and obtain imageId from records
	for(thumbnails.moveToFirst(); !thumbnails.isAfterLast(); thumbnails.moveToNext()){
	  String imageId = thumbnails.getString(thumbnails.getColumnIndex(Thumbnails.IMAGE_ID));
	  //		// Request image related to this thumbnail
	  String[] filePathColumn = {MediaStore.Images.Media.DATA};

	  Cursor images = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, filePathColumn, MediaStore.Images.Media._ID + "=?", new String[]{imageId}, null);
i++;
	  if(images != null && images.moveToFirst()){
		//				  // Your file-path will be here
		//		assert images!=null;
		//		images.moveToFirst();
		filePath = images.getString(images.getColumnIndex(filePathColumn[0]));
				Mapa m=new Mapa();
		if(new File(filePath).exists()/* && isImage(tokenizer.nextToken())*/){
		  pathToImage.add(filePath);
		  isVideo.add(false);
		  		  m.bitmap =null /*BitmapFactory.decodeFile(filePath)*/;
		  		  m.isVideo = false;
		  		  m.path = filePath;
		  count++;
		  		  list.add(m);
		  ids.add(imageId);
		}
	  }
	  images.close();
	}
	thumbnails.close();
  }

  //get Video from thumbnails
  private void getVideo(){
	ContentResolver contentResolver = getContentResolver();
	String filePath = null;
	String[] projection = new String[]{MediaStore.Video.Thumbnails._ID, Video.Thumbnails.VIDEO_ID};
	Cursor thumbnails = contentResolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, projection, null, null, null);

	Options options = new Options();
	options.inDither = false;
	options.inPurgeable = true;
	options.inInputShareable = true;
	options.inPreferredConfig = Config.RGB_565;
	options.inPreferQualityOverSpeed = true;
	// Then walk thru result and obtain imageId from records
	for(thumbnails.moveToFirst(); !thumbnails.isAfterLast(); thumbnails.moveToNext()){
	  String videoId = thumbnails.getString(thumbnails.getColumnIndex(Video.Thumbnails.VIDEO_ID));
	  String[] filePathColumn = {Video.Media.DATA};
	  Cursor images = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, filePathColumn, MediaStore.Video.Media._ID + "=?", new String[]{videoId}, null);

	  if(images != null && images.moveToFirst()){
		//				  // Your file-path will be here
				  Mapa mapa=new Mapa();
		filePath = images.getString(images.getColumnIndex(filePathColumn[0]));
		  if(new File(filePath).exists()){
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()
				.getAbsolutePath()+"/.cache_MegaPhoto/"+videoId, options);
			options.inJustDecodeBounds=false;
			Bitmap bmp=BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()
				.getAbsolutePath()+"/.cache_MegaPhoto/"+videoId, options);
			mapa.bitmap = bmp;
			mapa.isVideo = true;
			mapa.path = filePath;
			list.add(mapa);
			pathToImage.add(filePath);
			isVideo.add(true);
			ids.add(videoId);
		  }
		  }
	  images.close();
	}
	thumbnails.close();
  }

  private class start_work extends AsyncTask<Void, Void, Void>{
	private ProgressDialog bar;

	@Override protected Void doInBackground(final Void... voids){
	  getImage();
	  getVideo();
	  return null;
	}

	@Override protected void onPreExecute(){
	  bar = new ProgressDialog(GalleryAct.this);
	  bar.setMessage("Loading file");
	  bar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	  bar.setIndeterminate(true);
	  bar.show();
	  super.onPreExecute();
	}

	@Override protected void onPostExecute(final Void aVoid){
	  masBool = new boolean[pathToImage.size()];
	  for(int i = 0; i <count; i++){
		masBool[i] = true;
	  }
	  for(int i = count; i <masBool.length; i++){
		masBool[i] = false;
	  }
	  bar.dismiss();
	  sPrefs = getApplicationContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
	  final Editor editor = getApplicationContext().getSharedPreferences("pref", Context.MODE_PRIVATE).edit();
	  _gal = (MyGallery) findViewById(R.id.gallery);
	  relativeLayout = (RelativeLayout) findViewById(R.id.relLayMenuBar);
	  adapter = new ImageAdapter(GalleryAct.this);
	  relativeLayout.setVisibility(View.INVISIBLE);
	  _gal.setAdapter(adapter);
	  _gal.setSpacing(40);
	  	  _gal.setSelection(sPrefs.getInt("ID", 0));
	  _gal.setOnItemClickListener(new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		  if(show) relativeLayout.setVisibility(View.VISIBLE);
		  else relativeLayout.setVisibility(View.INVISIBLE);
		  show = !show;
		  editor.putInt("ID", position);
		  editor.commit();
		}
	  });
	  super.onPostExecute(aVoid);
	}
  }

  @Override public void onBackPressed(){
	super.onBackPressed();
	startActivity(new Intent(GalleryAct.this, ExplorerAct.class));
  }

  private class ImageAdapter extends BaseAdapter{
	private Context context;
	private LayoutParams params;

	public ImageAdapter(Context context){
	  this.context = context;
	  params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
	}

	@Override
	public int getCount(){
	  return masBool.length;
	}

	@Override
	public Object getItem(int position){
	  return position;
	}

	@Override
	public long getItemId(int position){
	  return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent){
	  RelativeLayout rLayout = new RelativeLayout(context);
	  rLayout.setLayoutParams(params);
	  RelativeLayout.LayoutParams LayOutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	  RelativeLayout.LayoutParams LayOutParams1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
	  ImageView iView = new ImageView(rLayout.getContext());
	  iView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	  relativeLayout.setVisibility(View.INVISIBLE);
	  show = true;
	  LayOutParams1.addRule(RelativeLayout.CENTER_IN_PARENT);
	  rLayout.addView(iView, LayOutParams1);
	  try{
		if(!isVideo.get(position)){
		  imageLoader.displayImage("file://" + pathToImage.get(position), iView, imageOptions);
		}else{
		  iView.setImageBitmap(Bitmap.createScaledBitmap(list.get(position).bitmap,
			  width,
			  height,
			  true));
		}
		if(isVideo.get(position)){

		  Button playBut = new Button(rLayout.getContext());
		  playBut.setGravity(RelativeLayout.CENTER_IN_PARENT);
		  playBut.setBackgroundResource(R.drawable.play_icon);
		  playBut.setOnClickListener(new View.OnClickListener(){
			@Override public void onClick(final View view){
			  Editor editor = getApplicationContext().getSharedPreferences("pref", Context.MODE_PRIVATE).edit();
			  String pathToVid = pathToImage.get(position);
			  editor.putString("video", pathToVid);
			  editor.commit();
			  startActivity(new Intent(GalleryAct.this, VideoPlayAct.class));
			}
		  });
		  LayOutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		  rLayout.addView(playBut, LayOutParams);
		}
	  } catch(IndexOutOfBoundsException e){
		Toast.makeText(getApplicationContext(), "No video on device", Toast.LENGTH_SHORT).show();
	  }
	  return rLayout;
	}
  }
}
