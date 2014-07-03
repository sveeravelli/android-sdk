package com.ooyala.android.plugin;

import java.util.Observer;

/**
 * The interface that must be implemented in order for the plugin to notify UI
 * for the changes. Plugin can use the default implementation provided by
 * Ooyala.
 * 
 * @author michael.len
 * 
 */
public interface ChangeNotifierInterface {
  /**
   * This is called to notify playhead changes to update seekbar
   */
  public void notifyTimeChange();

  /**
   * This is called to notify state changes
   */
  public void notifyStateChange();

  /**
   * This is called to notify buffer percentage changes
   */
  public void notifyBufferChange();

  /**
   * This is called to add an observer
   * 
   * @param o
   *          the observer to be added
   */
  public void addObserver(Observer o);

  /**
   * This is called to delete an observer
   * 
   * @param o
   *          the observer to be deleted
   */
  public void deleteObserver(Observer o);

}
