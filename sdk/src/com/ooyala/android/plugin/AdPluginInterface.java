package com.ooyala.android.plugin;

import com.ooyala.android.player.PlayerInterface;

public interface AdPluginInterface extends PlayerInterface {
  /**
   * This is called when content changed
   * 
   * @return a token string if plugin wants take control, null otherwise
   */
  public String onContentChanged();

  /**
   * This is called before start playing content so plugin can play preroll
   * 
   * @return a token string if plugin wants take control, null otherwise
   */
  public String onInitialPlay();

  /**
   * This is called when playhead is updated so plugin can play midroll
   * 
   * @return a token string if plugin wants take control, null otherwise
   */
  public String onPlayheadUpdate(int playhead);

  /**
   * This is called before finishing playing content so plugin can play postroll
   * 
   * @return a token string if plugin wants take control, null otherwise
   */
  public String onContentFinished(); // put your postrolls here

  /**
   * This is called when a cue point is reached so plugin can play midroll
   * 
   * @return a token string if plugin wants take control, null otherwise
   */
  public String onCuePoint(int cuePointIndex);

  /**
   * This is called when an error occured when playing back content
   * 
   * @return a token string if plugin wants take control, null otherwise
   */
  public String onContentError(int errorCode);

  /**
   * This is called when control is handed over to the plugin
   * 
   * @param token
   *          passed from plugin in previous calls.
   */
  public void onAdModeEntered(String token);
}