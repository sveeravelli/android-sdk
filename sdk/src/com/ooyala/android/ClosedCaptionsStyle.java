package com.ooyala.android;

import android.graphics.Typeface;

public class ClosedCaptionsStyle {
  private int color;
  private int backgroundColor;
  private Typeface font;
  
  public ClosedCaptionsStyle(int color, int backgroundColor, Typeface font) {
    this.color = color;
    this.backgroundColor = backgroundColor;
    this.font = font;
  }
  
  public int getColor() {
    return color;
  }
  public void setColor(int color) {
    this.color = color;
  }

  public int getBackgroundColor() {
    return backgroundColor;
  }
  
  public void setBackgroundColor(int backgroundColor) {
    this.backgroundColor = backgroundColor;
  }
  
  public Typeface getFont() {
    return font;
  }
  public void setFont(Typeface font) {
    this.font = font;
  }
}
