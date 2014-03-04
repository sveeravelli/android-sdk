package com.ooyala.android;

import android.annotation.SuppressLint;
import android.content.Context;
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

	public OOClosedCaptionPresentation presentationStyle;

	public enum OOClosedCaptionPresentation {
		/** text that appears all at once */
		OOClosedCaptionPopOn,
		/** text that scrolls up as new text appears */
		OOClosedCaptionRollUp,
		/** text where each new letter or word is displayed as it arrives */
		OOClosedCaptionPaintOn
	};

	public int edgeType;
	public int edgeColor;

	//	public ClosedCaptionsStyle(int color, int backgroundColor, Typeface font) {
	//		this.color = color;
	//		this.backgroundColor = backgroundColor;
	//		this.font = font;
	//		this.bottomMargin = 0;
	//	}

	@SuppressLint("NewApi")
	public ClosedCaptionsStyle(Context context) {
		CaptioningManager captioningManager = (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);



		CaptioningManager.CaptionStyle captionStyle = captioningManager.getUserStyle();
		this.textSize = captioningManager.getFontScale() * 26;
		this.textFont = captionStyle.getTypeface();
		this.textColor = captionStyle.foregroundColor;

		this.backgroundColor = captionStyle.backgroundColor;

		this.edgeType = captionStyle.edgeType;
		this.edgeColor = captionStyle.edgeColor;

		this.presentationStyle = OOClosedCaptionPresentation.OOClosedCaptionPopOn; // default style

	}
}
