package com.ooyala.android.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.TypedValue;

import com.ooyala.android.configuration.FCCTVRatingConfiguration;

final class FCCTVRatingViewStampDimensions {

  private static final int WHITE_BORDER_DP = 2;
  private static final int BLACK_BORDER_DP = 4;
  private static final int MINIMUM_SIZE_PT = 24; // different than iOS version, to work on small Android displays.

  // sizes are in pixels.
  public int miniHeight;
  public int whiteBorderSize;
  public int blackBorderSize;
  public int borderSize;
  public int outerWidth; // including border.
  public int outerHeight; // including border.
  public int innerWidth; // excluding border.
  public int innerHeight; // excluding border.
  public Rect outerRect;
  public Rect innerRect;
  public Rect tvRect;
  public Rect labelsRect;
  public Rect ratingRect;

  public FCCTVRatingViewStampDimensions() {}
  
  public boolean contains( float x, float y ) {
    return outerRect.contains( (int)x, (int)y );
  }

  public void update( Context context, FCCTVRatingConfiguration tvRatingConfiguration, int measuredWidth, int measuredHeight, boolean hasLabels ) {
    // the order of these 3 calls must be preserved.
    updateBorder( context );
    updateDimensions( context, tvRatingConfiguration.scale, measuredWidth, measuredHeight );
    updateRects( tvRatingConfiguration.position, measuredWidth, measuredHeight, hasLabels );
  }
  
  private void updateBorder( Context context ) {
    whiteBorderSize = (int)TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, WHITE_BORDER_DP, context.getResources().getDisplayMetrics() );
    blackBorderSize = (int)TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, BLACK_BORDER_DP, context.getResources().getDisplayMetrics() );
    borderSize = whiteBorderSize + blackBorderSize;
  }
  
  private void updateDimensions( Context context, float scale, int measuredWidth, int measuredHeight ) {
    setInnerDimensions( context, scale, measuredWidth, measuredHeight );
    setOuterFromInnerDimensions();
  }
  
  private void setInnerDimensions( Context context, float scale, int measuredWidth, int measuredHeight ) {
    setBasicInnerDimentions( scale, measuredWidth, measuredHeight );
    setFinalInnerDimensions( context );
  }
  
  private void setBasicInnerDimentions( float scale, int measuredWidth, int measuredHeight ) {
      // Base the square off the halved video
    float w = measuredWidth;
    float h = measuredHeight;
    if (measuredWidth > measuredHeight) {
      w = measuredWidth / 2;
    } else {
      h = measuredHeight / 2;
    }
    innerWidth = Math.round( scale * w );
    innerHeight = Math.round( scale * h );
  }
  
  private void setFinalInnerDimensions( Context context ) {
    //    // Ensure width and height are of minimum size
    //    +  int minimumSize = [self calculateMinimumSizeInPixels];
    //    +  width = MAX( width, minimumSize );
    //    +  height = MAX( height, minimumSize );
    int minimumSize = (int)TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_PT, MINIMUM_SIZE_PT, context.getResources().getDisplayMetrics() );
    innerWidth = Math.max( innerWidth, minimumSize );
    innerHeight = Math.max( innerHeight, minimumSize );

    //    +  //Square the stamp
    //    +  height = MIN( height, width );
    //    +  width = height;
    int min = Math.min( innerHeight, innerWidth );
    innerWidth = min;
  }
  
  private void setOuterFromInnerDimensions() {
    outerWidth = innerWidth + borderSize*2;
    outerHeight = innerHeight + borderSize*2;
  }
  
  private void updateRects( FCCTVRatingConfiguration.Position position, int measuredWidth, int measuredHeight, boolean hasLabels ) {
    int left, top;
    int right = measuredWidth - outerWidth;
    int bottom = measuredHeight - outerHeight;
    switch( position ) {
    default:
    case TopLeft:
      left = 0;
      top = 0;
      break;
    case BottomLeft:
      left = 0;
      top = bottom;
      break;
    case TopRight:
      left = right;
      top = 0;
      break;
    case BottomRight:
      left = right;
      top = bottom;
      break;
    }
    
    outerRect =
        new Rect(
            0,
            0,
            outerWidth,
            outerHeight
            );
    outerRect.offset( left, top );
    
    innerRect =
        new Rect(
            whiteBorderSize,
            whiteBorderSize,
            outerWidth-whiteBorderSize,
            outerHeight-whiteBorderSize
            );
    innerRect.offset( left, top );
    
    miniHeight = Math.round( innerHeight * FCCTVRatingView.MINI_HEIGHT_FACTOR );

    int tl = borderSize;
    int tt = borderSize;
    int tr = tl + innerWidth;
    int tb = tt + miniHeight;
    tvRect = new Rect( tl, tt, tr, tb );
    tvRect.offset( left, top );
    
    int ll = borderSize;
    int lt = outerHeight - borderSize - miniHeight;
    int lr = ll + innerWidth;
    int lb = lt + miniHeight;
    labelsRect = new Rect( ll, lt, lr, lb );
    labelsRect.offset( left, top );

    int rl = borderSize;
    int rt = borderSize + miniHeight;
    int rr = rl + innerWidth;
    int rb = outerHeight - borderSize - (hasLabels ? miniHeight : 0);
    ratingRect = new Rect( rl, rt, rr, rb );
    ratingRect.offset( left, top );
  }

  public boolean isValid() {
    return innerWidth > 0 && innerHeight > 0;
  }
}