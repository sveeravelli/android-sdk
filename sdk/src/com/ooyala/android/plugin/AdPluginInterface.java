package com.ooyala.android.plugin;

public interface AdPluginInterface {
  public boolean onContentChanged(/* Metadata Object, currentItem Object */);

  public boolean onInitialPlay();

  public boolean onPlayheadUpdate(int playhead);

  public boolean onContentFinished(); // put your postrolls here

  public boolean onCuePoint(int cuePointIndex);

  public boolean onContentError(int errorCode);

  public enum AdMode {
    Preroll, Midroll, Postroll
  };

  public boolean onAdModeEntered(AdMode mode);
}