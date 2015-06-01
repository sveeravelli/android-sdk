package com.ooyala.android.castsdk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.ooyala.android.util.DebugMode;
import com.ooyala.android.OoyalaPlayer.State;

import java.lang.ref.WeakReference;

public class DefaultCastMiniController extends RelativeLayout implements CastMiniController {

  private static final String TAG = "OODefaultMiniController";


  private final int DP;
  
  protected ImageView icon;
  protected TextView title;
  protected TextView subTitle;
  protected ImageView playPause;
  private RelativeLayout container;
  private Bitmap pauseImageBitmap;
  private Bitmap playImageBitmap;
  
  public DefaultCastMiniController(Context context, AttributeSet attrs) {
    super(context, attrs);
    
    this.DP = (int)context.getResources().getDisplayMetrics().density;
    
    pauseImageBitmap = CastUtils.getDarkChromecastPauseButton();
    playImageBitmap = CastUtils.getDarkChromecastPlayButton();

    constructContainer(context);
    setupCallbacks();
  }
  
  private void constructContainer(Context context) {
    container = new RelativeLayout(context);
    LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
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
    LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
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
          DebugMode.assertCondition((CastManager.getCastManager().getCastPlayer() != null), TAG, "castPlayer should never be null when we have a mini controller");
          CastPlayer castPlayer = CastManager.getCastManager().getCastPlayer();
          State state = castPlayer.getState();
          DebugMode.logD(TAG, "Play/Pause button is clicked in default mini controller with state = " + state);
          if (state == State.PLAYING){
            castPlayer.pause();
          } else {
            castPlayer.play();
          }
        }
    });

    container.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        DebugMode.logD(TAG, "Mini Controller is clicked.");
        if (CastManager.getCastManager().getTargetActivity() != null) {
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
    Intent intent = new Intent(context, CastManager.getCastManager().getTargetActivity());
    intent.putExtra("embedcode", CastManager.getCastManager().getCastPlayer().getEmbedCode());
    context.startActivity(intent);
  }
  
  public void setIcon(Bitmap bitmap) {
    icon.setImageBitmap(bitmap);
  }
  
  @Override
  public void updatePlayPauseButtonImage(boolean isPlaying) {
    DebugMode.logD(TAG, "Update play/pause button in default mini controller for isPlaying = " + isPlaying);
    if (isPlaying) {
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
    updateUIInfo();
  }

  public void dismiss() {
    super.setVisibility(GONE);
  }

  private void updateUIInfo() {
    DebugMode.logD(TAG, "Update MiniController UI Info");
    title.setText(CastManager.getCastManager().getCastPlayer().getCastItemTitle());
    subTitle.setText(CastManager.getCastManager().getCastPlayer().getCastItemDescription());
    setIcon(CastManager.getCastManager().getCastPlayer().getCastImageBitmap());
  }
}
























































