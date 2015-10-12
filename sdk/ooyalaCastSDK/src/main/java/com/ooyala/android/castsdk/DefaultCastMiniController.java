package com.ooyala.android.castsdk;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.libraries.cast.companionlibrary.widgets.MiniController;

public class DefaultCastMiniController extends MiniController {
  protected Bitmap pauseImageBitmap;
  protected Bitmap playImageBitmap;
  protected int streamType = MediaInfo.STREAM_TYPE_BUFFERED;;

  public DefaultCastMiniController(Context context) {
    super(context);
    loadImages();
  }

  public DefaultCastMiniController(Context context, AttributeSet attrs) {
    super(context, attrs);
    loadImages();
  }

  private void loadImages() {
    pauseImageBitmap = CastUtils.getDarkChromecastPauseButton();
    playImageBitmap = CastUtils.getDarkChromecastPlayButton();
  }

  @Override
  public void setStreamType(int type) {
    super.setStreamType(type);
    streamType = type;
  }

  @Override
  public void setPlaybackStatus(int state, int idleReason) {
    switch (state) {
      case MediaStatus.PLAYER_STATE_PLAYING:
        mPlayPause.setVisibility(View.VISIBLE);
        mPlayPause.setImageBitmap(pauseImageBitmap);
        setLoadingVisibility(false);
        break;
      case MediaStatus.PLAYER_STATE_PAUSED:
        mPlayPause.setVisibility(View.VISIBLE);
        mPlayPause.setImageBitmap(playImageBitmap);
        setLoadingVisibility(false);
        break;
      case MediaStatus.PLAYER_STATE_IDLE:
        switch (streamType) {
          case MediaInfo.STREAM_TYPE_BUFFERED:
            mPlayPause.setVisibility(View.INVISIBLE);
            setLoadingVisibility(false);
            break;
          case MediaInfo.STREAM_TYPE_LIVE:
            if (idleReason == MediaStatus.IDLE_REASON_CANCELED) {
              mPlayPause.setVisibility(View.VISIBLE);
              mPlayPause.setImageBitmap(playImageBitmap);
              setLoadingVisibility(false);
            } else {
              mPlayPause.setVisibility(View.INVISIBLE);
              setLoadingVisibility(false);
            }
            break;
        }
        break;
      case MediaStatus.PLAYER_STATE_BUFFERING:
        mPlayPause.setVisibility(View.INVISIBLE);
        setLoadingVisibility(true);
        break;
      default:
        mPlayPause.setVisibility(View.INVISIBLE);
        setLoadingVisibility(false);
        break;
    }
  }
  private void setLoadingVisibility(boolean show) {
    mLoading.setVisibility(show ? View.VISIBLE : View.GONE);
  }
}