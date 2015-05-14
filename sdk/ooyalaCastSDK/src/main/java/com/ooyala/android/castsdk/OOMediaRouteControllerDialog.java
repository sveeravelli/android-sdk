package com.ooyala.android.castsdk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.MediaRouteControllerDialog;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.google.sample.castcompanionlibrary.utils.LogUtils;
import com.ooyala.android.util.DebugMode;
import com.ooyala.android.OoyalaPlayer.State;

import java.lang.ref.WeakReference;

public class OOMediaRouteControllerDialog extends MediaRouteControllerDialog implements OOMiniController {
  
  private static final String TAG = LogUtils.makeLogTag(MediaRouteControllerDialog.class);
  
  private WeakReference<OOCastManager> castManager;
  private State state;

  private final int DP;
  private Context context;

  private ImageView icon;
  private ImageView pausePlay;
  private TextView title;
  private TextView subTitle;
  private TextView emptyText;
  private RelativeLayout mainContainer;
  private RelativeLayout iconContainer;
  private LinearLayout textContainer;
  
  public OOMediaRouteControllerDialog(Context context, OOCastManager castManager) {
    super(context);
    this.castManager = new WeakReference<OOCastManager>(castManager);

    this.DP = (int)context.getResources().getDisplayMetrics().density;
    this.context = context;
  }

  @Override
  public View onCreateMediaControlView(Bundle savedInstanceState) {
      DebugMode.logE(TAG, "onCreateMediaControlView");
      constructMainContainer();
      if (castManager.get().getCastPlayer() != null) {
        state = castManager.get().getCastPlayer().getState();
        castManager.get().addMiniController(this);
      }
      updatePlayPauseButtonImage(state == State.PLAYING);
      setupCallbacks();
      updateMetadata();
      return mainContainer;
  }

  @Override
  public void setCastManager(OOCastManager castManager) {
    DebugMode.logD(TAG, "set CastManager to " + castManager);
    this.castManager = new WeakReference<OOCastManager>(castManager);
  }
  
  private void constructMainContainer() {
    mainContainer = new RelativeLayout(context);
    mainContainer.setId(0);
    LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    mainContainer.setLayoutParams(layoutParams);
    constructIconContainer();
    constructPlayPauseImageView();
    constructTextContainer();
    constructEmptyTextView();
  }
  
  private void constructIconContainer() {
    iconContainer = new RelativeLayout(context);
    iconContainer.setId(1);
    LayoutParams layoutParams = new LayoutParams(68 * DP, 68 * DP);
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    iconContainer.setLayoutParams(layoutParams);
    iconContainer.setBackgroundColor(Color.BLACK);
    mainContainer.addView(iconContainer);
    constructIconImageView();
  }
  
  private void constructIconImageView() {
    icon = new ImageView(context);
    icon.setId(5);
    LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
    icon.setLayoutParams(layoutParams);
    iconContainer.addView(icon);
  }
  
  private void constructTextContainer() {
    textContainer = new LinearLayout(context);
    textContainer.setId(2);
    LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
    layoutParams.addRule(RelativeLayout.RIGHT_OF, iconContainer.getId());
    layoutParams.addRule(RelativeLayout.LEFT_OF, pausePlay.getId());
    layoutParams.setMargins(5 * DP, 0, 10 * DP, 0);
    textContainer.setLayoutParams(layoutParams);
    textContainer.setOrientation(LinearLayout.VERTICAL);
    mainContainer.addView(textContainer);
    constructTitleTextView();
    constructSubtitleTextView();
  }
  
