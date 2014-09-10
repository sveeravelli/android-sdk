package com.ooyala.android.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.TypedValue;

import com.ooyala.android.configuration.FCCTVRatingConfiguration;

final class FCCTVRatingViewStampDimensions {

  private static final int WHITE_BORDER_DP = 2;
  private static final int BLACK_BORDER_DP = 4;
  private static final float SQUARE_SCALE = 0.25f;
  private static final float MAX_RATIO = 1.4f;

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

  public void update( Context context, FCCTVRatingConfiguration TVRatingConfiguration, int measuredWidth, int measuredHeight, int watermarkWidth, int watermarkHeight, boolean hasLabels ) {
    // the order of these 3 calls must be preserved.
    updateBorder( context );
    updateDimensions( TVRatingConfiguration.scale, measuredWidth, measuredHeight, watermarkWidth );
    updateRects( TVRatingConfiguration.position, watermarkWidth, watermarkHeight, hasLabels );
  }
  
  private void updateBorder( Context context ) {
    this.whiteBorderSize = (int)TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, WHITE_BORDER_DP, context.getResources().getDisplayMetrics() );
    this.blackBorderSize = (int)TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, BLACK_BORDER_DP, context.getResources().getDisplayMetrics() );
    this.borderSize = this.whiteBorderSize + this.blackBorderSize;
  }
  
  private void updateDimensions( float scale, int measuredWidth, int measuredHeight, int watermarkWidth ) {
    // todo: consider padding from parent layout?
    if( FCCTVRatingView.isSquareish( measuredWidth, measuredHeight ) ) {
      final int bitmapInnerSize = Math.round( Math.min(measuredWidth, measuredHeight) * SQUARE_SCALE );
      this.innerWidth = bitmapInnerSize;
      this.innerHeight = bitmapInnerSize;
    }
    else {
      // the bitmap is never wider than taller; it has the opposite aspect ratio than the video, hence flipping height/width.
      this.innerWidth = Math.round( scale * measuredHeight );
      this.innerHeight = Math.round( scale * measuredWidth );
    }
    this.innerWidth = constrainInnerWidth( this.innerWidth, watermarkWidth, measuredHeight );
    this.innerHeight = constrainInnerHeight( this.innerWidth, this.innerHeight, watermarkWidth, measuredHeight );
    this.outerWidth = this.innerWidth + this.borderSize*2;
    this.outerHeight = this.innerHeight + this.borderSize*2;
  }
  
  private void updateRects( FCCTVRatingConfiguration.Position position, int watermarkWidth, int watermarkHeight, boolean hasLabels ) {
    int left, top;
    int right = watermarkWidth - this.outerWidth;
    int bottom = watermarkHeight - this.outerHeight;
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
    
    this.outerRect =
        new Rect(
            0,
            0,
            this.outerWidth,
            this.outerHeight
            );
    this.outerRect.offset( left, top );
    
    this.innerRect =
        new Rect(
            this.whiteBorderSize,
            this.whiteBorderSize,
            this.outerWidth-this.whiteBorderSize,
            this.outerHeight-this.whiteBorderSize
            );
    this.innerRect.offset( left, top );
    
    this.miniHeight = Math.round( this.innerHeight * FCCTVRatingView.MINI_HEIGHT_FACTOR );

    int tl = this.borderSize;
    int tt = this.borderSize;
    int tr = tl + this.innerWidth;
    int tb = tt + this.miniHeight;
    this.tvRect = new Rect( tl, tt, tr, tb );
    this.tvRect.offset( left, top );
    
    int ll = this.borderSize;
    int lt = this.outerHeight - this.borderSize - miniHeight;
    int lr = ll + this.innerWidth;
    int lb = lt + this.miniHeight;
    this.labelsRect = new Rect( ll, lt, lr, lb );
    this.labelsRect.offset( left, top );

    int rl = this.borderSize;
    int rt = this.borderSize + miniHeight;
    int rr = rl + this.innerWidth;
    int rb = this.outerHeight - this.borderSize - (hasLabels ? miniHeight : 0);
    this.ratingRect = new Rect( rl, rt, rr, rb );
    this.ratingRect.offset( left, top );
  }

  private static int constrainInnerWidth( int innerWidth, int watermarkWidth, int watermarkHeight ) {
    // * bitmap is not allowed to be bigger than 50% of watermark.
    int dimension = Math.round( watermarkWidth * 0.5f );
    innerWidth = Math.min( innerWidth, dimension );
    return innerWidth;
  }

  private static int constrainInnerHeight( int innerWidth, int innerHeight, int watermarkWidth, int watermarkHeight ) {
    // * bitmap is not allowed to be bigger than 50% of watermark.
    int dimension = Math.round( watermarkHeight * 0.5f );
    innerHeight = Math.min( innerHeight, dimension );
    // * bitmap height must be >= width.
    innerHeight = Math.max( innerHeight, innerWidth );
    // * bitmap height must be <= width * ratio.
    innerHeight = Math.min( innerHeight, Math.round( innerWidth * MAX_RATIO ) );
    return innerHeight;
  }

  public boolean isValid() {
    return innerWidth > 0 && innerHeight > 0;
  }
}