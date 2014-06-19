package com.ooyala.android.plugin;

import com.ooyala.android.OoyalaPlayer.State;

class TestPlugin implements AdPluginInterface {

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
  public boolean onAdModeEntered(AdMode mode) {
    // TODO Auto-generated method stub
    return false;
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
  public void seekToTime(int timeInMillis) {
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
  public void reset() {
    // TODO Auto-generated method stub

  }

  @Override
  public void suspend() {
    // TODO Auto-generated method stub

  }

  @Override
  public void suspend(int millisToResume, State stateToResume) {
    // TODO Auto-generated method stub

  }

  @Override
  public void resume() {
    // TODO Auto-generated method stub

  }

  @Override
  public void resume(int millisToResume, State stateToResume) {
    // TODO Auto-generated method stub

  }

  @Override
  public void destroy() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean seekable() {
    // TODO Auto-generated method stub
    return false;
  }

}
