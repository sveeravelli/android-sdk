package com.nextreaming.nexplayerengine;


import com.nextreaming.nexplayerengine.NexClosedCaption.CaptionColor;
import com.nextreaming.nexplayerengine.NexClosedCaption.CaptionMode;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;
import android.view.View;

/**
 * \brief  This class defines the CEA-608 Closed Caption renderer view and displays the information to the user in FULL mode.
 * 
 * While CEA-608 closed captions are treated similar to other subtitle formats when supported in BASIC mode, in order to support
 * all of text attributes and display options of the CEA-608 specifications, it is necessary to create a separate Caption Renderer view
 * in the FULL mode.
 * 
 * This class should thus only be used when displaying CEA-608 closed captions in FULL mode.
 */
public class NexCaptionRenderer extends View {
	
	private int m_Width = 0;
	private int m_Height = 0;
	private int m_X = 0;
	private int m_Y = 0;
	
	private int m_border_X = 0;
	private int m_border_Y = 0;
	
	private NexClosedCaption m_Caption = null;
	private Paint m_paint = null;
	
	private String TAG = "NexCaptionRender";
	
	private Handler m_handler = new Handler();

	private CaptionColor setFgColor = null;
	private CaptionColor setBgColor = null;
	private int setFgOpacity = 0;
	private int setBgOpacity = 0;
	
	private CaptionColor setStrokeColor = null;
	private float setStrokeWidth = 0.0f;
	
	private boolean setBold = false;
	private boolean setShadow = false;
	private AssetManager m_AssetMgr = null;
	private int m_fontIndex = 0;
	
	private boolean setRaise = false;
	private boolean setDepressed = false;
	
	private Typeface m_typeItalic = Typeface.defaultFromStyle(Typeface.ITALIC);
	private Typeface m_typeBoldItalic = Typeface.defaultFromStyle(Typeface.BOLD_ITALIC);
	private Typeface m_typeBold = Typeface.defaultFromStyle(Typeface.BOLD);
	private Typeface m_typeNormal = Typeface.defaultFromStyle(Typeface.NORMAL);

	/**
	 * \brief This sets the CEA-608 closed caption renderer in FULL mode.
	 * 
	 * In order for captions to be legible when they are displayed, the closed caption rendering area
	 * defined here is a rectangle set within the larger video rendering area.  This ensures that when
	 * characters are displayed on the screen, they are not difficult to see because they are flush with the
	 * edge of the video.
	 * 
	 * The parameters \c borderX and \c borderY set a kind of border around the caption rendering area and within the video
	 * rendering area where no captions will appear.  
	 * 
	 * \param context  The handle for the player.
	 * \param borderX  The horizontal indent from the edge of the video rendering area to the caption rendering area.
	 * \param borderY  The vertical indent from the edge of the video rendering area to the caption rendering area.
	 */
	public NexCaptionRenderer(Context context, int borderX, int borderY) {
		super(context);
		m_border_X = borderX;
		m_border_Y = borderY;
	}
	
