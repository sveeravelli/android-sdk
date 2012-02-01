package com.ooyala.android;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;

public class ClosedCaptionsView extends TextView {

	private Caption _caption;

	public ClosedCaptionsView(Context context) {
		super(context);
		initStyle();
	}

	public ClosedCaptionsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initStyle();
	}

	public ClosedCaptionsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initStyle();
	}

	public Caption getCaption() {
		return _caption;
	}

	public void setCaption(Caption caption) {
		_caption = caption;
		if (_caption != null) {
		  setBackgroundColor(Color.BLACK);
			setText(caption.getText());
		}
		else {
		  setBackgroundColor(Color.TRANSPARENT);
		  setText("");
		}
	}
	
	public void setStyle(ClosedCaptionsStyle style) {
	  setTextColor(style.getColor());
	  setBackgroundColor(style.getBackgroundColor());
    setTypeface(style.getFont());
	}

	public void initStyle() {
		setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM));
		setMaxLines(5);
		setTextColor(Color.WHITE);
		setTextSize(16);
		setBackgroundColor(Color.TRANSPARENT);
		setGravity(Gravity.CENTER);
		setPadding(20, 0, 20, 20);
	}
}
