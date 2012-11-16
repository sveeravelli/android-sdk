package com.nextreaming.nexplayerengine;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import com.nextreaming.nexplayerengine.NexClosedCaption.TextBlink;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;

import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

public class NexCaptionRendererFor3GPPTT extends View {

	
	private int m_width = 0;
	private int m_height = 0;
	private int m_x = 0;
	private int m_y = 0;
	private int m_videoWidth = 0;
	private int m_videoHeight = 0;

	private Rect forceBox = null;
	
	private NexClosedCaption m_caption = null;
	private Paint m_paint = null;
	
	private String m_str = null;
	private SpannableString m_ss = null;
	private TextPaint m_textPaint = null;
	private StaticLayout m_layout = null;
	private ForegroundColorSpan m_HighlightFGColorSpan = null;
	
	private Rect m_regionRect = null;
	
	private short[] m_styleStart = null;
	private short[] m_styleEnd = null;
	private int[] m_fontColor = null;
	private int[] m_fontSize = null;
	private boolean[] m_isBold = null;
	private boolean[] m_isUnderline = null;
	private boolean[] m_isItalic = null;
	
	private boolean m_isFlash = false;
	private Handler m_handler = new Handler();
	private int redrawTime = 0;
	private ForegroundColorSpan[] m_BlinkColorSpan = null;
	private int[] m_blinkStartOffset= null;
	private int[] m_blinkEndOffset= null;
	private boolean m_ignoreJustification = false;
	
	private boolean m_isScrollOn = false;
	private int m_iScrollDirection = 0;
	private boolean m_isScrollIn = false;
	private boolean m_isScrollOut = false;
	private int m_iScrollDelay = 0;
	private int m_iScrollStart = 0;
	private int m_iScrollEnd = 0; 
	private int m_iScrollDuration = 0;
	
	private float m_scale = 0.0f;
	
	private int m_styleRecord_Count = 0;
	private int m_charBytes = 1;//default = 1.
	
	private long m_prevScrollTime = 0;
	private int m_iVerJust = 0;
	private int m_iHorJust = 0;
	private boolean m_scrollHold = false;

	//private Matrix m_matrix = new Matrix();
	
	private final String LOG_TAG = "NexCaptionRenderer for 3GPPTT";
	
