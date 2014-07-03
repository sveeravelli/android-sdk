package com.ooyala.android.plugin;

import com.ooyala.android.player.PlayerInterface;

class TestPlugin implements AdPluginInterface {

  @Override
  public void reset() {
    // TODO Auto-generated method stub

  }

  @Override
  public void suspend() {
    // TODO Auto-generated method stub

  }

  @Override
  public void resume() {
    // TODO Auto-generated method stub

  }

  @Override
  public void destroy() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean onContentChanged() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean onInitialPlay() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean onPlayheadUpdate(int playhead) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean onContentFinished() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean onCuePoint(int cuePointIndex) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean onContentError(int errorCode) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void onAdModeEntered() {
    // TODO Auto-generated method stub

  }

  @Override
  public PlayerInterface getPlayerInterface() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void resetAds() {
    // TODO Auto-generated method stub

  }

  @Override
  public void skipAd() {
    // TODO Auto-generated method stub

  }

  @Override
  public ChangeNotifierInterface getChangeNotifier() {
    // TODO Auto-generated method stub
    return null;
  }

}