  private void constructTitleTextView() {
    title = new TextView(context);
    title.setId(3);
    LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.ALIGN_TOP, icon.getId());
    layoutParams.addRule(RelativeLayout.LEFT_OF, pausePlay.getId());
    layoutParams.addRule(RelativeLayout.RIGHT_OF, icon.getId());
    title.setTextColor(Color.WHITE);
    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    title.setLayoutParams(layoutParams);
    title.setMaxLines(1);
    textContainer.addView(title);
  }
  
  private void constructSubtitleTextView() {
    subTitle = new TextView(context);
    subTitle.setId(4);
    LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, icon.getId());
    layoutParams.addRule(RelativeLayout.LEFT_OF, pausePlay.getId());
    layoutParams.addRule(RelativeLayout.RIGHT_OF, icon.getId());
    layoutParams.setMargins(10 * DP, 0, 5 * DP, 7 * DP);
    subTitle.setTextColor(Color.WHITE);
    subTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
    subTitle.setLayoutParams(layoutParams);
    subTitle.setMaxLines(1);
    subTitle.setText("Subtitle");
    textContainer.addView(subTitle);
  }
  
  private void constructPlayPauseImageView() {
    pausePlay = new ImageView(context);
    pausePlay.setId(6);
    LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
    layoutParams.setMargins(0, 0, 9 * DP, 0);
    pausePlay.setImageBitmap(OOCastUtils.getDarkChromecastPauseButton());
    pausePlay.setLayoutParams(layoutParams);
    mainContainer.addView(pausePlay);
  }
  
  private void constructEmptyTextView() {
    emptyText = new TextView(context);
    emptyText.setId(4);
    LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(Gravity.CENTER);
    layoutParams.setMargins(0, 10 * DP, 0, 10 *DP);
    emptyText.setTextColor(Color.WHITE);
    emptyText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    emptyText.setLayoutParams(layoutParams);
    emptyText.setMaxLines(1);
    emptyText.setText("No media information available");
    emptyText.setGravity(Gravity.CENTER);
    mainContainer.addView(emptyText);
  }
  
  @Override
  public void dismiss() {
    super.dismiss();
    DebugMode.logE(TAG, "Dismiss Dialog");
    if (castManager != null) {
      castManager.get().removeMiniController(this);
    }
  }
  
  private void setupCallbacks() {

    pausePlay.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          DebugMode.assertCondition((castManager.get().getCastPlayer() != null), TAG, "castPlayer should never be null when we have a mini controller");
          OOCastPlayer castPlayer = castManager.get().getCastPlayer();
          State state = castPlayer.getState();
          DebugMode.logD(TAG, "Play/Pause button is clicked in default mini controller with state = " + state);
          if (state == State.PLAYING){
            castPlayer.pause();
          } else {
            castPlayer.play();
          }
        }
    });

    mainContainer.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          if (castManager.get().getTargetActivity() != null) {
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
    if (castManager.get().getCurrentActivity() == castManager.get().getTargetActivity()) {
      DebugMode.logD(TAG, "Already in the target activity");
     } else {
      Intent intent = new Intent(context, castManager.get().getTargetActivity());
      intent.putExtra("embedcode", castManager.get().getCastPlayer().getEmbedCode());
      context.startActivity(intent);
     }
    dismiss();
  }
  
  private void updateMetadata() {
    // Currently we do not want to show a mini controller when the related playback is in "COMPLETED" state
    if (!castManager.get().isInCastMode() || castManager.get().getCastPlayer().getState() == State.COMPLETED) {
        hideControls(true);
    } else {
      hideControls(false);
      title.setText(castManager.get().getCastPlayer().getCastItemTitle());
      subTitle.setText(castManager.get().getCastPlayer().getCastItemDescription());
      setIcon(castManager.get().getCastPlayer().getCastImageBitmap());
    }
}
  
  private void setIcon(Bitmap bitmap) {
    icon.setImageBitmap(bitmap);
  }
  
  /*
   * Hides/show the icon and metadata and play/pause if there is no media
   */
  private void hideControls(boolean hide) {
      int visibility = hide ? View.GONE : View.VISIBLE;
      icon.setVisibility(visibility);
      iconContainer.setVisibility(visibility);
      textContainer.setVisibility(visibility);
      emptyText.setText("No media information available");
      emptyText.setVisibility(hide ? View.VISIBLE : View.GONE);
      if (hide) pausePlay.setVisibility(visibility);
  }

  public void updatePlayPauseButtonImage(boolean isPlaying) {
    if (castManager.get().getCastPlayer() != null) {
      if (isPlaying) {
        pausePlay.setImageBitmap(OOCastUtils.getDarkChromecastPauseButton());
        pausePlay.setVisibility(View.VISIBLE);
      } else {
        pausePlay.setImageBitmap(OOCastUtils.getDarkChromecastPlayButton());
        pausePlay.setVisibility(View.VISIBLE);
      }
    } else {
        pausePlay.setVisibility(View.INVISIBLE);
    }
  }

}