    //Constructor for 3GPP Timed Text.
    //param 1 : The handle for the player.
	public NexCaptionRendererFor3GPPTT(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * \brief This property clears the screen.
	 */
	public void clear()
	{
		m_regionRect = new Rect();
	}
	
	/**
	 * \brief This property specifies the scaling of the screen and video size.
	 *
	 * \param videoWidth The width of the display video.
	 * \param videoHeight The height of the display  video.
	 * \param surfaceWidth The  width of the screen.
	 * \param surfaceHeight The height of the screen.
	 * \param left The video display's horizontal (X) position.
     * \param top The video display's vertical (Y) position.
	 *
	 * \returns TRUE if <b>bold</b>, FALSE if not.
	 */
	public void setVideoSizeInformation(int videoWidth, int videoHeight, int surfaceWidth, int surfaceHeight, int left, int top)
	{
		Log.d(LOG_TAG, "Call Render Area. w : " + surfaceWidth + " h : " + surfaceHeight);
		m_videoWidth = videoWidth;
		m_videoHeight = videoHeight;
		m_width = surfaceWidth;
		m_height = surfaceHeight;
		m_x = left;
		m_y = top;
	}
	
	/**
	 * \brief This property overrides the text box and sets it to be able to be moved anywhere desired. 
	 *
	 * \param textBox Override to anywhere desired.
	 */
	public void setTextBoxOnLayout(Rect textBox)
	{
		forceBox = textBox;
	}
	/**
	 * \brief This property describes the video's scale ratio. 
	 *
	 * \param scale The video's scale ratio.
	 */
	public void setScaleRatio(float scale)
	{
		m_scale = scale;
		Log.d(LOG_TAG, "Set Scale : " + m_scale);
	}
	/**
	 * \brief This property specifies the 3GPPTT data to the renderer.
	 *
	 * \param data 3GPPTT data.
	 */
	public void setData(NexClosedCaption data)
	{
		Log.d(LOG_TAG, "try SetData");
		short highlight_start = 0;
		short highlight_end = 0;
		AbsoluteSizeSpan fontsizeSpan = null;
		Alignment align = Alignment.ALIGN_NORMAL;
		
		m_caption = data;
		byte[] string = m_caption.getTextDataFor3GPPTT();
		String strEncoding = "UTF-8";
		//check encoding types
		try
		{
			if(string[0] == (byte)0xFE && string[1] == (byte)0xFF)
			{
				strEncoding = "UTF-16";
			}
			else if(string [0] == (byte)0xFF && string[1] == (byte)0xFE)
			{
				strEncoding = "UTF-16";
			}
			else if(string[0] == (byte)0xEF && string[1] == (byte)0xBB && string[2] == (byte)0xBF)
			{
				strEncoding = "UTF-8";
			}
			else
			{
				strEncoding = "UTF-8";
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		try {
			m_str = new String(m_caption.getTextDataFor3GPPTT(), 0, m_caption.getTextDataFor3GPPTT().length, strEncoding);
			Log.d(LOG_TAG, "SetData String - " + m_str);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_ss = new SpannableString(m_str);
		m_textPaint = new TextPaint();
		
		if(m_caption.getTextHighlightColor() != null)
		{
			m_HighlightFGColorSpan = new ForegroundColorSpan(m_caption.getTextHighlightColor().getHighlightColor());
		}
		else
		{
			m_HighlightFGColorSpan = new ForegroundColorSpan(m_caption.getForegroundColorFor3GPPTT());
		}
		
		

		if(m_caption.getTextStyle() != null)
		{
			Log.d(LOG_TAG, "Style Set.");
			m_styleRecord_Count = m_caption.getTextStyle().getCount();
			m_styleStart = new short[m_styleRecord_Count];
			m_styleEnd = new short[m_styleRecord_Count];
			m_fontColor = new int[m_styleRecord_Count];
			m_fontSize = new int[m_styleRecord_Count];
			m_isBold = new boolean[m_styleRecord_Count];
			m_isUnderline = new boolean[m_styleRecord_Count];
			m_isItalic = new boolean[m_styleRecord_Count];
		}
		if(m_styleRecord_Count != 0)
		{
			Log.d(LOG_TAG, "Style Count ." + m_styleRecord_Count);
			for(int i=0;i<m_styleRecord_Count;i++)
			{
				m_styleStart[i] = (short) (m_caption.getTextStyle().getStyleEntry(i).getStartChar() / m_charBytes);
				m_styleEnd[i] = (short) (m_caption.getTextStyle().getStyleEntry(i).getEndChar() / m_charBytes);
				if(m_styleStart[i] == 0 && m_styleEnd[i] == 0 && m_str.length() > 0)
				{
					//apply whole string.
					m_styleEnd[i] = (short) (m_str.length());
				}
				m_fontColor[i] = m_caption.getTextStyle().getStyleEntry(i).getFontColor();
				Log.d(LOG_TAG, "style start : " + m_styleStart[i] + " end : " + m_styleEnd[i] + " " + m_fontColor[i]);
				m_fontSize[i] = (int)(m_caption.getTextStyle().getStyleEntry(i).getFontSize()*(m_scale==0?1.0:m_scale));
				m_isBold[i] = m_caption.getTextStyle().getStyleEntry(i).getBold();
				m_isUnderline[i] = m_caption.getTextStyle().getStyleEntry(i).getUnderline();
				m_isItalic[i] = m_caption.getTextStyle().getStyleEntry(i).getItalic();
			}
		}
		
		if(m_styleRecord_Count > 0)
		{
			for(int i=0;i<m_styleRecord_Count;i++)
			{
				if(m_str.length() >= m_styleEnd[i])
				{
					ForegroundColorSpan fgColorSpan = new ForegroundColorSpan(m_fontColor[i]);
					m_ss.setSpan(fgColorSpan, m_styleStart[i], m_styleEnd[i], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					m_textPaint.setTextSize((float)m_fontSize[i]);
					if(m_isUnderline[i])
					{
						StyleSpan sSpan = new StyleSpan(android.graphics.Typeface.ITALIC);
						m_ss.setSpan(sSpan, m_styleStart[i], m_styleEnd[i], Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
					}
					if(m_isBold[i])
					{
						StyleSpan sSpan = new StyleSpan(android.graphics.Typeface.BOLD);
						m_ss.setSpan(sSpan, m_styleStart[i], m_styleEnd[i], Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
					}
					if(m_isUnderline[i])
					{
						UnderlineSpan ulSpan = new UnderlineSpan();
						m_ss.setSpan(ulSpan, m_styleStart[i], m_styleEnd[i], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					if(m_fontSize[i] != 0)
					{
						AbsoluteSizeSpan aSpan = new AbsoluteSizeSpan(m_fontSize[i]);
						m_ss.setSpan(aSpan, m_styleStart[i], m_styleEnd[i], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						fontsizeSpan = aSpan;
					}
					Log.d(LOG_TAG, "Set. Color : " + m_fontColor[i]);
					
				}
			}
		}
		
		if(m_caption.getTextHighlight() != null)
		{
			highlight_start = (short) (m_caption.getTextHighlight().getStartChar() / m_charBytes);
			highlight_end = (short) (m_caption.getTextHighlight().getEndChar() / m_charBytes);
			//3gpp tt was written with Unicode, so offset unit will be multiple of 2.
			if(m_str.length() >= highlight_end)
				m_ss.setSpan(m_HighlightFGColorSpan, highlight_start, highlight_end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		if(m_caption.getTextBlink() != null)
		{
			TextBlink[] blink = m_caption.getTextBlink();
			int length = blink.length;
			m_BlinkColorSpan = new ForegroundColorSpan[length];
			m_blinkStartOffset = new int[length];
			m_blinkEndOffset = new int[length];
			for(int i=0;i<length;i++)
			{
				m_BlinkColorSpan[i] =new ForegroundColorSpan(m_caption.getBackgroundColorFor3GPPTT());
				m_blinkStartOffset[i] = blink[i].getStartOffset();
				m_blinkEndOffset[i] = blink[i].getEndOffset(); 
				if(m_blinkEndOffset[i] > m_str.length())
					m_blinkEndOffset[i] = m_str.length();
			}
			m_isFlash = true;
		}
		else
		{
			m_isFlash = false;
		}
		
		Rect tBox = m_caption.getTextBox();
		Log.d(LOG_TAG, "default TBox : " + tBox);
		
		int [] regionCoord = m_caption.getTextboxCoordinatesFor3GPPTT();
		if(regionCoord[2] == 0 && regionCoord[3] == 0)//how can I handle this coordinates set to 0?
		{
			int fontSize = (int)(m_fontSize[0]/m_scale);
			SpannableString ss = new SpannableString(m_str);
			ss.setSpan(new AbsoluteSizeSpan(fontSize), 0, m_str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			TextPaint textPaint = new TextPaint();
			StaticLayout tempLayout = new StaticLayout(ss, textPaint, getWidth(), Alignment.ALIGN_NORMAL, 1, 0, false);
			m_ss.removeSpan(fontsizeSpan);
			m_ss.setSpan(new AbsoluteSizeSpan(fontSize), 0, m_str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			//align = Alignment.ALIGN_CENTER;
			Log.d(LOG_TAG, "Layout Height : " + tempLayout.getHeight() + "vid H " + m_videoHeight + "fsize : " + fontSize);
			regionCoord[0] = tBox.left;
			regionCoord[1] = (int)(m_videoHeight*0.9) - tempLayout.getHeight();//tBox.top + (tempLayout.getHeight() + (int)(m_videoHeight*0.9));
			regionCoord[2] = tBox.right - tBox.left;
			regionCoord[3] = ((int)(m_videoHeight*0.9) - regionCoord[1]);
			
			m_ignoreJustification = true;
		}
		else
		{
			m_ignoreJustification = true;
		}
		m_regionRect = new Rect();
		m_regionRect.left = regionCoord[0];
		m_regionRect.top = regionCoord[1];
		m_regionRect.right = regionCoord[0] + regionCoord[2];
		m_regionRect.bottom = regionCoord[1] + regionCoord[3];
		
		Log.d(LOG_TAG, "RECT : " + m_regionRect);
		
		if(m_scale != 0.0f)
		{
			m_regionRect.left = (int) ((m_width - (int)(regionCoord[2]*m_scale)) / 2);
			m_regionRect.top = (int)(regionCoord[1]*m_scale + m_y);
			m_regionRect.right = m_regionRect.left + (int)(regionCoord[2]*m_scale);
			m_regionRect.bottom = m_regionRect.top + (int)(regionCoord[3]*m_scale);
			Log.d(LOG_TAG, "Mod RECT : " + m_regionRect);
		}
		
		if(forceBox != null )
		{
			m_regionRect.left = forceBox.left;
			m_regionRect.right = forceBox.right;
			m_regionRect.top = forceBox.top;
			m_regionRect.bottom = forceBox.bottom;
			Log.d(LOG_TAG, "Forced RECT : " + m_regionRect);
		}

		{
			m_isScrollOn = false;
		}
		
		m_textPaint.setColor(Color.BLACK);
		
		m_iVerJust = m_caption.getVerticalJustification();
		m_iHorJust = m_caption.getHorizontalJustification();
		
				
		m_layout = new StaticLayout(m_ss, m_textPaint, getWidth(), align, 1, 0, false);
		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if(m_paint == null)
		{
			m_paint = new Paint();
		}
		Paint p = m_paint;
		p.reset();
		p.setAntiAlias(true);
		
		if( m_caption == null )
			return;
		
		long uptime = System.currentTimeMillis();
		boolean flashStateOn = (uptime % 400 < 200);
		
		int scrollDelay = 33;
		int scrollProgress = 0;
		float scrollToX = 0;
		float scrollToY = 0;

		Rect subRect = m_regionRect;//m_caption.getTextBox();
				
		int backgroundColor = m_caption.getBackgroundColorFor3GPPTT();
		
		if(m_isFlash)
		{
			if(!flashStateOn)
			{
				for(int i=0;i<m_blinkStartOffset.length;i++)
				{
					m_ss.setSpan(m_BlinkColorSpan[i], m_blinkStartOffset[i], m_blinkEndOffset[i], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
			else
			{
				for(int i=0;i<m_blinkStartOffset.length;i++)
				{
					m_ss.removeSpan(m_BlinkColorSpan[i]);
				}
			}
		}
		
		p.setColor(backgroundColor);
		canvas.drawRect(subRect, p);

		
		canvas.save();
		canvas.clipRect(subRect);
		canvas.translate(m_regionRect.left, m_regionRect.top);
		
		m_layout.draw(canvas);
		
		canvas.restore();
		
		if( m_isFlash ) {
			int flashTime = 200-(int)(uptime%200);
			if( flashTime < redrawTime || redrawTime == 0 )
				redrawTime = flashTime;
		}
		if(scrollDelay != 0 && redrawTime > scrollDelay)
			redrawTime = scrollDelay;
		if( redrawTime > 0 ) {
			m_handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					NexCaptionRendererFor3GPPTT.this.invalidate();
				}
			}, /* uptime + */ redrawTime);
		}
	}
}
