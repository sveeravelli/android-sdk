package com.ooyala.chromecastsampleapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.castsdk.OOCastManager;
import com.ooyala.android.castsdk.OOMiniController;
import com.ooyala.android.util.DebugMode;
import com.ooyala.chromecastsampleapp.R;

public class CustomizedMiniController extends RelativeLayout implements OOMiniController {

  private static final String TAG = "CustomizedMiniController";

  private static OOCastManager castManager;
  
  protected ImageView icon;
  protected TextView title;
  protected TextView subTitle;
  protected ImageView playPause;
  private View container;
  private Drawable pauseDrawable;
  private Drawable playDrawable;
  
  public CustomizedMiniController(Context context, AttributeSet attrs) {
    super(context, attrs);
    LayoutInflater inflater = LayoutInflater.from(context);
    View miniControllerView = inflater.inflate(R.layout.customized_ooyala_mini_controller, this);
    pauseDrawable = getResources().getDrawable(R.drawable.ic_mini_controller_pause);
    playDrawable = getResources().getDrawable(R.drawable.ic_mini_controller_play);
    loadViews(miniControllerView);
    setupCallbacks();
  }
  
  public void setCastManager(OOCastManager castManager) {
    CustomizedMiniController.castManager = castManager;
  }
  
  private void loadViews(View miniControllerView) {
    icon = (ImageView) miniControllerView.findViewById(R.id.iconView);
    title = (TextView) miniControllerView.findViewById(R.id.titleView);
    subTitle = (TextView) miniControllerView.findViewById(R.id.subTitleView);
    playPause = (ImageView) miniControllerView.findViewById(R.id.playPauseView);
    container = miniControllerView.findViewById(R.id.bigContainer);
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
      playPause.setImageDrawable(pauseDrawable);
    } else {
      playPause.setVisibility(View.VISIBLE);
      playPause.setImageDrawable(playDrawable);
    }  
  }
  
  public void updateVisibility() {
    DebugMode.logD(TAG, "Update Mini Controller Visibility");
    if (castManager != null && castManager.getCurrentCastPlayer() != null) {
      super.setVisibility(VISIBLE);
      hideControls(false);
      updateUIInfo();
    } else {
      super.setVisibility(GONE);
    }
  }
  
  private void hideControls(boolean hide) {
    int visibility = hide ? View.GONE : View.VISIBLE;
    icon.setVisibility(visibility);
    title.setVisibility(visibility);
    subTitle.setVisibility(visibility);
    if (hide) playPause.setVisibility(visibility);
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
























































