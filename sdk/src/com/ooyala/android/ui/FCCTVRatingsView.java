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

import com.ooyala.android.configuration.TVRatingsConfiguration;

/* todo:
 * + update from content item.
 * + handle change between fullscreen & inline.
 * + handle all of TVRatingsConfiguration.
 */

public class FCCTVRatingsView extends View {

  private static boolean isSquareish( int w, int h ) {
    final float fullRatio = w / (float)h;
    final boolean squareish = fullRatio > 0.9 && fullRatio < 1.1;
    return squareish;
  }

  private static final class StampDimensions {

    // sizes are in pixels.
    private int watermarkWidth;
    public int whiteBorderSize;
    public int blackBorderSize;
    public int borderSize;
    public int outerWidth; // including border.
    public int outerHeight; // including border.
    public int innerWidth; // excluding border.
    public int innerHeight; // excluding border.

    public StampDimensions() {}

    public void update( Context context, int measuredWidth, int measuredHeight ) {
      this.watermarkWidth = Math.round( measuredWidth / 2f );

      this.whiteBorderSize = (int)TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, WHITE_BORDER_DP, context.getResources().getDisplayMetrics() );
      this.blackBorderSize = (int)TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, BLACK_BORDER_DP, context.getResources().getDisplayMetrics() );
      this.borderSize = whiteBorderSize + blackBorderSize;

      // todo: consider padding from parent layout?
      if( isSquareish( measuredWidth, measuredHeight ) ) {
        final int bitmapInnerSize = Math.round( Math.min(measuredWidth, measuredHeight) * SQUARE_SCALE );
        this.innerWidth = bitmapInnerSize;
        this.innerHeight = bitmapInnerSize;
      }
      else {
        float scale = measuredWidth >= 430 ? NON_SQUARE_LARGE_SCALE : NON_SQUARE_SMALL_SCALE;
        // the bitmap is never wider than taller; it has the opposite aspect ratio than the video, hence flipping height/width.
        this.innerWidth = Math.round( scale * measuredHeight );
        this.innerHeight = Math.round( scale * measuredWidth );
      }
      constrainInnerWidth();
      constrainInnerHeight();
      this.outerWidth = this.innerWidth + this.borderSize*2;
      this.outerHeight = this.innerHeight + this.borderSize*2;
    }

    private void constrainInnerWidth() {
      // * bitmap is not allowed to be bigger than 50% of watermark.
      innerWidth = Math.min( innerWidth, Math.round(watermarkWidth * 0.5f) );
    }

    private void constrainInnerHeight() {
      // * bitmap is not allowed to be bigger than 50% of watermark.
      innerHeight = Math.min( innerHeight, Math.round(watermarkWidth * 0.5f) );
      // * bitmap is not allowed to be wider than tall.
      innerHeight = Math.max( innerHeight, innerWidth );
      // * bitmap is not allowed to be lots taller than wide.
      innerHeight = Math.min( innerHeight, Math.round( innerHeight * MAX_RATIO ) );
    }

    public boolean isValid() {
      return innerWidth > 0 && innerHeight > 0;
    }
  }

