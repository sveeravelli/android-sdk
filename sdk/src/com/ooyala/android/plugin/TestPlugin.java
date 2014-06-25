package com.ooyala.android.plugin;

import com.ooyala.android.player.PlayerInterface;

class TestPlugin implements AdPluginInterface, PlayerInterface {

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
  public void pause() {
    // TODO Auto-generated method stub

  }

  @Override
  public void play() {
    // TODO Auto-generated method stub

  }

  @Override
  public void stop() {
    // TODO Auto-generated method stub

  }

  @Override
  public int currentTime() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int duration() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int buffer() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean seekable() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void seekToTime(int timeInMillis) {
    // TODO Auto-generated method stub

  }

  @Override
  public StateNotifier getStateNotifier() {
    // TODO Auto-generated method stub
    return null;
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
}
