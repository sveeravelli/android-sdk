package com.ooyala.android.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.ooyala.android.TVRatings;
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

    public StampDimensions() {}

    public void update( Context context, TVRatingsConfiguration tvRatingsConfiguration, int measuredWidth, int measuredHeight, int watermarkWidth, int watermarkHeight, boolean hasLabels ) {
      // the order of these 3 calls must be preserved.
      updateBorder( context );
      updateDimensions( tvRatingsConfiguration.scale, measuredWidth, measuredHeight, watermarkWidth );
      updateRects( tvRatingsConfiguration.position, watermarkWidth, watermarkHeight, hasLabels );
    }
    
    private void updateBorder( Context context ) {
      this.whiteBorderSize = (int)TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, WHITE_BORDER_DP, context.getResources().getDisplayMetrics() );
      this.blackBorderSize = (int)TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, BLACK_BORDER_DP, context.getResources().getDisplayMetrics() );
      this.borderSize = this.whiteBorderSize + this.blackBorderSize;
    }
    
    private void updateDimensions( float scale, int measuredWidth, int measuredHeight, int watermarkWidth ) {
      // todo: consider padding from parent layout?
      if( isSquareish( measuredWidth, measuredHeight ) ) {
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
    
    private void updateRects( TVRatingsConfiguration.Position position, int watermarkWidth, int watermarkHeight, boolean hasLabels ) {
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
      
      this.miniHeight = Math.round( this.innerHeight * MINI_HEIGHT_FACTOR );

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

  private static final int FADE_IN_MSEC = 1 * 500;
  private static final int FADE_OUT_MSEC = 1 * 1000;
  private static final float MINI_HEIGHT_FACTOR = 0.2f;
  private Paint watermarkPaint;
  private Paint textPaint;
  private Paint blackPaint;
  private Paint whitePaint;
  private Paint clearPaint;
  private float miniTextSize;
  private float miniTextScaleX;
  private Rect watermarkRect;
  private StampDimensions stampDimensions;
  // n means 'possibly null'; a reminder to check.
  private Bitmap nBitmap;
  private AlphaAnimation nFadeInAnimation;
  private AlphaAnimation nFadeOutAnimation;
  private TVRatingsConfiguration nTVRatingsConfiguration;
  private TVRatings nTVRatings;

  public FCCTVRatingsView( Context context, AttributeSet attrs ) {
    super( context, attrs );
    initPaints( TVRatingsConfiguration.DEFAULT_OPACITY );
    this.miniTextSize = 0;
    this.miniTextScaleX = 0;
    this.watermarkRect = new Rect();
    this.stampDimensions = new StampDimensions();
    this.nTVRatingsConfiguration = TVRatingsConfiguration.s_getDefaultTVRatingsConfiguration();
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    freeResources();
  }

  @Override
  public boolean onTouchEvent( MotionEvent event ) {
    final boolean isVisible = getVisibility() == VISIBLE;
    if( isVisible && hasClickthrough() ) {
      getContext().startActivity(
          new Intent(
              Intent.ACTION_VIEW,
              Uri.parse( nTVRatings.clickthrough )
              )
          );
    }
    return isVisible;
  }

  @Override
  protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
    if( ! hasTVRatingsConfiguration() ) {
      setMeasuredDimension( 0, 0 );
      watermarkRect.set( 0, 0, 0, 0 );
    }
    else {
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
      
      final int watermarkWidth = (int)Math.round(measuredWidth/2f);
      int left;
      switch( nTVRatingsConfiguration.position ) {
      default:
      case TopLeft:
      case BottomLeft:
        left = 0;
        break;
      case TopRight:
      case BottomRight:
        left = watermarkWidth;
        break;
      }
      watermarkRect.set( left, 0, Math.min(measuredWidth, left+watermarkWidth), measuredHeight );
      
      setMeasuredDimension( measuredWidth, measuredHeight );
    }
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged( w, h, oldw, oldh );
    freeResources();
  }

  /**
   * For the configuration to actually take effect, it must be set before Android's
   * layout system calls onMeasure.
   */
  public void setTVRatingsConfiguration( TVRatingsConfiguration tvRatingsConfiguration ) {
    this.nTVRatingsConfiguration = tvRatingsConfiguration;
    if( hasTVRatingsConfiguration() ) {
      initPaints( tvRatingsConfiguration.opacity );
      freeResources();
    }
  }
  
  private void initPaints( float opacity ) {
    final int iOpaticy = (int)Math.round(255*opacity);
    
    watermarkPaint = new Paint();
    watermarkPaint.setColor( Color.argb( (int)Math.round(iOpaticy*0.8f), 255, 255, 255 ) );
//    watermarkPaint.setColor( Color.argb( 64, 255, 0, 0 ) );
    watermarkPaint.setStyle( Paint.Style.FILL );

    blackPaint = new Paint();
    blackPaint.setColor( Color.argb( iOpaticy, 0, 0, 0 ) );
//    blackPaint.setColor( Color.argb( 64, 0, 255, 0 ) );
    blackPaint.setStyle( Paint.Style.FILL );

    whitePaint = new Paint();
    whitePaint.setColor( Color.argb( iOpaticy, 255, 255, 255 ) );
//    whitePaint.setColor( Color.argb( 64, 0, 0, 255 ) );
    whitePaint.setStyle( Paint.Style.FILL );

    textPaint = new Paint( Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG );
    textPaint.setColor( Color.argb( iOpaticy, 255, 255, 255 ) );
    textPaint.setStyle( Paint.Style.FILL );
    Typeface tf = Typeface.create( "DroidSans", Typeface.BOLD );
    textPaint.setTypeface( tf );
    textPaint.setTextAlign( Align.CENTER );

    clearPaint = new Paint();
    clearPaint.setColor( Color.TRANSPARENT );
    clearPaint.setStyle( Paint.Style.FILL );
  }
  
  public void setTVRatings( TVRatings tvRatings ) {
    this.nTVRatings = tvRatings;
	  freeResources();
	  startAnimation();
  }

  private void startAnimation() {
    if( hasValidRating() &&
        ! hasAnimation() &&
        hasTVRatingsConfiguration() &&
        nTVRatingsConfiguration.durationSeconds != TVRatingsConfiguration.TIMER_NEVER ) {
    	startFadeInAnimation();
    }
  }
  
  private void startFadeInAnimation() {
	  nFadeInAnimation = new AlphaAnimation( 0f, 1f );
	  nFadeInAnimation.setDuration( FADE_IN_MSEC );
	  nFadeInAnimation.setFillAfter( true );
	  nFadeInAnimation.setAnimationListener(new AnimationListener(){
		  @Override
		  public void onAnimationStart(Animation arg0) {
			  setVisibility( VISIBLE );
		  }
		  @Override
		  public void onAnimationEnd(Animation arg0) {
			  startFadeOutAnimation();
		  }
		  @Override
		  public void onAnimationRepeat(Animation arg0) {
		  }
	  });
	  startAnimation( nFadeInAnimation );
  }

  private void startFadeOutAnimation() {
    if( hasTVRatingsConfiguration() &&
        nTVRatingsConfiguration.durationSeconds != TVRatingsConfiguration.TIMER_ALWAYS ) {
      nFadeOutAnimation = new AlphaAnimation( 1f, 0f );
      nFadeOutAnimation.setStartOffset( nTVRatingsConfiguration.durationSeconds * 1000 );
      nFadeOutAnimation.setDuration( FADE_OUT_MSEC );
      nFadeOutAnimation.setFillAfter( true );
      nFadeOutAnimation.setAnimationListener(new AnimationListener(){
        @Override
        public void onAnimationStart(Animation arg0) {
        }
        @Override
        public void onAnimationEnd(Animation arg0) {
          setVisibility( INVISIBLE );
        }
        @Override
        public void onAnimationRepeat(Animation arg0) {
        }
      });
      startAnimation( nFadeOutAnimation );
    }
  }

  private void freeResources() {
    nBitmap = null;
  }
  
  private boolean hasTVRatingsConfiguration() {
    return this.nTVRatingsConfiguration != null;
  }
  
  private boolean hasAnimation() {
    return this.nFadeInAnimation != null || this.nFadeOutAnimation != null;
  }

  private boolean hasValidRating() {
    return nTVRatings != null && nTVRatings.rating != null;
  }

  private boolean hasLabels() {
    return nTVRatings != null && nTVRatings.labels != null && nTVRatings.labels.length() > 0;
  }
  
  private boolean hasClickthrough() {
    return nTVRatings != null && nTVRatings.clickthrough != null;
  }

  private boolean hasBitmap() {
    return nBitmap != null;
  }

  @Override
  protected void onDraw( Canvas canvas ) {
    super.onDraw( canvas );
    maybeGenerateBitmap();
    if( hasBitmap() ) {
      canvas.drawBitmap( nBitmap, watermarkRect.left, watermarkRect.top, null );
    }
  }

  private void maybeGenerateBitmap() {
    if( hasValidRating() ) {      
      stampDimensions.update( getContext(), nTVRatingsConfiguration, getMeasuredWidth(), getMeasuredHeight(), watermarkRect.width(), watermarkRect.height(), hasLabels() );
      generateBitmap();
    }
    else {
      freeResources();
    }
  }

  private void generateBitmap() {
    nBitmap = Bitmap.createBitmap( watermarkRect.width(), getMeasuredHeight(), Bitmap.Config.ARGB_8888 ); // todo: Check for fastest ARGB mode vs. SurfaceView.
    Canvas c = new Canvas( nBitmap );
    drawBitmapWatermark( c );
    drawBitmapStamp( c );
  }

  private void drawBitmapWatermark( Canvas c ) {
    c.drawRect( 0, 0, nBitmap.getWidth(), nBitmap.getHeight(), watermarkPaint );
  }

  private void drawBitmapStamp( Canvas c ) {
    drawBitmapStampBackground( c );
    drawBitmapStampTV( c );
    drawBitmapStampLabels( c );
    drawBitmapStampRating( c );
  }

  private void drawBitmapStampBackground( Canvas c ) {
    c.clipRect( stampDimensions.outerRect, Region.Op.REPLACE );
    c.drawRect( stampDimensions.outerRect, whitePaint );
    c.drawRect( stampDimensions.innerRect, blackPaint );
  }

  private void drawBitmapStampTV( Canvas c ) {
    c.clipRect( stampDimensions.tvRect, Region.Op.REPLACE );
    drawTV( c, stampDimensions.tvRect );
  }

  private void drawBitmapStampLabels( Canvas c ) {
    if( hasLabels() ) {
      c.clipRect( stampDimensions.labelsRect, Region.Op.REPLACE );
      drawLabels( c, stampDimensions.labelsRect, nTVRatings.labels );
    }
  }

  private void drawBitmapStampRating( Canvas c ) {
    if( hasValidRating() ) {
      c.clipRect( stampDimensions.ratingRect, Region.Op.REPLACE );
      drawRating( c, stampDimensions.ratingRect, nTVRatings.rating );
    }
  }

  private void drawTV( Canvas c, Rect r ) {
    updateMiniTextPaintFactors( r );
    String text = "TV";
    drawTextInRectGivenTextFactors( c, r, text, miniTextSize, miniTextScaleX );
  }

  private void drawLabels( Canvas c, Rect r, String labels ) {
    drawTextInRectGivenTextFactors( c, r, labels, miniTextSize, miniTextScaleX );
  }

  private void drawRating( Canvas c, Rect r, String rating ) {
    drawTextInRectAutoTextFactors( c, r, rating );
  }

  private void updateTextPaintFactors( Rect r, String text ) {
    Pair<Float,Float> sizeAndScaleX = calculateTextPaintFactors( r, text );
    textPaint.setTextSize( sizeAndScaleX.first );
    textPaint.setTextScaleX( sizeAndScaleX.second );
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
    float textScaleX = Math.min( 1f, tsx ) * 0.7f;
    return new Pair<Float,Float>( textSize, textScaleX );
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

