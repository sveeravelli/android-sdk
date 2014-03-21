package com.ooyala.android;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.accessibility.CaptioningManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.ooyala.android.ClosedCaptionsStyle.OOClosedCaptionPresentation;

public class ClosedCaptionsView extends TextView {

	private Caption caption;
	private ClosedCaptionsStyle style;
	private Paint StrokePaint; // Paint for drawing outline
	private Paint textPaint; // Paint for drawing text
	private Rect textBounds; // Rect for a text
	private double textHeight;

	private String existingText = ""; // Store the closed captions text for Roll-up effect
	private String currentText = ""; // The closed captions for this period of time
	private Scroller scroller;

	// Paint-on variables
	private CharSequence paintOnText;
	private int paintOnIndex;
	private long paintOnDelay = 10;
	private final Handler paintOnHandler = new Handler();
	private final Runnable charPainter = new Runnable() {
		@Override
		public void run() {
			setText(paintOnText.subSequence(0, paintOnIndex++));
			if(paintOnIndex <= paintOnText.length()) {
				paintOnHandler.postDelayed(charPainter, paintOnDelay);
			}
		}
	};

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
		return this.caption;
	}

	public void setCaption(Caption caption) {
		this.caption = caption;
		if (this.caption != null && !this.currentText.equals(this.caption.getText())) {
			this.currentText = caption.getText();
			setClosedCaptions(this.caption.getText(), false);
		} else {
			setBackgroundColor(Color.TRANSPARENT);
			setText("");
		}
	}

	// Set specific text on textview (shared by live streams and normal stream)
	private void setClosedCaptions(String text, Boolean isLive) {
		setBackgroundColor(style.backgroundColor);
		// With outline edge type we draw text with Paint so we do not need to show original text in textview
		// However, we still need the original text on textview to figure the position of text for Paint
		// So we set the textColor to transparent for outline edge type
		if (this.style.edgeType == CaptioningManager.CaptionStyle.EDGE_TYPE_OUTLINE) {
			this.setTextColor(Color.TRANSPARENT);
		}
		if (this.style.presentationStyle == ClosedCaptionsStyle.OOClosedCaptionPresentation.OOClosedCaptionRollUp) {
			String splitText = getSplitTextAndUpdateFrame(text); // still need to split the text if it is too long even we do not need to change Frame for roll-up
			setLayoutParams(new FrameLayout.LayoutParams((((View) this.getParent()).getWidth()) * 9 / 10, (int)(this.textHeight * 3.5),  Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM));
			this.setGravity(Gravity.CENTER_HORIZONTAL);
			this.updateBottomMargin();
			// Calculate scrolling distance
			this.setText(this.existingText);
			if (this.getLayout() == null) {
				return;
			}
			int prevBottomLine = this.getLayout().getLineTop(this.getLineCount());
			// When the text has different lines scroll could be not smooth so make up the missing lines up to three lines
			int lineNum = splitText.split("\n").length;
			if (lineNum == 1) {
				splitText = "\n" + splitText + "\n";
			} else if (lineNum == 2) {
				splitText = "\n" + splitText;
			}
			this.existingText = this.existingText + "\n" + splitText + "\n"; // these two \n are for sperating prev and post closed captions
			this.setText(this.existingText);
			if (this.getLayout() == null) {
				return;
			}
			int currentBottomLine = this.getLayout().getLineTop(this.getLineCount());
			// This magic line make sure scroll will not start from top;
			this.setMovementMethod(new ScrollingMovementMethod());

			this.scroller = new Scroller(this.getContext(), new LinearInterpolator());
			setScroller(this.scroller);
			// TODO: choose duration for live since live does not contain caption object
			int currentDuration = 600;
			if (!isLive) {
				currentDuration = (int) (100 * ((caption._end - caption._begin) * 2 / 3));
			}
			this.scroller.startScroll(0, prevBottomLine -  (currentBottomLine - prevBottomLine), 0, currentBottomLine - prevBottomLine, currentDuration);
			// Clean the textView before it is too big. When there are too many texts in textView it will scroll really slow and unsmoonthly
			// We can clean the textview every 100 or 200 lines (the number does not matter that much)
			if (this.getLineCount() >= 100) {
				this.existingText = "\n";
			}
		} else if (this.style.presentationStyle == ClosedCaptionsStyle.OOClosedCaptionPresentation.OOClosedCaptionPaintOn) {
			//TODO: choose paint-on delay for live since live does not contain caption object
			if (!isLive) {
				this.paintOnDelay = Math.min(this.paintOnDelay, (long)(caption._end - caption._begin) * 2 / (caption.getText().length() * 3));
			}
			this.setGravity(Gravity.LEFT | Gravity.TOP);
			String splitText = getSplitTextAndUpdateFrame(text);
			paintOn(splitText);
		} else {
			this.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
			String splitText = getSplitTextAndUpdateFrame(text);
			setText(splitText);
		}
	}

	// Useful for when captions are coming live, not from pre-defined file
	public void setCaptionText(String text) {
		if (text != null) {
			setBackgroundColor(style.backgroundColor);
			setClosedCaptions(text, true);
		} else {
			setBackgroundColor(Color.TRANSPARENT);
			setText("");
		}
	}

	public void updateEdgeStyle() {
		if (this.style.edgeType == CaptioningManager.CaptionStyle.EDGE_TYPE_OUTLINE) {// Setup stroke paint;
			this.StrokePaint = new Paint();
			this.StrokePaint.setAntiAlias(true);
			this.StrokePaint.setTextSize(super.getTextSize());
			this.StrokePaint.setStyle(Paint.Style.STROKE);
			this.StrokePaint.setColor(this.style.edgeColor);
			this.StrokePaint.setTypeface(super.getTypeface());
			this.StrokePaint.setFlags(super.getPaintFlags());
			this.StrokePaint.setStrokeWidth(4);

			// Setup text paint
			this.textPaint = new Paint();
			this.textPaint.setAntiAlias(true);
			this.textPaint.setTextSize(super.getTextSize());
			this.textPaint.setStyle(Paint.Style.FILL);
			this.textPaint.setColor(this.style.textColor);
			this.textPaint.setTypeface(super.getTypeface());
			this.textPaint.setFlags(super.getPaintFlags());
			setTextColor(Color.TRANSPARENT);
		} else if (this.style.edgeType == CaptioningManager.CaptionStyle.EDGE_TYPE_DROP_SHADOW) {
			setShadowLayer(4, 4, 4, this.style.edgeColor);
		}
	}

	public void setStyle(ClosedCaptionsStyle style) {
		this.style = style;
		this.setTextSize(style.textSize);
		String testString = "just for height"; // any text including "j" and "f" can define the max height for this font size
		super.getPaint().getTextBounds(testString, 0, testString.length(), this.textBounds);
		this.textHeight = this.textBounds.height() * 1.5;
		this.setTextColor(this.style.textColor);
		this.setTypeface(style.textFont);
		this.updateEdgeStyle();
		this.updateBottomMargin();
	}

	public void updateBottomMargin() {
		MarginLayoutParams params = (MarginLayoutParams) this.getLayoutParams();
		params.bottomMargin = style.bottomMargin;
		this.setLayoutParams(params);
	}

	public void initStyle() {
		this.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,  Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM));
		this.textBounds = new Rect();
		this.setPadding(10, 10, 10, 10);
		this.setEnabled(false);
	}

	public void paintOn(CharSequence text) {
		paintOnText = text;
		paintOnIndex = 0;

		setText("");
		paintOnHandler.removeCallbacks(charPainter);
		paintOnHandler.postDelayed(charPainter, paintOnDelay);
	}

	// Set frame of text view based on the text size
	public String getSplitTextAndUpdateFrame(String text) {
		// Current maxWidth of closed caption view in this device
		int maxWidth = (int)(((View)this.getParent()).getWidth() * 0.9 - this.getPaddingLeft() - this.getPaddingRight());
		ArrayList<String> lines = new ArrayList<String>(Arrays.asList(text.split("\n")));
		this.setText(text); // setText temporally to help calculate where those text should be when they are added to textView

		// Find the width of the longest line in currently closed caption
		// Set width of closed caption to that width if the longest line is shorter than the maxWidth
		// Otherwise, set width of closed caption to the maxWidth for this device
		int longestLineWidth = 0;
		for (String line : lines) {
			super.getPaint().getTextBounds(line, 0, line.length(), this.textBounds);
			if (this.textBounds.width() > longestLineWidth) {
				longestLineWidth = this.textBounds.width();
				if (this.textBounds.width() >= maxWidth) {
					break;
				}
			}
		}
		int width = longestLineWidth;
		ArrayList<String> splitLines = new ArrayList<String>();

		int lineNum = lines.size();
		String splitText = text;
		if (longestLineWidth >= maxWidth) {
			// LongestLineWidth is greater than maxWidth so we need to split at least one line of this closed caption
			width = maxWidth;
			Rect currentBound = new Rect();
			for (String line : lines) {
				int prevWhiteSpaceIndex = 0; // The index where the '\n' char should be insert if this line need to be split
				for (int i = 0; i < line.length(); i++) {
					String subline = line.substring(0, i + 1);
					super.getPaint().getTextBounds(subline, 0, subline.length(), currentBound);
					// This line need to be split
					// Currently we only split once at most per line so if we get a super lone line
					// some later part of that line could be invisiable
					if (currentBound.width() > width) {
						splitLines.add(line.substring(0, prevWhiteSpaceIndex));
						splitLines.add(line.substring(prevWhiteSpaceIndex + 1, line.length()));
						lineNum++;
						break;
					}
					// This line does not need to be split
					if (i == line.length() - 1) {
						splitLines.add(line);
					}
					if (line.charAt(i) == ' ') {
						prevWhiteSpaceIndex = i;
					}
				}
			}
			// Construct the new text based on frame width
			splitText = splitLines.get(0);
			for (int i = 1; i < splitLines.size(); i++) {
				splitText = splitText + "\n" + splitLines.get(i);
			}
		}

		// If we always use fix size frame for roll-up
		// For paint-on and pop-on we change the frame size based on how long the text is and the font size
		if (this.style.presentationStyle != OOClosedCaptionPresentation.OOClosedCaptionRollUp) {
			// Set 150 as the smallest width for shortest text
			this.setLayoutParams(new FrameLayout.LayoutParams(Math.max(150, width * 10 / 9 + this.getPaddingLeft() + this.getPaddingRight()), (int)(lineNum * this.textHeight + (this.getPaddingBottom() + this.getPaddingTop())),  Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM));
			this.updateBottomMargin();
		}
		return splitText;
	}

	// Draw text and stroke for outline edge type
	@Override
	public void onDraw(Canvas canvas) {
		// Only draw outline when the edge type is EDGE_TYPE_OUTLINE
		if (this.style.edgeType == CaptioningManager.CaptionStyle.EDGE_TYPE_OUTLINE) {
			String[] lines = super.getText().toString().split("\n");

			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				if (this.getLayout() != null && i < this.getLineCount()) {
					int currentVeriticalOffset = this.getLayout().getLineTop(i) + this.getBaseline();
					super.getPaint().getTextBounds(line, 0, line.length(), this.textBounds);
					int leftPadding = (int) ((this.getWidth() - this.textBounds.width()) * 0.5); // Center the text

					// Paint-on always starts from left
					if (this.style.presentationStyle == OOClosedCaptionPresentation.OOClosedCaptionPaintOn) {
						leftPadding = 25;
					}
					canvas.drawText(line, leftPadding, currentVeriticalOffset, this.StrokePaint);
					canvas.drawText(line, leftPadding, currentVeriticalOffset, this.textPaint);
				}
			}
		}
		super.onDraw(canvas);
	}
}
