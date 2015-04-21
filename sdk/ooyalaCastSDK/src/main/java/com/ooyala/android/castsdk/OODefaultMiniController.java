package com.ooyala.android.castsdk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.ooyala.android.util.DebugMode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;

public class OODefaultMiniController extends RelativeLayout implements com.ooyala.android.castsdk.OOMiniController {

  private static final String TAG = "OODefaultMiniController";

  private static OOCastManager castManager;

  private final int DP;
  
  protected ImageView icon;
  protected TextView title;
  protected TextView subTitle;
  protected ImageView playPause;
  protected ProgressBar loadingBar;
  private RelativeLayout container;
  private Bitmap pauseImageBitmap;
  private Bitmap playImageBitmap;
  
  public OODefaultMiniController(Context context, AttributeSet attrs) {
    super(context, attrs);
    
    this.DP = (int)context.getResources().getDisplayMetrics().density;
    
    pauseImageBitmap = OOCastUtils.getDarkChromecastPauseButton();
    playImageBitmap = OOCastUtils.getDarkChromecastPlayButton();

    constructContainer(context);
    setupCallbacks();
  }
  
  public void setCastManager(OOCastManager castManager) {
    OODefaultMiniController.castManager = castManager;
  }
  
  private void constructContainer(Context context) {
    container = new RelativeLayout(context);
    LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    container.setLayoutParams(layoutParams);
    constructIconImageView(context);
    constructPlayPauseImageView(context);
    constructTitleTextView(context);
    constructSubtitleTextView(context);
    this.addView(container);
  }
  
  private void constructIconImageView(Context context) {
    icon = new ImageView(context);
    icon.setId(1);
    LayoutParams layoutParams = new LayoutParams(89 * DP, 50 * DP);
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
    layoutParams.setMargins(7 * DP, 0, 0, 0);
    icon.setLayoutParams(layoutParams);
    container.addView(icon);
  }
  
  private void constructPlayPauseImageView(Context context) {
    playPause = new ImageView(context);
    playPause.setId(2);
    LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
    layoutParams.setMargins(0, 0, 4 * DP, 0);
    playPause.setImageBitmap(pauseImageBitmap);
    playPause.setLayoutParams(layoutParams);
    container.addView(playPause);
  }
  
  private void constructTitleTextView(Context context) {
    title = new TextView(context);
    title.setId(3);
    LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.ALIGN_TOP, icon.getId());
    layoutParams.addRule(RelativeLayout.RIGHT_OF, icon.getId());
    layoutParams.addRule(RelativeLayout.LEFT_OF, playPause.getId());
    layoutParams.setMargins(10 * DP, 6 * DP, 5 * DP, 0);
    title.setTextColor(Color.BLACK);
    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    title.setLayoutParams(layoutParams);
    title.setMaxLines(1);
    container.addView(title);
  }
  
  private void constructSubtitleTextView(Context context) {
    subTitle = new TextView(context);
    subTitle.setId(4);
    LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, icon.getId());
    layoutParams.addRule(RelativeLayout.ALIGN_LEFT, title.getId());
    layoutParams.addRule(RelativeLayout.RIGHT_OF, icon.getId());
    layoutParams.addRule(RelativeLayout.LEFT_OF, playPause.getId());
    layoutParams.setMargins(10 * DP, 6 * DP, 5 * DP, 0);
    subTitle.setTextColor(Color.BLACK);
    subTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
    subTitle.setLayoutParams(layoutParams);
    subTitle.setMaxLines(1);
    subTitle.setText("Subtitle");
    container.addView(subTitle);
  }
  
  private void setupCallbacks() {

    playPause.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          if (castManager.getCurrentCastPlayer()  == null) {
            return;
        } else if (castManager.getCurrentCastPlayer().getState() == State.PAUSED || 
                   castManager.getCurrentCastPlayer().getState() == State.READY || 
                   castManager.getCurrentCastPlayer().getState() == State.COMPLETED){
          castManager.getCurrentCastPlayer().play();
        } else if (castManager.getCurrentCastPlayer() .getState() == State.PLAYING){
          castManager.getCurrentCastPlayer().pause();
        }
      }
    });

    container.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {

        if (castManager.getTargetActivity() != null) {
          try {
            onTargetActivityInvoked(getContext());
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    });
  }

  private void onTargetActivityInvoked(Context context) throws TransientNetworkDisconnectionException,
      NoConnectionException {
    Intent intent = new Intent(context, castManager.getTargetActivity());
    intent.putExtra("embedcode", castManager.getCurrentCastPlayer().getEmbedCode());
    context.startActivity(intent);
  }
  
  public void setIcon(Bitmap bitmap) {
    icon.setImageBitmap(bitmap);
  }
  
  @Override
  public void updatePlayPauseState(State state) {
    if (state == OoyalaPlayer.State.PLAYING) {
      playPause.setVisibility(View.VISIBLE);
      playPause.setImageBitmap(pauseImageBitmap);
    } else {
      playPause.setVisibility(View.VISIBLE);
      playPause.setImageBitmap(playImageBitmap)
      ;
    }  
  }

  public void show() {
    super.setVisibility(VISIBLE);
    hideControls(false);
    updateUIInfo();
  }


  public void dismiss() {
    super.setVisibility(GONE);
  }
  
  private void hideControls(boolean hide) {
    int visibility = hide ? View.GONE : View.VISIBLE;
    icon.setVisibility(visibility);
    title.setVisibility(visibility);
    subTitle.setVisibility(visibility);
    if (hide) {
      playPause.setVisibility(visibility);
    }
  }
  
  private void updateUIInfo() {
    DebugMode.logD(TAG, "Update MiniController UI Info");
    title.setText(castManager.getCurrentCastPlayer().getCastItemTitle());
    subTitle.setText(castManager.getCurrentCastPlayer().getCastItemDescription());
    setIcon(castManager.getCurrentCastPlayer().getCastImageBitmap());
  }

  @Override
  public void play() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void pause() {
    // TODO Auto-generated method stub
    
  }
}
























