//  private static final int FADE_MSEC = 1 * 1000;
  private static final float MINI_HEIGHT_FACTOR = 0.2f;
  private static final int WHITE_BORDER_DP = 2;
  private static final int BLACK_BORDER_DP = 4;
  private static final float SQUARE_SCALE = 0.25f;
  private static final float NON_SQUARE_LARGE_SCALE = 0.20f;
  private static final float NON_SQUARE_SMALL_SCALE = 0.30f;
  private static final float MAX_RATIO = 1.4f;
  private final Paint watermarkPaint;
  private final Paint textPaint;
  private final Paint blackPaint;
  private final Paint whitePaint;
  private final Paint clearPaint;
  private float miniTextSize;
  private float miniTextScaleX;
  private int miniHeight;
  private String rating;
  private String labels;
  private Bitmap nBitmap; // n means 'possibly null'; a reminder to check.
  private StampDimensions stampDimensions;

  private TVRatingsConfiguration tvRatingsConfiguration;

  public FCCTVRatingsView( Context context, AttributeSet attrs ) {
    super( context, attrs );

    watermarkPaint = new Paint();
    watermarkPaint.setColor( android.graphics.Color.argb( (int)Math.round(255*0.8f), 255, 255, 255 ) );
    watermarkPaint.setStyle( Paint.Style.FILL );

    blackPaint = new Paint();
    blackPaint.setColor( android.graphics.Color.BLACK );
    blackPaint.setStyle( Paint.Style.FILL );

    whitePaint = new Paint();
    whitePaint.setColor( android.graphics.Color.argb( 255, 128, 128, 128 ) );
    whitePaint.setStyle( Paint.Style.FILL );

    textPaint = new Paint( Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG );
    textPaint.setColor( android.graphics.Color.WHITE );
    textPaint.setStyle( Paint.Style.FILL );
    Typeface tf = Typeface.create( "DroidSans", Typeface.BOLD );
    textPaint.setTypeface( tf );
    textPaint.setTextAlign( Align.CENTER );

    clearPaint = new Paint();
    clearPaint.setColor( android.graphics.Color.TRANSPARENT );
    clearPaint.setStyle( Paint.Style.FILL );

    // these will be properly updated later when we know enough.
    stampDimensions = new StampDimensions();
    miniTextSize = 0;
    miniTextScaleX = 0;
    miniHeight = 0;
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    freeResources();
  }

  public void setTVRatingsConfiguration( TVRatingsConfiguration tvRatingsConfiguration ) {
    this.tvRatingsConfiguration = tvRatingsConfiguration;
  }

