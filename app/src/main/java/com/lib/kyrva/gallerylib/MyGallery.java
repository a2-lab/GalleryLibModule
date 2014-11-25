package com.lib.kyrva.gallerylib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

/**
 * Created on 28.08.14.
 */
public class MyGallery extends Gallery{
  public MyGallery(final Context context){
	super(context);
  }

  private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2)
  {
	return e2.getX()>e1.getX();
  }

  public MyGallery(Context context, AttributeSet attrs) {
	super(context, attrs);
  }

  @Override
  public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY){
	int kEvent;
	if(isScrollingLeft(e1, e2)){
	  kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
	  onKeyDown(kEvent, null);
	}else{
	  kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
	  onKeyDown(kEvent, null);
	}
	return true;
  }
}