	/**
	 * \brief This method sets the caption data to be rendered and displayed with CEA-608 closed captions in FULL mode.
	 * 
	 * \param caption  The NexClosedCaption object containing the closed captions and attributes to be displayed.
	 */
	public void SetData(NexClosedCaption caption)
	{
		//Log.d(TAG, "SetData Called.");
		m_Caption = caption;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension( MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec) );
	}
	
	/**
	 * \brief This sets the CEA-608 closed caption rendering area within the displayed video area.
	 * 
	 * CEA-608 closed captions will be displayed in this caption rendering area on the screen within the border defined by NexCaptionRenderer.
	 * 
	 * \param x  The horizontal position of the tope left corner caption rendering area, as an integer.
	 * \param y  The vertical position of the top left corner of the caption rendering area, as an integer. 
	 * \param width  The width of the caption rendering area, as an integer.
	 * \param height  The height of the caption rendering area, as an integer.
	 */
	public void setRenderArea(int x, int y, int width, int height)
	{
		m_X = x;
		m_Y = y;
		m_Width = width;
		m_Height = height;
		
		Log.d(TAG, "SetRenderArea : X = " + x + " Y = " + y + " W = " + width + " H = " + height);
	}
	
	/**
	 * \brief  This clears the current captions.
	 * 
     * Use this if you want to erase captions that are left on the screen.
     *
	 */
	public void makeBlankData()
	{
		m_Caption.makeBlankData();
	}
	/** \brief This sets the CEA-608 caption renderer foreground (text).
	 * 
	 * For a full list of colors , please refer to \ref NexClosedCaption::CaptionColor.
	 * 
	 * \param foreground  The foreground color, or \c null to use the color from the original caption data.
	 * \param fgOpacity  The foreground opacity, from 0 (transparent) to 255 (fully opaque). 
	 */
	public void setFGCaptionColor(CaptionColor foreground, int fgOpacity)
	{
		setFgColor = foreground;
		setFgOpacity = fgOpacity;
	}
	/** \brief This sets the CEA-608 caption renderer background.
	 * 
	 * For a full list of colors , please refer to \ref NexClosedCaption::CaptionColor.
	 * 
	 * \param background  The background color, or \c null to use the color from the original caption data.
	 * \param bgOpacity  The background opacity, from 0 (transparent) to 255 (fully opaque). 
	 */
	public void setBGCaptionColor(CaptionColor background, int bgOpacity)
	{
		setBgColor = background;
		setBgOpacity = bgOpacity;
	}
	/** \brief This sets the CEA-608 caption renderer stroke color and width.
	 * 
	 * For a full list of colors, please refer to \ref NexClosedCaption::CaptionColor. The stroke line width is in 
     * pixels. Anti-aliasing is supported, so fractions of a pixel are allowed.  
	 * 
	 * \param strokeColor  The stroke color, or \c null to use the color from the original caption data.
	 * \param strokeWidth  The stroke width in pixels. 
	 */
	public void setCaptionStroke(CaptionColor strokeColor, float strokeWidth)
	{
		setStrokeColor = strokeColor;
		setStrokeWidth = strokeWidth;
	}
	/** \brief Controls whether captions are displayed in bold text.
	 * 
	 * Caption data includes attributes such as bold and italic.  
	 * 
	 * Normally, the caption renderer displays each character
	 * in normal, bold or italic based on the attributes included in the caption data.  
	 * 
	 * However in some cases (such as for
	 * users with visual impairment) it may be desirable to force the use of bold text.  
	 * 
	 * By enabling this option, the
	 * bold attributes in the caption data are ignored and a bold font is used for all characters.
	 * 
	 * \param isBold   Set this to true to force bold text, or false to use the bold attribute in the original captions.
	 * 
	 */
	public void setBold(boolean isBold)
	{
		setBold = isBold;
	}
	/** \brief This sets whether the CEA-608 captions have a shadow.
	 * 
	 * \param isShadow  Set this to \c true to force shadow text, or \c false for no shadow. 
	 * 
	 */
	public void setShadow(boolean isShadow)
	{
		setShadow = isShadow;
	}
	/** \brief  This sets the font used for the captions.
	 * 
	 * Four typefaces may be specified for different combinations of bold and 
     * italic. The  caption renderer will select the appropriate typeface from 
     * among these based on the CEA-608 captions being displayed.
	 * 
	 * For best results, specify all four typefaces. Any typeface can be set 
     * to \c null, in which case the system default typeface will be used.
	 * 
	 * \param normType          Typeface to be used for captions that are neither bold  nor italic.
	 * \param boldType          Typeface to be used for bold CEA-608 captions. 
	 * \param italicType        Typeface to be used for italic CEA-608 captions.
	 * \param boldItalicType    Typeface to be used for CEA-608 captions that are both and italic.
	 */
	public void setFonts(Typeface normType, Typeface boldType, Typeface italicType, Typeface boldItalicType)
	{
		m_typeBold = boldType!=null?boldType:Typeface.defaultFromStyle(Typeface.BOLD);
		m_typeNormal = normType!=null?normType:Typeface.defaultFromStyle(Typeface.NORMAL);
	}
	
	public void setRaise(boolean isRaise)
	{
		setRaise = isRaise;
	}
	
	public void setDepressed(boolean isDepressed)
	{
		setDepressed = isDepressed;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if( m_paint == null ) {
			m_paint = new Paint();
		}
		Paint p = m_paint;
		p.reset();
		p.setAntiAlias(true);
		
		if( m_Caption == null )
			return;
		
		//Log.d(TAG, "Call OnDraw");
		
		Rect r = new Rect();
		Rect textBounds = new Rect();
		int width = m_Width - (m_border_X*2);//getWidth() - 20;
		int height = m_Height - (m_border_Y*2);//getHeight() - 20;
		int block_width = width/32;
		int block_height = height/16;
		
		boolean isFlash = false;
		long uptime = System.currentTimeMillis();
		boolean flashStateOn = (uptime % 400 < 200);
		
		long rollupTime = m_Caption.getRollUpElapsedTime();
		int rollUpProgress = (int)Math.min(13,rollupTime/33);
		int redrawTime = 0;
		int rollUpRows = m_Caption.getRollUpNumRows();
		int rollUpBase = m_Caption.getRollUpBaseRow();
		
		int row;
		int rollUpOffset;
		boolean bSavedState;
		
		/*
		Log.d(TAG, "UpdateTime : ch1 : " + m_Caption.getCaptionUpdateTime(0)
				+ " ch2 : " + m_Caption.getCaptionUpdateTime(1)
				+ " ch3 : " + m_Caption.getCaptionUpdateTime(2)
				+ " ch4 : " + m_Caption.getCaptionUpdateTime(3));
		
		for(int cc = 0; cc < 4; cc++)
		{
			Log.d(TAG, "Available channel " + cc + " : " + m_Caption.getAvaliableCaptionChannel(cc));
		}
		*///These codes are used for testing of count caption channel.
	
		Paint.FontMetricsInt fmi = new Paint.FontMetricsInt();
		
		CaptionColor color = null;
		for(int rowcount=-1;rowcount<15;rowcount++)
		{
			row = rowcount;
			
			if( m_Caption.getCaptionMode() == CaptionMode.RollUp && rollUpRows > 0 && (row==-1 || (row > rollUpBase - rollUpRows && row <= rollUpBase)) ) 
			{
				row = row==-1?NexClosedCaption.ROLLED_OUT_ROW:row;
				rollUpOffset = block_height - (block_height * rollUpProgress / 13 );
				canvas.save();
				int originx = m_border_X + m_X;
				int originy = (rollUpBase-rollUpRows+1)*block_height + m_border_Y + m_Y;
				canvas.clipRect(originx, originy, originx+width, originy + (rollUpRows * block_height));
				bSavedState = true;
			}
			else if( row == -1 ) 
			{
				continue;
			}
			else
			{
				bSavedState = false;
				rollUpOffset = 0;
			}
			for(int col=0;col<32;col++)
			{
				color = m_Caption.getBGColor(row, col);
				final CaptionColor fgColor = m_Caption.getFGColor(row, col);
				
				//int fg_color = color.getFGColor();
				//Color.argb(Color.alpha(fg_color)*fg_opacity/(255*255), Color.red(fg_color), Color.green(fg_color), Color.blue(fg_color))
				
				r.left = (block_width*(col))+m_border_X + m_X;
				r.right = (block_width*(col+1))+m_border_X + m_X;
				r.top = (block_height*(row))+m_border_Y + m_Y + rollUpOffset;
				r.bottom = (block_height*(row+1))+m_border_Y + m_Y + rollUpOffset;
				
//				if(row == -1)
//				{
//					r.top = (block_height*(row+1))+m_border_Y + m_Y + rollUpOffset;
//					r.bottom = (block_height*(row+2))+m_border_Y + m_Y + rollUpOffset;
//				}
				
				//For debugging.
				//This field makes yellow grid. It shows a text rendered area.
				/*p.setColor(0xFFFFFF00);
				p.setStyle(Paint.Style.STROKE);
				canvas.drawRect(r, p);*/
								
				p.setColor(color.getBGColor());
				p.setStyle(Paint.Style.FILL);
				if(CaptionColor.TRANSPARENT != color)
				{
					//Log.d(TAG, "Rect : " + r);
					if(setBgColor != null)
					{
						int set_color = setBgColor.getFGColor();
						int setColor = Color.argb(setBgOpacity, Color.red(set_color), Color.green(set_color), Color.blue(set_color));
						p.setColor(setColor);
					}
						
					canvas.drawRect(r, p);
					
					if(m_Caption.isUnderline(row, col))
					{
						p.setColor(fgColor.getFGColor());
						if(setFgColor != null)
						{
							int set_color = setFgColor.getFGColor();
							int setColor = Color.argb(setFgOpacity, Color.red(set_color), Color.green(set_color), Color.blue(set_color));
							p.setColor(setColor);
						}
						canvas.drawLine(r.left, r.bottom - 5, r.right, r.bottom - 5, p);
					}
				}
				
				char charCode = m_Caption.getCharCode(row, col);
				if( charCode != 0 ) {
					char[] text = new char[]{charCode};
					p.setColor(fgColor.getFGColor());
					if(setFgColor != null)
					{
						int bg_color = setFgColor.getFGColor();
						int bgColor = Color.argb(setFgOpacity, Color.red(bg_color), Color.green(bg_color), Color.blue(bg_color));
						p.setColor(bgColor);
					}
					p.setTextScaleX(0.9f);
					p.setTextSize(r.height()*4/5);
					p.setTextSkewX(0.0f);
					if(m_Caption.isItalic(row, col))
					{
						if(m_Caption.isLarge(row, col))
						{
							p.setTypeface(m_typeBoldItalic);
							if(!m_typeBoldItalic.isItalic())
							{
								p.setTextSkewX(-0.25f);
							}
						}
						else
						{
							p.setTypeface(m_typeItalic);
							if( !m_typeItalic.isItalic() )
								p.setTextSkewX(-0.25f);
						}
					}
					else
					{
						if(m_Caption.isLarge(row, col))							
							p.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
						else
							p.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
					}
					
					p.getTextBounds(text, 0, 1, textBounds);
					p.getFontMetricsInt(fmi);
					if( m_Caption.isFlashing(row, col) ) {
						isFlash = true;
						if( !flashStateOn )
							continue;
					}
					//Log.d(TAG, "RenderText : ");
					if(m_fontIndex != 0)
					{
						switch(m_fontIndex)
						{
							case 1://Fanwood(simillar Fanwood)
							{
								//p.setTypeface(Typeface.createFromAsset(m_AssetMgr, "fanwood-webfont.ttf"));
								break;
							}
							default:
								break;
						}
					}
					if(setBold)
					{
						p.setTypeface(m_typeBold);
					}
					if(setShadow)
					{
						p.setShadowLayer(2, 0, 0, 0xFF000000);
					}

					float[] direction = null;
					
					if(setRaise)
					{
						float[] setDirection = { -1.0f, -1.0f, -1.0f }; // Raised
						direction = setDirection;
					}
					
					if(setDepressed)
					{
						float[] setDirection = {1, 1, -1}; // Depressed
						direction = setDirection;
					}
					
					if(direction != null)
					{
						p.setMaskFilter(new EmbossMaskFilter( direction, (float) 0.5, 8, 3 ));
					}
					else
					{
						p.setMaskFilter(null);
					}
					canvas.drawText(text, 0, 1, r.left + (r.width() - textBounds.width())/2, r.top + (r.height()-fmi.ascent)/2, p);
					p.setMaskFilter(null);
					if(setShadow)
					{
						p.setShadowLayer(0, 0, 0, 0x00000000);
					}
					if(setStrokeColor != null && setStrokeWidth != 0)
					{
						p.setStyle(Paint.Style.STROKE);
						int stroke_color = setStrokeColor.getFGColor();
						int strokeColor = Color.argb(255, Color.red(stroke_color), Color.green(stroke_color), Color.blue(stroke_color));
						p.setColor(strokeColor);
						p.setStrokeWidth(setStrokeWidth);
						canvas.drawText(text, 0, 1, r.left + (r.width() - textBounds.width())/2, r.top + (r.height()-fmi.ascent)/2, p);
					}
					
				}
			}
			if( bSavedState )
				canvas.restore();
		}
		
		if( isFlash ) {
			int flashTime = 200-(int)(uptime%200);
			if( flashTime < redrawTime || redrawTime == 0 )
				redrawTime = flashTime;
		}
		
		if( rollUpRows>0 && rollUpProgress < 13 && (redrawTime==0||redrawTime<33) )
			redrawTime=33;
		
		if( redrawTime > 0 ) {
			m_handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					NexCaptionRenderer.this.invalidate();
				}
			}, /* uptime + */ redrawTime);
		}
	}
}
