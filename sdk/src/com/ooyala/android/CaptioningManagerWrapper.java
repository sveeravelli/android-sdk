package com.ooyala.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.accessibility.CaptioningManager;

public class CaptioningManagerWrapper {

  @SuppressLint("NewApi")
  public static void updateClosedCaptionsStyleFromCaptioningManager(ClosedCaptionsStyle style, Context context) {
    CaptioningManager captioningManager = (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
    CaptioningManager.CaptionStyle captionStyle = captioningManager.getUserStyle();

    style.textSize = captioningManager.getFontScale() * 26;
    style.textFont = captionStyle.getTypeface();
    style.textColor = captionStyle.foregroundColor;
    style.backgroundColor = captionStyle.backgroundColor;
    style.edgeType = captionStyle.edgeType;
    style.edgeColor = captionStyle.edgeColor;
  }
}
