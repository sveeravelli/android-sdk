package com.ooyala.android.captions;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;

public class ClosedCaptionsStyle {

  public static final int CC_FONT_SP = 16;

	public int textColor;
	public float textSize;
	public float textOpacity;
	public Typeface textFont;

	public int backgroundColor;
	public int backgroundOpacity;

	public int bottomMargin;

	public int edgeType;
	public int edgeColor;

  public ClosedCaptionsStyle(Context context) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
		  CaptioningManagerWrapper.updateClosedCaptionsStyleFromCaptioningManager(this, context);
		} else {
			this.textSize = CC_FONT_SP;
			this.textFont = Typeface.DEFAULT;
			this.textColor = Color.WHITE;

			this.backgroundColor = Color.TRANSPARENT;

			this.edgeType = 0;
			this.edgeColor = Color.TRANSPARENT;
		}
	}
}
