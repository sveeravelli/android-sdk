package com.ooyala.android;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

class MovieView extends SurfaceView {
  private float _aspectRatio = -1;
  public MovieView(Context context) {
    super(context);
  }

  public MovieView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public MovieView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void setAspectRatio(float aspectRatio) {
    _aspectRatio = aspectRatio;
    requestLayout();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (_aspectRatio <= 0) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    } else {
      int pWidth = MeasureSpec.getSize(widthMeasureSpec);
      int pHeight = MeasureSpec.getSize(heightMeasureSpec);
      int newWidth = 0;
      int newHeight = 0;
      if (pWidth == 0 || pHeight == 0) {
        Log.e(this.getClass().getName(), "ERROR: cannot set MovieView size");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        return;
      }
      float availableAspectRatio = ((float)pWidth)/((float)pHeight);
      if (availableAspectRatio > _aspectRatio) {
        // bounded by the available height
        newWidth = (int)(_aspectRatio*((float)pHeight));
        newHeight = pHeight;
        Log.d(this.getClass().getName(), "TEST - resizing bounded by height: "+newWidth+","+newHeight);
      } else if (availableAspectRatio < _aspectRatio) {
        // bounded by the available width
        newWidth = pWidth;
        newHeight = (int)(((float)pWidth)/_aspectRatio);
        Log.d(this.getClass().getName(), "TEST - resizing bounded by width: "+newWidth+","+newHeight);
      } else {
        // no bound, aspect ratios are the same.
        newWidth = pWidth;
        newHeight = pHeight;
        Log.d(this.getClass().getName(), "TEST - resizing bounded by nothing: "+newWidth+","+newHeight);
      }
      setMeasuredDimension(newWidth, newHeight);
    }
  }
}