//  private void setFade() {
//    if( tvRatingsConfiguration != null &&
//        tvRatingsConfiguration.timerSeconds != TVRatingsConfiguration.TIMER_ALWAYS ) {
//      final AlphaAnimation fadeOut = new AlphaAnimation( 1, 0 );
//      fadeOut.setStartOffset( tvRatingsConfiguration.timerSeconds * 1000 );
//      fadeOut.setDuration( FADE_MSEC );
//      fadeOut.setFillAfter( true );
//      fadeOut.setAnimationListener(new AnimationListener(){
//        @Override
//        public void onAnimationEnd(Animation arg0) {
//          setVisibility( GONE );
//        }
//        @Override
//        public void onAnimationRepeat(Animation arg0) {
//        }
//        @Override
//        public void onAnimationStart(Animation arg0) {
//        }
//      });
//      startAnimation( fadeOut );
//    }
//  }

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
    stampDimensions.update( getContext(), measuredWidth, measuredHeight );
    miniHeight = Math.round( stampDimensions.innerHeight * MINI_HEIGHT_FACTOR ); // todo: move into StampDimensions.
    setMeasuredDimension( measuredWidth, measuredHeight );
  }

  private void freeResources() {
    nBitmap = null;
  }

  private void updateVisibility() {
    // todo: respect fade animation.
    setVisibility( hasRating() ? VISIBLE : GONE );
  }

  private boolean hasRating() {
    return rating != null;
  }

  private boolean hasLabels() {
    return labels != null && labels.length() > 0;
  }

  private boolean canGenerateBitmap() {
    return stampDimensions.isValid() && hasRating();
  }

  private boolean hasBitmap() {
    return nBitmap != null;
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged( w, h, oldw, oldh );
    freeResources();
  }

  public void setRating( String rating ) {
    this.rating = rating;
    freeResources();
    updateVisibility();
    invalidate();
  }

  public void setLabels( String labels ) {
    this.labels = labels;
    freeResources();
    updateVisibility();
    invalidate();
  }

  @Override
  protected void onDraw( Canvas canvas ) {
    super.onDraw( canvas );
    maybeGenerateBitmap();
    if( hasBitmap() ) {
      canvas.drawBitmap( nBitmap, 0, 0, null );
    }
  }

  private void maybeGenerateBitmap() {
    if( canGenerateBitmap() ) {
      generateBitmap();
    }
    else {
      freeResources(); // todo: Check this isn't going to prevent forward progress.
    }
  }

  private void generateBitmap() {
    nBitmap = Bitmap.createBitmap( Math.round(getMeasuredWidth()/2f), getMeasuredHeight(), Bitmap.Config.ARGB_8888 ); // todo: Check for fastest ARGB mode vs. SurfaceView.
    Canvas c = new Canvas( nBitmap );
    drawWatermark( c );
    drawStamp( c );
  }

  private void drawWatermark( Canvas c ) {
    c.drawRect( 0, 0, nBitmap.getWidth(), nBitmap.getHeight(), watermarkPaint );
  }

  private void drawStamp( Canvas c ) {
    drawStampBackground( c );
    drawStampTV( c );
    drawStampLabels( c );
    drawStampRating( c );
  }

  private void drawStampBackground( Canvas c ) {
    // todo: use Position for offset; this currently only does TopLeft.
    Rect outerRect = new Rect(
        0,
        0,
        stampDimensions.outerWidth,
        stampDimensions.outerHeight
        );
    c.drawRect( outerRect, whitePaint );
    Rect innerRect = new Rect(
        stampDimensions.whiteBorderSize,
        stampDimensions.whiteBorderSize,
        stampDimensions.outerWidth-stampDimensions.whiteBorderSize,
        stampDimensions.outerHeight-stampDimensions.whiteBorderSize
        );
    c.drawRect( innerRect, blackPaint );
  }

  private void drawStampTV( Canvas c ) {
    int tl = stampDimensions.borderSize;
    int tt = stampDimensions.borderSize;
    int tr = tl + stampDimensions.innerWidth;
    int tb = tt + miniHeight;
    Rect tvRect = new Rect( tl, tt, tr, tb );
    drawTV( c, tvRect );
  }

  private void drawStampLabels( Canvas c ) {
    if( hasLabels() ) {
      int ll = stampDimensions.borderSize;
      int lt = stampDimensions.outerHeight - stampDimensions.borderSize - miniHeight;
      int lr = ll + stampDimensions.innerWidth;
      int lb = lt + miniHeight;
      Rect labelsRect = new Rect( ll, lt, lr, lb );
      drawLabels( c, labelsRect, labels );
    }
  }

  private void drawStampRating( Canvas c ) {
    int rl = stampDimensions.borderSize;
    int rt = stampDimensions.borderSize + miniHeight;
    int rr = rl + stampDimensions.innerWidth;
    int rb = stampDimensions.outerHeight - stampDimensions.borderSize - (hasLabels() ? miniHeight : 0);
    Rect ratingRect = new Rect( rl, rt, rr, rb );
    drawRating( c, ratingRect, rating );
  }

  private void drawTV( Canvas c, Rect r ) {
    updateMiniTextPaintFactors( r );
//    drawDebugBackground( c, r, android.graphics.Color.argb( 128, 0, 255, 255 ) );
    String text = "TV";
    drawTextInRectGivenTextFactors( c, r, text, miniTextSize, miniTextScaleX );
  }

  private void drawLabels( Canvas c, Rect r, String labels ) {
//    drawDebugBackground( c, r, android.graphics.Color.argb( 128, 255, 255, 0 ) );
    drawTextInRectGivenTextFactors( c, r, labels, miniTextSize, miniTextScaleX );
  }

  private void drawRating( Canvas c, Rect r, String rating ) {
//    drawDebugBackground( c, r, android.graphics.Color.argb( 128, 0, 0, 255 ) );
    drawTextInRectAutoTextFactors( c, r, rating );
  }

//  private void drawDebugBackground( Canvas c, Rect r, int color ) {
//    if( debug ) {
//      int rx = (int)Math.rint( Math.random()*3 );
//      Paint p = new Paint();
//      p.setColor( color );
//      p.setStyle( Paint.Style.FILL );
//      c.drawRect( r.left+rx, r.top, r.right-rx, r.bottom, p );
//      p.setColor( android.graphics.Color.argb( 128, (int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255) ) );
//      c.drawLine(r.left, r.top, r.right, r.bottom, p );
//      c.drawLine(r.left, r.bottom, r.right, r.top, p );
//    }
//  }

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

