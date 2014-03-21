package com.ooyala.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.accessibility.CaptioningManager;

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

	@SuppressLint("NewApi")
	public ClosedCaptionsStyle(Context context) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			CaptioningManager captioningManager = (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
			CaptioningManager.CaptionStyle captionStyle = captioningManager.getUserStyle();
			this.textSize = captioningManager.getFontScale() * 26;
			this.textFont = captionStyle.getTypeface();
			this.textColor = captionStyle.foregroundColor;

			this.backgroundColor = captionStyle.backgroundColor;

			this.edgeType = captionStyle.edgeType;
			this.edgeColor = captionStyle.edgeColor;
		} else {
			this.textSize = 26;
			this.textFont = Typeface.DEFAULT;
			this.textColor = Color.WHITE;

			this.backgroundColor = Color.BLACK;

			this.edgeType = CaptioningManager.CaptionStyle.EDGE_TYPE_NONE;
			this.edgeColor = Color.TRANSPARENT;
		}
		this.presentationStyle = OOClosedCaptionPresentation.OOClosedCaptionPopOn; // default style
	}
}
