package com.ooyala.android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;

public class ClosedCaptionsStyle {
	public int textColor;
	public float textSize;
	public float textOpacity;
	public Typeface textFont;

	public int backgroundColor;
	public int backgroundOpacity;

	public int bottomMargin;

	public int edgeType;
	public int edgeColor;

	public OOClosedCaptionPresentation presentationStyle;
	public enum OOClosedCaptionPresentation {
		/** text that appears all at once */
		OOClosedCaptionPopOn,
		/** text that scrolls up as new text appears */
		OOClosedCaptionRollUp,
		/** text where each new letter or word is displayed as it arrives */
		OOClosedCaptionPaintOn
	};

  public ClosedCaptionsStyle(Context context) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
		  CaptioningManagerWrapper.updateClosedCaptionsStyleFromCaptioningManager(this, context);
		} else {
			this.textSize = 26;
			this.textFont = Typeface.DEFAULT;
			this.textColor = Color.WHITE;

			this.backgroundColor = Color.BLACK;

			this.edgeType = 0;
			this.edgeColor = Color.TRANSPARENT;
		}
		this.presentationStyle = OOClosedCaptionPresentation.OOClosedCaptionPopOn; // default style
	}
}
