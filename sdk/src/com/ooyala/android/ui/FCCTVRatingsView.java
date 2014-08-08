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
import android.view.View;

public class FCCTVRatingsView extends View {
  private final Paint backgroundPaint;
  private final Paint textPaint;
  private final Paint clearPaint;
  private float miniTextSize;
  private float miniTextScaleX;
  private Bitmap bitmap;
  private String rating;
  private String labels;
  public boolean debug;

  public FCCTVRatingsView( Context context, AttributeSet attrs ) {
    super( context, attrs );

    backgroundPaint = new Paint();
    backgroundPaint.setColor( android.graphics.Color.BLACK );
    backgroundPaint.setStyle( Paint.Style.FILL );

    textPaint = new Paint( Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG );
    textPaint.setColor( android.graphics.Color.WHITE );
    textPaint.setStyle( Paint.Style.FILL );
    Typeface tf = Typeface.create( "DroidSans", Typeface.BOLD );
    textPaint.setTypeface( tf );
    textPaint.setTextAlign( Align.CENTER );

    clearPaint = new Paint();
    clearPaint.setColor( android.graphics.Color.TRANSPARENT );
    clearPaint.setStyle( Paint.Style.FILL );

    // these will be properly updated later, if we know the bitmap size.
    miniTextSize = 0;
    miniTextScaleX = 0;
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

  @Override
  protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
    super.onMeasure( widthMeasureSpec, heightMeasureSpec );
    // todo: make it relative to video player size, per feature specs.
    setMeasuredDimension( 100, 100 );
  }

  @Override
  protected void onDraw( Canvas canvas ) {
    super.onDraw( canvas );
    if( bitmap == null ) {
      generateBitmap();
    }
    canvas.drawBitmap( bitmap, 0, 0, null );
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged( w, h, oldw, oldh );
    bitmap = null;
  }

  private void updateMiniTextPaintFactors( Rect exampleMiniRect ) {
    // calculate text factors; same ones are used for both top and bottom strips of the rating stamp.
    Pair<Float,Float> sizeAndScaleX = calculateTextPaintFactors( exampleMiniRect, "VSLDFV" );
    miniTextSize = sizeAndScaleX.first;
    miniTextScaleX = sizeAndScaleX.second;
  }

  private void generateBitmap() {
    int w = getMeasuredWidth();
    int h = getMeasuredHeight();
    bitmap = Bitmap.createBitmap( w, h, Bitmap.Config.ARGB_8888 );
    Canvas c = new Canvas( bitmap );
    if( rating != null ) {
      generateRatingBitmap( c, w, h );
    }
    else {
      generateEmptyBitmap( c, w, h );
    }
  }

  private void generateRatingBitmap( Canvas c, int w, int h ) {
    drawBackground( c, w, h );

    int miniHeight = Math.round(h*0.2f);
    Rect tvRect = new Rect( 0, 0, w, miniHeight );
    updateMiniTextPaintFactors( tvRect ); // this must happen here, after new tvRect & before draw{TV,Labels}.
    drawTV( c, tvRect );

    boolean hasLabels = true;
    int lh = hasLabels ? miniHeight : 0;
    Rect labelsRect = new Rect( 0, h-lh, w, h );
    drawLabels( c, labelsRect, labels );

    Rect ratingRect = new Rect( 0, miniHeight, w, h-lh );
    drawRating( c, ratingRect, rating );
  }

  private void generateEmptyBitmap( Canvas c, int w, int h ) {
    c.drawRect( 0, 0, w, h, clearPaint );
  }

  private void drawBackground( Canvas c, int w, int h ) {
    Rect r = new Rect( 0, 0, w, h );
    if( debug ) {
      drawDebugBackground( c, r, android.graphics.Color.argb( 128, 255, 0, 0 ) );
    }
    else {
      c.drawRect( r, backgroundPaint );
    }
  }

  private void drawTV( Canvas c, Rect r ) {
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

  private Pair<Float,Float> calculateTextPaintFactors( Rect r, String text ) {
    textPaint.setTextSize( 1000 );
    Rect tb = new Rect();
    textPaint.getTextBounds( text, 0, text.length(), tb );
    float ts = r.height()/(float)tb.height()*1000;
    // fudge factors to really fit into rect.
    float textSize = ts * 0.8f;
    float tsx = r.width()/(float)tb.width()*1000;
    float textScaleX = Math.min( 1f, tsx ) * 0.9f;
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