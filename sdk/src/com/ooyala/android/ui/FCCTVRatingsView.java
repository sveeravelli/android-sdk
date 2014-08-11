package com.ooyala.android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class FCCTVRatingsView extends View {

  private static final int SHOW_FOR_MSEC = 5 * 1000;
  private static final int FADE_MSEC = 1000;
  private static final float MINI_HEIGHT_FACTOR = 0.2f;
  private static final int WHITE_BORDER_DP = 2;
  private static final int BLACK_BORDER_DP = 4;
  private static final float SQUARE_SCALE = 0.25f;
  private static final float NON_SQUARE_LARGE_SCALE = 0.20f;
  private static final float NON_SQUARE_SMALL_SCALE = 0.30f;
  private static final float MAX_RATIO = 1.4f;
  public boolean debug;
  private Paint textPaint;
  private Paint backgroundPaint;
  private Paint borderPaint;
  private Paint clearPaint;
  private float miniTextSize;
  private float miniTextScaleX;
  private int miniHeight;
  private String rating;
  private String labels;
  private Bitmap bitmap;
  // sizes are in pixels.
  private int bitmapWhiteBorderSize;
  private int bitmapBlackBorderSize;
  private int bitmapBorderSize;
  private int bitmapOuterWidth; // including border.
  private int bitmapOuterHeight; // including border.
  private int bitmapInnerWidth; // excluding border.
  private int bitmapInnerHeight; // excluding border.

  public FCCTVRatingsView( Context context, AttributeSet attrs ) {
    super( context, attrs );
    _initPaints();
    _initSizes();
    _initFade();
  }

  private void _initPaints() {
    backgroundPaint = new Paint();
    backgroundPaint.setColor( android.graphics.Color.BLACK );
    backgroundPaint.setStyle( Paint.Style.FILL );

    borderPaint = new Paint();
    borderPaint.setColor( android.graphics.Color.argb( 255, 128, 128, 128 ) );
    borderPaint.setStyle( Paint.Style.FILL );

    textPaint = new Paint( Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG );
    textPaint.setColor( android.graphics.Color.WHITE );
    textPaint.setStyle( Paint.Style.FILL );
    Typeface tf = Typeface.create( "DroidSans", Typeface.BOLD );
    textPaint.setTypeface( tf );
    textPaint.setTextAlign( Align.CENTER );

    clearPaint = new Paint();
    clearPaint.setColor( android.graphics.Color.TRANSPARENT );
    clearPaint.setStyle( Paint.Style.FILL );
  }

  private void _initSizes() {
    // these will be properly updated later when we know the bitmap size.
    miniTextSize = 0;
    miniTextScaleX = 0;
    miniHeight = 0;

    bitmapWhiteBorderSize = (int)TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, WHITE_BORDER_DP, getResources().getDisplayMetrics() );
    bitmapBlackBorderSize = (int)TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, BLACK_BORDER_DP, getResources().getDisplayMetrics() );
    bitmapBorderSize = bitmapWhiteBorderSize + bitmapBlackBorderSize;
  }

  private void _initFade() {
    final AlphaAnimation fadeOut = new AlphaAnimation( 1, 0 );
    fadeOut.setStartOffset( SHOW_FOR_MSEC );
    fadeOut.setDuration( FADE_MSEC );
    fadeOut.setFillAfter( true );
    fadeOut.setAnimationListener(new AnimationListener(){
      @Override
      public void onAnimationEnd(Animation arg0) {
        setVisibility( GONE );
      }
      @Override
      public void onAnimationRepeat(Animation arg0) {
      }
      @Override
      public void onAnimationStart(Animation arg0) {
      }
    });
    startAnimation( fadeOut );
  }

  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    bitmap = null;
  }

  @Override
  protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {

    final int paddingLeft = getPaddingLeft();
    final int paddingTop = getPaddingTop();
    final int paddingRight = getPaddingRight();
    final int paddingBottom = getPaddingBottom();

    final int viewWidthSize = MeasureSpec.getSize(widthMeasureSpec);
    final int viewHeightSize = MeasureSpec.getSize(heightMeasureSpec);
    final int elementWidth = viewWidthSize - paddingLeft - paddingRight;
    final int elementHeight = viewHeightSize - paddingTop - paddingBottom;

    int measuredWidth = elementWidth + paddingLeft + paddingRight;
    int measuredHeight = elementHeight + paddingTop + paddingBottom;
    measuredWidth = Math.max(measuredWidth, getSuggestedMinimumWidth());
    measuredHeight = Math.max(measuredHeight, getSuggestedMinimumHeight());

    final float fullRatio = measuredWidth / (float)measuredHeight;
    final boolean squareish = fullRatio > 0.9 && fullRatio < 1.1;
    if( squareish ) {
      final int bitmapInnerSize = Math.round( Math.min(measuredWidth, measuredHeight) * SQUARE_SCALE );
      bitmapInnerWidth = bitmapInnerSize;
      bitmapInnerHeight = bitmapInnerSize;
    }
    else {
      // todo: consider padding.
      float scale = measuredWidth >= 430 ? NON_SQUARE_LARGE_SCALE : NON_SQUARE_SMALL_SCALE;
      // the bitmap is taller than wider, hence the flipping of height and width here.
      bitmapInnerWidth = Math.round( scale * measuredHeight );
      bitmapInnerHeight = Math.round( scale * measuredWidth );
    }

    final int watermarkWidth = squareish ? measuredWidth : Math.round(measuredWidth/2f);

    // constraints.
    // * bitmap is not allowed to be bigger than 50% of watermark.
    bitmapInnerWidth = Math.min( bitmapInnerWidth, Math.round(watermarkWidth * 0.5f) );
    bitmapInnerHeight = Math.min( bitmapInnerHeight, Math.round(watermarkWidth * 0.5f) );
    // * bitmap is not allowed to be wider than tall.
    bitmapInnerHeight = Math.max( bitmapInnerHeight, bitmapInnerWidth );
    // * bitmap is not allowed to be lots taller than wide.
    bitmapInnerHeight = Math.min( bitmapInnerHeight, Math.round(bitmapInnerWidth* MAX_RATIO ) );

    bitmapOuterWidth = bitmapInnerWidth + bitmapBorderSize*2;
    bitmapOuterHeight = bitmapInnerHeight + bitmapBorderSize*2;
    miniHeight = Math.round( bitmapInnerHeight * MINI_HEIGHT_FACTOR );

    setMeasuredDimension( watermarkWidth, measuredHeight );
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged( w, h, oldw, oldh );
    bitmap = null;
  }

  public void setRating( String rating ) {
    this.rating = rating;
    this.bitmap = null;
    invalidate();
  }

  public void setLabels( String labels ) {
    this.labels = labels;
    this.bitmap = null;
    invalidate();
  }

  private boolean hasLabels() {
    return labels != null && labels.length() > 0;
  }

  @Override
  protected void onDraw( Canvas canvas ) {
    super.onDraw( canvas );
    if( bitmap == null ) {
      maybeGenerateBitmap();
    }
    if( bitmap != null ) {
      canvas.drawBitmap( bitmap, 0, 0, null );
    }
  }

  private void maybeGenerateBitmap() {
    if( bitmapInnerWidth > 0 && bitmapInnerWidth > 0 ) {
      generateBitmap();
    }
  }

  private void generateBitmap() {
    bitmap = Bitmap.createBitmap( bitmapOuterWidth, bitmapOuterHeight, Bitmap.Config.ARGB_8888 );
    Canvas c = new Canvas( bitmap );
    if( rating != null ) {
      generateRatingBitmap( c );
    }
    else {
      generateEmptyBitmap( c );
    }
  }

  private void generateRatingBitmap( Canvas c ) {
    drawBackground( c );
    drawTV( c );
    drawLabels( c );
    drawRating( c );
  }

  private void drawTV( Canvas c ) {
    int tl = bitmapBorderSize;
    int tt = bitmapBorderSize;
    int tr = tl + bitmapInnerWidth;
    int tb = tt + miniHeight;
    Rect tvRect = new Rect( tl, tt, tr, tb );
    drawTV( c, tvRect );
  }

  private void drawLabels( Canvas c ) {
    if( hasLabels() ) {
      int ll = bitmapBorderSize;
      int lt = bitmapOuterHeight - bitmapBorderSize - miniHeight;
      int lr = ll + bitmapInnerWidth;
      int lb = lt + miniHeight;
      Rect labelsRect = new Rect( ll, lt, lr, lb );
      drawLabels( c, labelsRect, labels );
    }
  }

  private void drawRating( Canvas c ) {
    int rl = bitmapBorderSize;
    int rt = bitmapBorderSize + miniHeight;
    int rr = rl + bitmapInnerWidth;
    int rb = bitmapOuterHeight - bitmapBorderSize - (hasLabels() ? miniHeight : 0);
    Rect ratingRect = new Rect( rl, rt, rr, rb );
    drawRating( c, ratingRect, rating );
  }

  private void generateEmptyBitmap( Canvas c ) {
    c.drawRect( 0, 0, bitmapOuterWidth, bitmapOuterHeight, clearPaint );
  }

  private void drawBackground( Canvas c ) {
    Rect outerRect = new Rect( 0, 0, bitmapOuterWidth, bitmapOuterHeight );
    if( debug ) {
      drawDebugBackground( c, outerRect, android.graphics.Color.argb( 128, 255, 0, 0 ) );
    }
    else {
      c.drawRect( outerRect, borderPaint );
      Rect innerRect = new Rect( bitmapWhiteBorderSize, bitmapWhiteBorderSize, bitmapOuterWidth-bitmapWhiteBorderSize, bitmapOuterHeight-bitmapWhiteBorderSize );
      c.drawRect( innerRect, backgroundPaint );
    }
  }

  private void drawTV( Canvas c, Rect r ) {
    updateMiniTextPaintFactors( r );
    drawDebugBackground( c, r, android.graphics.Color.argb( 128, 0, 255, 255 ) );
    String text = "TV";
    drawTextInRectGivenTextFactors( c, r, text, miniTextSize, miniTextScaleX );
  }

  private void drawLabels( Canvas c, Rect r, String labels ) {
    drawDebugBackground( c, r, android.graphics.Color.argb( 128, 255, 255, 0 ) );
    drawTextInRectGivenTextFactors( c, r, labels, miniTextSize, miniTextScaleX );
  }

  private void drawRating( Canvas c, Rect r, String rating ) {
    drawDebugBackground( c, r, android.graphics.Color.argb( 128, 0, 0, 255 ) );
    drawTextInRectAutoTextFactors( c, r, rating );
  }

  private void drawDebugBackground( Canvas c, Rect r, int color ) {
    if( debug ) {
      int rx = (int)Math.rint( Math.random()*3 );
      Paint p = new Paint();
      p.setColor( color );
      p.setStyle( Paint.Style.FILL );
      c.drawRect( r.left+rx, r.top, r.right-rx, r.bottom, p );
      p.setColor( android.graphics.Color.argb( 128, (int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255) ) );
      c.drawLine(r.left, r.top, r.right, r.bottom, p );
      c.drawLine(r.left, r.bottom, r.right, r.top, p );
    }
  }

  private void updateMiniTextPaintFactors( Rect exampleMiniRect ) {
    // calculate text factors; same ones are used for both top and bottom strips of the rating stamp.
    Pair<Float,Float> sizeAndScaleX = calculateTextPaintFactors( exampleMiniRect, "VSLDFV" );
    miniTextSize = sizeAndScaleX.first;
    miniTextScaleX = sizeAndScaleX.second;
  }

  private Pair<Float,Float> calculateTextPaintFactors( Rect r, String text ) {
    textPaint.setTextSize( 1000 );
    Rect tb = new Rect();
    textPaint.getTextBounds( text, 0, text.length(), tb );
    float ts = r.height()/(float)tb.height()*1000;
    // fudge factors to really fit into rect.
    float textSize = ts * 0.7f;
    float tsx = r.width()/(float)tb.width()*1000;
    float textScaleX = Math.min( 1f, tsx ) * 0.8f;
    return new Pair<Float,Float>( textSize, textScaleX );
  }

  private void updateTextPaintFactors( Rect r, String text ) {
    Pair<Float,Float> sizeAndScaleX = calculateTextPaintFactors( r, text );
    textPaint.setTextSize( sizeAndScaleX.first );
    textPaint.setTextScaleX( sizeAndScaleX.second );
  }

  private void drawTextInRectGivenTextFactors( Canvas c, Rect r, String text, float textSize, float textScaleX ) {
    textPaint.setTextSize( textSize );
    textPaint.setTextScaleX( textScaleX );
    drawTextInRect( c, r, text );
  }

  private void drawTextInRectAutoTextFactors( Canvas c, Rect r, String text ) {
    updateTextPaintFactors( r, text );
    drawTextInRect( c, r, text );
  }

  private void drawTextInRect( Canvas c, Rect r, String text ) {
    Rect tb = new Rect();
    textPaint.getTextBounds( text, 0, text.length(), tb );
    int tl = r.left + Math.round(r.width()/2f);
    int tt = r.top + Math.round((r.height() + tb.height())/2f);
    c.drawText( text, tl, tt, textPaint );
  }
}

