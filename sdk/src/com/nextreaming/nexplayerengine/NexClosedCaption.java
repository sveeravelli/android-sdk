package com.nextreaming.nexplayerengine;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.nextreaming.nexlogger.NexLogRecorder.LOG_LEVEL;

import android.R.integer;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

/**
 *  \brief This class handles the subtitles and closed captions data of content.
 *
 * NexPlayer&trade;&nbsp;uses this class to handle SMI, SRT, SUB, and Smooth Streaming subtitles
 * as well as CEA 608 closed captions included in HLS content.
 *
 * CEA 608 closed captions in particular may be implemented in a simple BASIC mode or in the FULL mode
 * that supports the variety of text attributes and display options available in the specification.  This
 * display mode type should be set with the NexPlayer&trade;&nbsp; property, \link NexPlayer.NexProperty.SET_CEA608_TYPE SET_CEA608_TYPE\endlink .
 *
 * The instance of NexClosedCaption is delivered through the
 * \link NexPlayer.IListener.onTextRenderRender() onTextRenderRender\endlink method for both regular subtitles
 * and CEA 608 closed captions supported in the BASIC mode.
 *
 * When CEA 608 closed captions are supported in the FULL mode however, it is necessary to create and display the captions in a
 * separate caption renderer, \link NexCaptionRenderer \endlink .  These captions can be displayed one character at a time and thus the
 * position of each character must be considered.  The vertical position of each character is set by a row number, \c row, between 0 and 15, while
 * the horizontal position of the character is set by a column number, \c col, between 0 and 32.
 *
 *
 * \see NexPlayer.IListener.onTextRenderRender and NexPlayer.NexProperty.SET_CEA608_TYPE for additional details.
 *
 */
public class NexClosedCaption {

    /** This is a possible \c return value for NexClosedCaption.getTextType().  When the text type is unknown, NexPlayer&trade;&nbsp;doesn't
     *  perform any special processing on the text (the same as in the case of general subtitles.)*/
    public static final int TEXT_TYPE_UNKNOWN = 0;          // Same with general type.
    /** This is a possible \c return value for NexClosedCaption.getTextType(). This indicates general text type and requires no special
     *  processing of the text. */
    public static final int TEXT_TYPE_GENERAL = 1;          // No special process is needed.
    /** This is a possible \c return value for \link NexClosedCaption.getTextType()\endlink. This format is not currently supported. */
    public static final int TEXT_TYPE_ATSCMH_CC = 0x11;     // ATSC-M/H CC. caption_channel_packet() -> CEA-708-D.
    /** This is a possible \c return value for \link NexClosedCaption.getTextType()\endlink. This format is not currently supported. */
    public static final int TEXT_TYPE_ATSCMH_BAR = 0x12;    // ATSC-M/H CC. bar_data() -> ATSC A/53: ch6.2.3.2 Bar Data. Table 6.8
    /** This is a possible \c return value for \link NexClosedCaption.getTextType()\endlink.  This format is not currently supported. */
    public static final int TEXT_TYPE_ATSCMH_AFD = 0x13;    // ATSC-M/H CC. afd_data() -> ATSC A/53: ch6.2.4 Active Format Description Data. Table 6.10
    /** This is a possible \c return value for \link NexClosedCaption.getTextType()\endlink.  It indicates the contents include CEA 608 closed captions
     *  on Data Channel 1 and processes the attributes accordingly. */
    public static final int TEXT_TYPE_NTSC_CC_CH1 = 0x14;   // CEA-608. Data Channel 1.
    /** This is a possible \c return value for \link NexClosedCaption.getTextType()\endlink.  It indicates the contents include CEA 608 closed captions
     *  on Data Channel 2 and processes the attributes accordingly. */
    public static final int TEXT_TYPE_NTSC_CC_CH2 = 0x15;   // CEA-608. Data Channel 2.
    /** Thia is a possible \c return value for \link NexClosedCaption.getTextType()\endlink. It indicates the contents including 3GPP Timed Text.*/
    public static final int TEXT_TYPE_3GPP_TIMEDTEXT = 0x20; //3GPP TS 26.245    

    

    // Possible caption background colors for CEA 608 closed captions only.
    // Other subtitle formats don't use these values.
    //** These are possible \c return values for getCaptionColor & getbgColor*/
    private final int White = 0;
    private final int Green = 1;
    private final int Blue = 2;
    private final int Cyan = 3;
    private final int Red = 4;
    private final int Yellow = 5;
    private final int Magenta = 6;
    private final int Black = 7;
    private final int Transparent = 8;

    // Encoding Type of Text
    /** This is a possible \c return value for \link NexClosedCaption.getEncodingType()\endlink. */
    public static final int ENCODING_TYPE_ISO8859_1     = 0x0;
    /** This is a possible \c return value for \link NexClosedCaption.getEncodingType()\endlink. */
    public static final int ENCODING_TYPE_UTF16         = 0x1;
    /** This is a possible \c return value for \link NexClosedCaption.getEncodingType()\endlink. */
    public static final int ENCODING_TYPE_UTF16_BE  = 0x2;
    /** This is a possible \c return value for \link NexClosedCaption.getEncodingType()\endlink. */
    public static final int ENCODING_TYPE_UTF8      = 0x3;
    /** This is a possible \c return value for \link NexClosedCaption.getEncodingType()\endlink. */
    public static final int ENCODING_TYPE_ASCII     = 0x10;
    /** This is a possible \c return value for \link NexClosedCaption.getEncodingType()\endlink. */
    public static final int ENCODING_TYPE_UNICODE   = 0x20;
    /** This is a possible \c return value for \link NexClosedCaption.getEncodingType()\endlink. */
    public static final int ENCODING_TYPE_UNKNOWN   = 0xFFFFFFFF;

    private int     mTextType= 0;       //TextType;
    private int     mIsItalic= 0;       //Italics
    private int     mIsUnderline = 0;
    private int     mCaptionColor = 0;
    private int     mBgColor = 0;
    private int     mIsOpaque = 0;
    private int     mIsEnable = 0;  //Captions ON?
    private byte[]  mTextData = null;

    private int     mRows = 0;
    private int     mIndent = 0;

    private int     mEncodingType   = ENCODING_TYPE_ISO8859_1;

    // This is the support for the FULL display (by character) of CEA 608 closed captions
    private short[] m_attr; //= new short[32*16];
	private short[] m_charcode; //= new short[32*16];
	private CaptionMode m_captionMode;
	private int m_rollUpBaseRow;
	private int m_rollUpNumRows;
	private int m_rollUpAnimationStartTime;
	private int[] m_UpdateTime = new int[4];

	// Character attribute masks
	private static final int CHARATTR_CHARSET_MASK = 0x0007;
	private static final int CHARATTR_LARGE        = 0x0008; // Character is bolded.
	private static final int CHARATTR_FG_MASK      = 0x00F0; // Sets the foreground (text) color of a character.
	private static final int CHARATTR_BG_MASK      = 0x0F00; // Sets the background color of a character.
	private static final int CHARATTR_ITALIC       = 0x1000; // Character is italicized.
	private static final int CHARATTR_UNDERLINE    = 0x2000; // Character is underlined.
	private static final int CHARATTR_FLASH        = 0x4000; // Character is flashing.
	private static final int CHARATTR_DRAW_BG      = 0x8000; // Draw the background; if set to 0, BG_MASK is ignored (ie transparent).

	/**
	 * \brief This enumeration sets how CEA 608 captions will be displayed (in FULL mode only).
	 */
	public enum CaptionMode {
		/** No captions.*/
	    None(0),
	    /** The rows of captions will be displayed and "roll up" the screen.  This may include 2, 3, or 4 rows displayed and "rolling" at once.*/
	    RollUp(1),         // RU2, RU3, RU4 Roll up (number of lines displayed in roll up mode)
	    /** The entire row of the caption will be displayed ("pop on") the screen at once. */
	    PopOn(2),          // RCL, EOC : whole line of text is displayed (pops up on the screen) at once (wait for command to display)
	    /** Each character in the caption will be displayed at a time, as it becomes available.*/
	    PaintOn(3),        // RDC : resume direct captioning:  each character is displayed one at a time.
	    /** Only text will be displayed.  This mode is used for example in emergency broadcast situations.*/
	    Text(4);           // TR, RTD  : TR text restart/resume text display: ONLY text mode ( e.g. in emergency broadcast situations)

		private int m_value;

		private CaptionMode( int value ) {
			m_value = value;
		}

		/**
		 * \brief This gets the integer value of the CaptionMode enumeration.
		 *
		 * \returns  The integer value of the CaptionMode to be used.  See the CaptionMode enumeration for more details.
		 */
		public int getValue() {
			return m_value;
		}

		/**
		 * \brief This gets the mode that captions should be displayed based on the integer value of the enumeration.
		 *
		 * \returns  The CaptionMode to be used to display CEA 608 closed captions in FULL mode.  Please see the
		 *           enumeration for details on the different modes.
		 */
		public static CaptionMode fromValue( int value ) {
			for( CaptionMode item : values() ) {
				if( item.getValue() == value )
					return item;
			}
			return null;
		}
	}
	/**
	 * \brief This enumerator defines the encoding character set to be used for CEA 608 closed captions (in FULL mode only).
	 */
	public enum Charset {
		/** Unicode characters, include special characters for French and Spanish accents.*/
		UNICODE_UCS2(0), // includes special characters (french/spanish accents)
		/** Not currently used but reserved for future use.  Can be ignored.*/
		PRIVATE_1(1), // can be ignored
		/** Not currently used but reserved for future use.  Can be ignored.*/
		PRIVATE_2(2), // can be ignored, currently not used but reserved for future use
		/** Korean characters encoding.*/
		KSC_5601_1987(3), // Korean characters
		/** Chinese characters encoding.*/
		GB_2312_80(4); // Chinese characters

		private int m_value;

		private Charset( int value ) {
			m_value = value;
		}

		/**
		 * \brief  This gets the integer value code for the Charset enumeration.
		 *
		 * \returns The integer value of the Charset enumeration.  See the enumeration for possible values.
		 */
		public int getValue() {
			return m_value;
		}
		/**
		 * \brief  This gets the character set to be used for encoding based on the integer value.
		 *
		 * \returns  The character set to be used for encoding.  See the Charset enumeration for the possible character encoding sets.
		 *
		 */
		public static Charset fromValue( int value ) {
			for( Charset item : values() ) {
				if( item.getValue() == value )
					return item;
			}
			return null;
		}
	}

	/**
     * \brief   This enumeration sets the text display and background colors of CEA 608 closed captions.  (FULL mode)
     *
     * Each color option has an ARGB hexacode associated with the foreground color or background color of the caption to
     * be displayed, as well as a unique value to be used to identify which color is to be selected.
	 */

	public enum CaptionColor {
		/** This sets the CEA 608 closed captions text or background color white in the FULL mode. */
		WHITE             (0x00, 0xFFFFFFFF, 0xFFEEEEEE),
		/** This sets the CEA 608 closed captions text or background color semi-transparent white in the FULL mode. */
		WHITE_SEMITRANS   (0x01, 0xFFFFFFFF, 0x77FFFFFF),
		/** This sets the CEA 608 closed captions text or background color green in the FULL mode. */
		GREEN             (0x02, 0xFF00FF00, 0xFF007700),
		/** This sets the CEA 608 closed captions text or background color semi-transparent green in the FULL mode. */
		GREEN_SEMITRANS   (0x03, 0xFF00FF00, 0x7700FF00),
		/** This sets the CEA 608 closed captions text or background color blue in the FULL mode. */
		BLUE              (0x04, 0xFF0000FF, 0xFF000077),
		/** This sets the CEA 608 closed captions text or background color semi-transparent blue in the FULL mode. */
		BLUE_SEMITRANS    (0x05, 0xFF0000FF, 0x770000FF),
		/** This sets the CEA 608 closed captions text or background color cyan in the FULL mode. */
		CYAN              (0x06, 0xFF00FFFF, 0xFF007777),
		/** This sets the CEA 608 closed captions text or background color semi-transparent cyan in the FULL mode. */
		CYAN_SEMITRANS    (0x07, 0xFF00FFFF, 0x7700FFFF),
		/** This sets the CEA 608 closed captions text or background color red in the FULL mode. */
		RED               (0x08, 0xFFFF0000, 0xFF770000),
		/** This sets the CEA 608 closed captions text or background color semi-transparent red in the FULL mode. */
		RED_SEMITRANS     (0x09, 0xFFFF0000, 0x77FF0000),
		/** This sets the CEA 608 closed captions text or background color yellow in the FULL mode. */
		YELLOW            (0x0A, 0xFFFFFF00, 0xFF777700),
		/** This sets the CEA 608 closed captions text or background color semi-transparent yellow in the FULL mode. */
		YELLOW_SEMITRANS  (0x0B, 0xFFFFFF00, 0x77FFFF00),
		/** This sets the CEA 608 closed captions text or background color magenta in the FULL mode. */
		MAGENTA           (0x0C, 0xFFFF00FF, 0xFF770077),
		/** This sets the CEA 608 closed captions text or background color semi-transparent magenta in the FULL mode. */
		MAGENTA_SEMITRANS (0x0D, 0xFFFF00FF, 0x77FF00FF),
		/** This sets the CEA 608 closed captions text or background color black in the FULL mode. */
		BLACK             (0x0E, 0xFF000000, 0xFF000000),
		/** This sets the CEA 608 closed captions text or background color semi-transparent black in the FULL mode. */
		BLACK_SEMITRANS   (0x0F, 0xFF000000, 0x77000000),
		/** This sets the CEA 608 closed captions text or background color transparent (no color) in the FULL mode. */
		TRANSPARENT		  (0xFF, 0x00000000, 0x00000000);

		private int m_value;
		private int m_fg;
		private int m_bg;

		private CaptionColor( int value, int fg, int bg ) {
			m_value = value;
			m_fg = fg;
			m_bg = bg;
		}

		/**
		 * \brief  This gets the integer value of the CaptionColor enumerator, to be used with CEA 608 closed captions in FULL mode.
		 *
		 * \returns  The integer value of the color to be used.  This will be one of:
		 *             - <b>0x00</b>:  White
		 *             - <b>0x01</b>:  Semi-transparent white
		 *             - <b>0x02</b>:  Green
		 *             - <b>0x03</b>:  Semi-transparent green
		 *             - <b>0x04</b>:  Blue
		 *             - <b>0x05</b>:  Semi-transparent blue
		 *             - <b>0x06</b>:  Cyan
		 *             - <b>0x07</b>:  Semi-transparent cyan
		 *             - <b>0x08</b>:  Red
		 *             - <b>0x09</b>:  Semi-transparent red
		 *             - <b>0x0A</b>:  Yellow
		 *             - <b>0x0B</b>:  Semi-transparent yellow
		 *             - <b>0x0C</b>:  Magenta
		 *             - <b>0x0D</b>:  Semi-transparent magenta
		 *             - <b>0x0E</b>:  Black
		 *             - <b>0x0F</b>:  Semi-transparent black
		 *             - <b>0xFF</b>:  Transparent
		 */
		public int getValue() {
			return m_value;
		}

		/**
		 * \brief This gets the caption color to be used with CEA 608 closed captions in FULL mode from the integer value.
		 *
		 * \param value  An integer value indicating the caption color to be selected.  This is a value between 0x00 and 0xFF.
		 *
		 * \returns The caption color to be used.
		 *
		 * \see The enumerator {@link CaptionColor} for more details on the possible captions and color values to be used.
		 */
		public static CaptionColor fromValue( int value ) {
			for( CaptionColor item : values() ) {
				if( item.getValue() == value )
					return item;
			}
			return null;
		}

		/**
		 * \brief This gets the text or foreground color of the character to be displayed with CEA 608 closed captions in FULL mode.
		 *
		 * \returns  The text color to be displayed as an ARGB hexacode.  Please see the enumerator {@link CaptionColor} for the
		 *           possible hexacodes and their associated colors.
		 */
		public int getFGColor() {
			return m_fg;
		}

		/**
		 * \brief This gets the background color of the character to be displayed with CEA 608 closed captions in FULL mode.
		 *
		 * \returns  The background color to be displayed as an ARGB hexacode.  Please see the enumerator {@link CaptionColor} for the
		 *           possible hexacodes and their associated colors.
		 */
		public int getBGColor() {
			return m_bg;
		}

	}

	private static final int CHARSET_UNICODE_UCS2 = 0;

	/**
	 * In FULL mode, when CEA 608 closed captions are to be displayed and "rolled up" with animation,
	 * this defines the row that is being "rolled out" of the display during the animation, in other words
	 * the row that is disappearing as the next, new row of captions appears.
	 *
	 * This can be ignored if animation of the display is not being used.
	 */
	public static final int ROLLED_OUT_ROW = -1;

	private NexClosedCaption(
			short[] attr,
			short[] charcode,
			int mode,
			int rollUpBaseRow,
			int rollUpNumRows,
			int rollUpAnimationStartTime,
			int updateTimeCH1,
			int updateTimeCH2,
			int updateTimeCH3,
			int updateTimeCH4) {
		m_attr = attr;
		m_charcode = charcode;
		m_captionMode = CaptionMode.fromValue(mode);
		m_rollUpBaseRow = rollUpBaseRow;
		m_rollUpNumRows = rollUpNumRows;
		m_rollUpAnimationStartTime = rollUpAnimationStartTime;
		mTextType = TEXT_TYPE_NTSC_CC_CH1;
		m_UpdateTime[0] = updateTimeCH1;
		m_UpdateTime[1] = updateTimeCH2;
		m_UpdateTime[2] = updateTimeCH3;
		m_UpdateTime[3] = updateTimeCH4;
	}

	/**
	 * \brief This method gets the base row of the roll up rows (will be a number from 0 to 15)
	 */
	public int getRollUpBaseRow() {
		return m_rollUpBaseRow;
	}
	/**
	 * \brief This gets the number of roll-up rows to be displayed when CEA 608 closed captions are to be displayed "rolling up" in FULL mode.
	 *
	 * \returns the number of roll up rows to be displayed:  2, 3, or 4.
	 */
	public int getRollUpNumRows() {
		return m_rollUpNumRows;
	}
	/**
	 * \brief This gets the time to be taken to "roll up" between rows or the animation time for CEA 608 closed captions in FULL mode.
	 *
	 * \returns The time in milliseconds it takes for a row to "roll up" to the next row, as a long.
	 */
	public long getRollUpElapsedTime() {
		return System.currentTimeMillis()%0xFFFFFFFFL - (((long)m_rollUpAnimationStartTime)&0xFFFFFFFFL);
	}

	/**
	 * \brief This gets the time at which the CEA 608 closed captions in the given channel were last updated.
	 *
	 * This function allows NexPlayer&trade;&nbsp;to determine if caption information is available on the
	 * given channel, and thus determine which CEA 608 channels are available for the playing content, using
	 * the method \c getAvailableCaptionChannel.
	 *
	 * \param channelNumber  The CEA 608 channel in which to get the time at which the captions were last updated.
	 *                       This will be 1, 2, 3, or 4.
	 *
	 * \returns  The time at which the caption content was last updated for the channel passed, in milliseconds (ms).
	 *
	 * \see getAvailableCaptionChannel
	 *
	 * \since version 5.11
	 */
	public long getCaptionUpdateTime(int channelNumber)
	{
		return ((long)(m_UpdateTime[channelNumber]) & 0xFFFFFFFFL);
	}

	/**
	 * \brief This method checks if CEA 608 closed caption information is available for the given channel.
	 *
	 * By checking how much time has passed since the channel information was updated (with the method \c getCaptionUpdateTime), this method can
	 * determine if there is any closed caption information available on this channel to be displayed.
	 * This allows the player to present the available CEA 608 channels to the user in order for the desired
	 * channel to be selected.
	 *
	 * Different channels often provide different closed caption information, for example in different languages,
	 * but the four specified channels may not always be used or available for any particular content.
	 *
	 * \param channelNumber  The CEA 608 channel number to check for closed caption information availability.
	 *                       This will be 1, 2, 3, or 4.
	 *
	 * \returns  \c TRUE if the channel has available information, \c FALSE if no recent closed caption information is available
	 *           on the given channel.
	 *
	 * \see getCaptionUpdateTime
	 *
	 * \since version 5.11
	 */
	public boolean getAvailableCaptionChannel(int channelNumber)
	{
		if(System.currentTimeMillis()%0xFFFFFFFFL - (((long)m_UpdateTime[channelNumber])&0xFFFFFFFFL) < 60000)
		{
			return true;
		}
		else
			return false;
	}

	/**
	 * \brief This gets the display mode for the CEA 608 closed captions when supported in FULL mode
	 *
	 * \returns  The CaptionMode of how the captions should be displayed.  This will be one of:
	 *             - CaptionMode.None
	 *             - CaptionMode.RollUp
	 *             - CaptionMode.PopOn
	 *             - CaptionMode.PaintOn
	 *             - CaptionMode.Text
	 *
	 * \see The CaptionMode enumeration for more details on the ways in which captions can be displayed. displaytext/rollup/paint on modes etc
	 */

	public CaptionMode getCaptionMode() {
		return m_captionMode;
	}
	/**
	 * \brief  This determines if the character in CEA 608 closed captions is to be displayed in italics. (FULL mode)
	 *
	 * \param row  The row or vertical position of the character to be displayed.
	 * \param col  The column or horizontal position of the character to be displayed.
	 *
	 * \returns TRUE if italicized, FALSE if not.
	 */
	public boolean isItalic( int row, int col ) {
		if( (row<0 || row>14) && row != ROLLED_OUT_ROW ) {
			return false;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		return (m_attr[ row*32 + col ] & CHARATTR_ITALIC)!=0 ? true : false;
	}

	/**
	 * \brief  This determines if the character in CEA 608 closed captions is to be underlined when displayed (FULL mode).
	 *
	 * \param row  The row or vertical position of the character to be displayed.
	 * \param col  The column or horizontal position of the character to be displayed.
	 *
	 * \returns TRUE if underlined, FALSE if not.
	 *
	 */
	public boolean isUnderline( int row, int col ) {
		if( (row<0 || row>14) && row != ROLLED_OUT_ROW ) {
			return false;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		return (m_attr[ row*32 + col ] & CHARATTR_UNDERLINE)!=0 ? true : false;
	}

	/**
	 * This determines if the character in CEA 608 closed captions is to be displayed flashing (FULL mode).
	 *
	 * \param row  The row or vertical position of the character to be displayed.
	 * \param col  The column or horizontal position of the character to be displayed.
	 *
	 * \returns TRUE if flashing, FALSE if not.
	 */
	public boolean isFlashing( int row, int col ) {
		if( (row<0 || row>14) && row != ROLLED_OUT_ROW ) {
			return false;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		return (m_attr[ row*32 + col ] & CHARATTR_FLASH)!=0 ? true : false;
	}

	/**
	 * \brief This determines if the character in CEA 608 closed captions is to be displayed in BOLD (FULL mode).
	 *
	 * \param row  The row or vertical position of the character to be displayed.
	 * \param col  The column or horizontal position of the character to be displayed.
	 *
	 * \returns TRUE if <b>bold</b>, FALSE if not.
	 */
	public boolean isLarge( int row, int col ) {
		if( (row<0 || row>14) && row != ROLLED_OUT_ROW ) {
			return false;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		return (m_attr[ row*32 + col ] & CHARATTR_LARGE)!=0 ? true : false;
	}

	/**
	 * \brief  This determines if the background of the character in CEA 608 closed captions is to be displayed or not (FULL mode).
	 *
	 * If the background is not to be displayed, it will be transparent and the background color value of the caption will be ignored.
	 * \param row  The row or vertical position of the character to be displayed.
	 * \param col  The column or horizontal position of the character to be displayed.
	 *
	 * \returns TRUE if background color is to be displayed, FALSE if not.
	 */
	public boolean isDrawBackground( int row, int col ) {
		if( (row<0 || row>14) && row != ROLLED_OUT_ROW ) {
			return false;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		return (m_attr[ row*32 + col ] & CHARATTR_DRAW_BG)!=0 ? true : false;
	}
	/**
	 * \brief This determines the color to display the character in CEA 608 closed captions (FULL mode).
	 *
	 * \param row  The row or vertical position of the character to be displayed.
	 * \param col  The column or horizontal position of the character to be displayed.
	 *
	 * \returns TRUE if <i>italicized</i>, FALSE if not.
	 */
	public CaptionColor getFGColor( int row, int col ) {
		if( (row<0 || row>14) && row != ROLLED_OUT_ROW ) {
			return null;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		return CaptionColor.fromValue((m_attr[ row*32 + col ] >> 4) & 0xF);
	}

	/**
	 * \brief This determines the background color to display behind the character in CEA 608 closed captions (FULL mode).
	 *
	 * \param row  The row or vertical position of the character to be displayed.
	 * \param col  The column or horizontal position of the character to be displayed.
	 *
	 * \returns The background color to display.
	 *
	 * \see {@link CaptionColor} for color options available for CEA 608 closed captions in FULL mode.
	 */
	public CaptionColor getBGColor( int row, int col ) {
		if( (row<0 || row>14) && row != ROLLED_OUT_ROW ) {
			return null;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		if( (m_attr[ row*32 + col ] & CHARATTR_DRAW_BG) == 0 ) {
			return CaptionColor.TRANSPARENT;
		}
		return CaptionColor.fromValue((m_attr[ row*32 + col ] >> 8) & 0xF);
	}

	/**
	 * \brief This gets the encoding set for the character in CEA 608 closed captions
	 *
	 * \param row  The row position of the character to be displayed, as an integer.
	 * \param col  The column position of the character to be displayed, as an integer.
	 *
	 * \returns  The character encoding set to be used.  This will be one of:
	 *               - UNICODE_UCS2(0) : Unicode characters, including special characters for French and Spanish accents.
	 *               - PRIVATE_1(1): Not currently used but reserved for future use.  Can be ignored.
	 *               - PRIVATE_2(2): Not currently used but reserved for future use.  Can be ignored.
	 *               - KSC_5601_1987(3):  Korean characters encoding.
	 *               - GB_2312_80(4):  Chinese characters encoding.
	 */
	public Charset getCharset( int row, int col ) {
		if( (row<0 || row>14) && row != ROLLED_OUT_ROW ) {
			return null;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		return Charset.fromValue((m_attr[ row*32 + col ]) & 0x7);
	}

	/**
	 * \brief This gets the character to display in CEA 608 closed captions (FULL mode).
	 *
	 * \param row  The row position of the character to be displayed, as an integer.
	 * \param col  The column position of the character to be displayed, as an integer.
	 *
	 * \returns  The character to be displayed.
	 *
	 */
	public char getCharCode( int row, int col ) {
		if( (row<0 || row>14) && row != ROLLED_OUT_ROW ) {
			return 0;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		return (char)m_charcode[ row*32 + col ];
	}
	/**
	 * \brief This clears displayed CEA 608 closed captions in FULL mode, displaying a blank line instead.
	 */
	public void makeBlankData()
	{
		m_attr = new short[32*16];
		m_charcode = new short[32*16];
		for(int i=0;i<32*16;i++)
		{
			m_attr[i] = 0x0E00;
		}
	}

	/*
	 * END
	 * */

    private NexClosedCaption(int textType, int encodingType, byte[] text)
    {
        if(text == null)
        {
            Log.d("NexClosedCaption", "NexClosedCaption text is null!!");
        }
        mTextType = getTextType(textType);
        mEncodingType = getEncodingType(encodingType);
        mTextData = text;
    }

    private NexClosedCaption(
            int textType,
            int encodingType,
            int isItalic,
            int isUnderline,
            int captionColor,
            int bgColor,
            int isOpaque,
            int isEnable,
            int rows,
            int indent,
            byte[] TextData)
    {
        if(TextData == null)
        {
            Log.d("NexClosedCaption", "ID3TagText text is null!!");
        }

        mTextType = getTextType(textType);
        mEncodingType = getEncodingType(encodingType);

        switch(captionColor)
        {
            case White:
                mCaptionColor = 0xFFFFFFFF;
                break;
            case Green:
                mCaptionColor = 0xFF00FF00;
                break;
            case Blue:
                mCaptionColor = 0xFF0000FF;
                break;
            case Cyan:
                mCaptionColor = 0xFF00FFFF;
                break;
            case Red:
                mCaptionColor = 0xFFFF0000;
                break;
            case Yellow:
                mCaptionColor = 0xFFFFFF00;
                break;
            case Magenta:
                mCaptionColor = 0xFFFF00FF;
                break;
            case Black:
                mCaptionColor = 0xFF000000;
                break;
            case Transparent:
                mCaptionColor = 0x00000000;
                break;
            default:
                mCaptionColor = 0xFFFFFFFF;
                break;
        }

        switch(bgColor)
        {
            case White:
                mBgColor = 0xFFFFFFFF;
                break;
            case Green:
                mBgColor = 0xFF00FF00;
                break;
            case Blue:
                mBgColor = 0xFF0000FF;
                break;
            case Cyan:
                mBgColor = 0xFF00FFFF;
                break;
            case Red:
                mBgColor = 0xFFFF0000;
                break;
            case Yellow:
                mBgColor = 0xFFFFFF00;
                break;
            case Magenta:
                mBgColor = 0xFFFF00FF;
                break;
            case Black:
                mBgColor = 0xFF000000;
                break;
            case Transparent:
                mBgColor = 0x00000000;
                break;
            default:
                mBgColor = 0xFF000000;
                break;
        }

        mIsItalic       = isItalic;
        mIsUnderline    = isUnderline;
        mIsOpaque       = isOpaque;
        mIsEnable       = isEnable;

        mRows           = rows;
        mIndent         = indent;

        mTextData = TextData;
    }


    private int getTextType(int textType)
    {
        switch(textType)
        {
        case TEXT_TYPE_UNKNOWN:
        case TEXT_TYPE_GENERAL:
        case TEXT_TYPE_ATSCMH_CC:
        case TEXT_TYPE_ATSCMH_BAR:
        case TEXT_TYPE_ATSCMH_AFD:
        case TEXT_TYPE_NTSC_CC_CH1:
        case TEXT_TYPE_NTSC_CC_CH2:
            return textType;
        default:
            return TEXT_TYPE_UNKNOWN;
        }
    }

    private int getEncodingType(int encodingType)
    {
        switch(encodingType)
        {
        case ENCODING_TYPE_ISO8859_1:
        case ENCODING_TYPE_UTF16:
        case ENCODING_TYPE_UTF16_BE:
        case ENCODING_TYPE_UTF8:
        case ENCODING_TYPE_ASCII:
        case ENCODING_TYPE_UNICODE:
            return encodingType;
        default:
            return ENCODING_TYPE_UNKNOWN;
        }
    }



    /**
     * \brief   This method determines the type of text captions used by the content.
     *
     *  Most subtitles will be displayed without further processing but CEA 608 closed captions can include
     *  additional text attributes.
     *
     * \returns The type of text to be displayed.  This will be one of:
     *          - <b>TEXT_TYPE_UNKNOWN</b> The type of text is unknown. The text is treated like a general text file.
     *          - <b>TEXT_TYPE_GENERAL</b>  This is text only and requires no additional processing.
     *          - <b>TEXT_TYPE_ATSCMH_CC</b> Not a format currently supported.
     *          - <b>TEXT_TYPE_ATSCMH_BAR</b> The text includes text bar data.  Not currently supported.
     *          - <b>TEXT_TYPE_ATSCMH_AFD</b> The text includes Active Format Description data.  Not currently supported.
     *          - <b>TEXT_TYPE_NTSC_CC_CH1</b> The text is CEA 608 closed captions on Data Channel 1.
     *          - <b>TEXT_TYPE_NTSC_CC_CH2</b> The text is CEA 608 closed captions on Data Channel 2.
     */
    public int getTextType()
    {
        return mTextType;
    }

    /**
     * \brief   This method returns the text display color of CEA 608 closed captions.
     *
     * \returns  The color of the displayed caption text as an ARGB hexacode.
     * 	         This will be one of:
     *              - <b>White = 0xFFFFFFFF </b> (default)
     *              - <b>Green  = 0xFF00FF00 </b>
     *              - <b>Blue = 0xFF0000FF </b>
     *              - <b>Cyan = 0xFF00FFFF </b>
     *              - <b>Red = 0xFFFF0000 </b>
     *              - <b>Yellow = 0xFFFFFF00 </b>
     *              - <b>Magenta = 0xFFFF00FF </b>
     *              - <b>Black = 0xFF000000 </b>
     *              - <b>Transparent = 0x00000000 </b>
     *
     *
     */
    public int getCaptionColor()
    {
        return mCaptionColor;
    }

    /**
     * \brief   This method returns the background color of CEA 608 closed captions for the Basic display type.
     *
     * \returns  The background color of the displayed caption text as an ARGB hexacode.
     * 	         This will be one of:
     *              - <b>White = 0xFFFFFFFF </b>
     *              - <b>Green  = 0xFF00FF00 </b>
     *              - <b>Blue = 0xFF0000FF </b>
     *              - <b>Cyan = 0xFF00FFFF </b>
     *              - <b>Red = 0xFFFF0000 </b>
     *              - <b>Yellow = 0xFFFFFF00 </b>
     *              - <b>Magenta = 0xFFFF00FF </b>
     *              - <b>Black = 0xFF000000 </b> (default)
     *              - <b>Transparent = 0x00000000 </b>
     *
     */
    public int getBGColor()
    {
        return mBgColor;
    }

    /**
     * \brief   This method determines CEA 608 closed captions <i>italics</i>.
     *
     * \returns  Zero if the text is displayed normally; 1 if the text should be displayed in <i>italics</i>.
     *
     */
    public int isItalic()
    {
        return mIsItalic;
    }

    /**
     * \brief   This method determines whether CEA 608 closed captions are underlined.
     *
     * \returns Zero if the text is displayed normally; 1 if the text should be underlined.
     *
     */
    public int isUnderline()
    {
        return mIsUnderline;
    }

    /**
     * \brief   This method determines the opacity of CEA 608 closed captions background color.
     *
     * \returns  Always 1 (opaque) for CEA 608 closed captions.
     *
     */
    public int isOpaque()
    {
        return mIsOpaque;
    }
    // TODO : [CLAIRE] Not used yet. only for CEA 608
    /** \brief  This method controls whether CEA 608 closed captions are enabled or disabled.
     *
     * \returns  Always 0.
     */
    public int isEnable()
    {
        return mIsEnable;
    }

    /** \brief This method returns the text's vertical position for CEA 608 closed captions only.
     *
     *  CEA 608 closed captions are positioned vertically based on this value.
     *  There are 15 possible rows in which the captions can be displayed, 1 being at the top of the screen and 15 at
     *  the bottom.
     *
     *  \returns The vertical position at which to display the caption. This will be an integer value from 1 to 15.
     */
    public int getRows()
    {
        return mRows;
    }

    /** \brief This method returns the text's horizontal position for CEA 608 closed captions only.
     *
     *  CEA 608 closed captions will be indented horizontally based on this value.
     *  There are 8 possible horizontal column positions, 1 being at the far left of the screen and 8 indented to
     *  the far right.
     *
     *  \returns The horizontal position at which to display the caption. This will be an integer value from 1 to 8.
     */
    public int getIndent()
    {
        return mIndent;
    }


    /**
     * \brief This determines the encoding type of the content's captions or subtitles, when available.
     *
     * \returns The encoding type of the subtitles or captions.  This will be one of the following values:
     *              - <b>ENCODING_TYPE_ISO8859_1    (0x0)</b>
     *              - <b>ENCODING_TYPE_UTF16        (0x1)</b>
     *              - <b>ENCODING_TYPE_UTF16_BE     (0x2)</b>
     *              - <b>ENCODING_TYPE_UTF8         (0x3)</b>
     *              - <b>ENCODING_TYPE_ASCII        (0x10)</b>
     *              - <b>ENCODING_TYPE_UNICODE      (0x20)</b>
     *              - <b>ENCODING_TYPE_UNKNOWN  (0xFFFFFFFF)</b>
     */
    public int getEncodingType()
    {
        return mEncodingType;
    }

    /**
     * \brief This method gets the text data of the content.
     *
     * \returns A byte array of the text data.
     */
    public byte[] getTextData()
    {
        return mTextData;
    }

    /**
     * 3GPP Timed Text data structure.
     */
    
    private final int SampleModifier_TEXTSTYLE = 0;
    private final int SampleModifier_TEXTHIGHLIGHT = 1;
    private final int SampleModifier_TEXTHILIGHTCOLOR = 2;
    private final int SampleModifier_TEXTKARAOKE = 3;
    private final int SampleModifier_TEXTSCROLLDELAY = 4;
    private final int SampleModifier_TEXTHYPERTEXT = 5;
    private final int SampleModifier_TEXTTEXTBOX = 6;
    private final int SampleModifier_TEXTBLINK = 7;
    private final int SampleModifier_TEXTTEXTWRAP = 8;
    
    private final int SampleModifier_VerticalJustification = 10;
    private final int SampleModifier_HorizontalJustification = 11;
    private final int SampleModifier_ScrollIN = 12;
    private final int SampleModifier_ScrollOUT = 13;
    private final int SampleModifier_ScrollDirection = 14;
    private final int SampleModifier_ContinuousKaraoke = 15;
    private final int SampleModifier_WriteVertically = 16;
    private final int SampleModifier_FillTextRegion = 17;
    
    
    private byte[] m_3gppTT_TextBuffer;
    private int m_3gppTTRegionTX = 0;
    private int m_3gppTTRegionTY = 0;
    private int m_3gppTTRegionWidth = 0;
    private int m_3gppTTRegionHeight = 0;
    private int m_3gppTTTextColor = 0xFF000000;//default : BLACK.
    private int m_3gppTTBGColor = 0;
    
    private int m_VerticalJustification = 0;//0 : top, 1: center, -1 : bottom
    private int m_HorizontalJustification = 0;//0:left, 1: center, -1 : right
    
    private boolean isScrollIn = false;
    private boolean isScrollOut = false;
    private int isScrollDirection = 0;
    private boolean isContinuousKaraoke = false;
    private boolean isWriteVertically = false;
    private boolean isFillTextRegion = false;
    
    private int m_startTime = 0;//these values used for scroll delay. 
    private int m_endTime = 0;
    
    /**
	 * \brief Both the sample format and the sample description contain style records, so it is define here for campactness.
	 * 
	 * 
	 */
    public static class TextStyleEntry
    {	
    	private short c_startChar = 0;
    	private short c_endChar = 0;
    	private short c_fontID = 0;
    	private short c_fontSize = 0;
    	private int c_textColor = 0;
    	private boolean c_isBold = false;
    	private boolean c_isItalic = false;
    	private boolean c_isUnderline = false;
    	
    	private TextStyleEntry(short startChar, 
    					short endChar,
    					short fontID,
    					short fontSize,
    					int textColor,
    					int isBold,
    					int isItalic,
    					int isUnderline)
    	{
    		c_startChar = startChar;
        	c_endChar = endChar;
        	c_fontID = fontID;
        	c_fontSize = fontSize;
        	c_textColor = textColor;
        	if(isBold > 0)
        		c_isBold = true;
        	if(isItalic < 0)
        		c_isItalic = true;
        	if(isUnderline > 0)
        		c_isUnderline = true;
    	}
    	
    	/**
    	 * \brief This property specifies the starting position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getStartChar()
    	{
    		return c_startChar;
    	}
    	
    	/**
    	 * \brief This property specifies the ending position of the character.
    	 * 
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getEndChar()
    	{
    		return c_endChar;
    	}
    	
    	/**
    	 * \brief This property specifies the font type that will be used for this style.
    	 * 
    	 * \returns The font table index. 
    	 */
    	public short getFontID()
    	{
    		return c_fontID;
    	}
    	
    	/**
    	 * \brief This property specifies the font size that will be used for this style.
    	 * 
    	 * \returns The pixel of the font. 
    	 */
    	public short getFontSize()
    	{
    		return c_fontSize;
    	}
    	/**
    	 * \brief This property specifies the font color that will be used for this style.
    	 * 
    	 * \returns The color of the font. 
    	 */
    	public int getFontColor()
    	{
    		return c_textColor;
    	}
    	/**
    	 * \brief This property specifies the Bold font type that will be used for this style.
    	 * 
    	 * \returns TRUE if <b>bold</b>, FALSE if not.
    	 */
    	public boolean getBold()
    	{
    		return c_isBold;
    	}
    	/**
    	 * \brief This property specifies the Italic font type that will be used for this style.
    	 * 
    	 * \returns TRUE if <b>italic</b>, FALSE if not.
    	 */
    	public boolean getItalic()
    	{
    		return c_isItalic;
    	}
    	/**
    	 * \brief This property specifies the Underline font type that will be used for this style.
    	 * 
    	 * \returns TRUE if <b>underline</b>, FALSE if not.
    	 */
    	public boolean getUnderline()
    	{
    		return c_isUnderline;
    	}
    }
    
    /**
	 * \brief This property specifies the style of the text and sets the TextStyleEntry.
	 */
    public class TextStyle{
    	private int totalEntry = 0;
    	private TextStyleEntry[] c_entry;
    	private int m_index = 0;
    	private TextStyle(int totalCount){
    		totalEntry = totalCount;
    		c_entry = new TextStyleEntry[totalCount];
    	}
    	/**
    	 * \brief Adds the TextStyleEntry to this specific class.
    	 * 
    	 * \param entry The TextStyleEntry for TextStyle class. 
    	 */
    	public void setTextStyleEntry(TextStyleEntry entry)
    	{
    		if(m_index >= totalEntry)
    		{
    			return;
    		}
    		c_entry[m_index] = entry;
    		m_index++;
    	}
    	
    	/**
    	 * \brief This property specifies the number of the TextStyleEntry.
    	 * 
    	 * \returns The total number of the  TextStyleEntry.
    	 */
    	public int getCount()
    	{
    		return totalEntry;
    	}
    	/**
    	 * \brief This property gets specific TextStyleEntry.
    	 * 
    	 * \param index The index of the specific TextStyleEntry.
    	 * 
    	 * \returns The specific TextStyleEntry.
    	 * 
    	 */
    	public TextStyleEntry getStyleEntry(int index)
    	{
    		if(index >= totalEntry)
    		{
    			return null;
    		}
    		return c_entry[index];
    	}
		/**
		 * \brief This property specifies the current number of the TextStyleEntry.
		 * 
		 * \returns The current TextStyleEntry index.
		 * 
		 */
    	public int getCurrentCount()
    	{
    		return m_index;
    	}
    }
    
    /**
	 * \brief This property specifies the position of the highlighted text.
	 */
    public class TextHighlight
    {
    	private short c_startChar = 0;
    	private short c_endChar = 0;
    	
    	private TextHighlight(short startChar, short endChar)
    	{
    		c_startChar = startChar;
    		c_endChar = endChar;
    	}
    	/**
    	 * \brief This property specifies the starting position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getStartChar()
    	{
    		return c_startChar;
    	}
    	/**
    	 * \brief This property specifies the ending position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getEndChar()
    	{
    		return c_endChar;
    	}
    }
    
        /**
  	    * \brief This property specifies the color of the highlighted text.
  	    */
    public class TextHighlightColor
    {
    	private int c_highlightcolor = 0;
    	
    	private TextHighlightColor(int col){
    		c_highlightcolor = col;
    	}
    	/**
    	 * \brief This property specifies the color of the highlighted character.
    	 *
    	 * \returns The color of highlighted character. 
    	 */
    	public int getHighlightColor()
    	{
    		return c_highlightcolor;
    	}
    }
    
    /**
  	 * \brief This property specifies the karaoke type highlighted text.
  	 */
    public static class TextKaraokeEntry
    {
    	private int c_highlight_end_time = 0;
    	private short c_startcharoffset = 0;
    	private short c_endcharoffset = 0;
    	
    	private TextKaraokeEntry(int highlight_end_time, 
    							short startcharoffset, 
    							short endcharoffset)
    	{
    		c_highlight_end_time = highlight_end_time;
    		c_startcharoffset = startcharoffset;
    		c_endcharoffset = endcharoffset;
    	}
    	
    	 /**
    	 * \brief This property specifies the end time of the sequence of the karaoke type highlighted text.
    	 */
    	public int getHighlightEndTime()
    	{
    		return c_highlight_end_time;
    	}
    	/**
    	 * \brief This property specifies the starting position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getStartCharOffset()
    	{
    		return c_startcharoffset;
    	}
    	/**
    	 * \brief This property specifies the ending position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getEndCharOffset()
    	{
    		return c_endcharoffset;
    	}
    }
    
    /**
  	 * \brief This property specifies the number of highlighted text.
  	 */
    public class TextKaraoke
    {
    	private int c_startTime = 0;
    	private TextKaraokeEntry[] c_entry = null;
    	private int totalEntry = 0;
    	private int m_index = 0;
    	
    	private TextKaraoke(int startTime, int count){
    		totalEntry = count;
    		c_startTime = startTime;
    		c_entry = new TextKaraokeEntry[count];
    	}
    	/**
    	 * \brief Adds the TextKaraokeEntry to this specific class.
    	 * 
    	 * \param entry The TextKaraokeEntry for TextKaraoke class. 
    	 */
    	public void setKaraokeEntry(TextKaraokeEntry entry)
    	{
    		if(m_index >= totalEntry)
    		{
    			return;
    		}
    		c_entry[m_index] = entry;
    		m_index++;
    	}
    	
    	/**
    	 * \brief This property specifies the starting time of the whole KaraokeEntry.
    	 *
    	 * \returns The start time of the whole KaraokeEntry. 
    	 */
    	public int getStartTime()
    	{
    		return c_startTime;
    	}
    	/**
    	 * \brief This property specifies the number of the TextKaraokeEntry.
    	 * 
    	 * \returns The total number of the TexKaraokeEntry.
    	 */
    	
    	public int getCount()
    	{
    		return totalEntry;
    	}
    	/**
    	 * \brief This property gets specific TextKaraokeEntry.
    	 * 
    	 * \param index The index of the specific TextKaraokeEntry.
    	 * 
    	 * \returns The specific TextKaraokeEntry.
    	 * 
    	 */
    	public TextKaraokeEntry getKaraokeEntry(int index)
    	{
    		return c_entry[index];
    	}
		/**
		 * \brief This property specifies the current number of the TextKaraokeEntry.
		 * 
		 * \returns The current TextKaraokeEntry index.
		 * 
		 */
    	public int getCurrentCount()
    	{
    		return m_index;
    	}
    }
    
    /**
  	 * \brief This property specifies the delaying time of the scrolling text.
  	 */
    public class TextScrollDelay
    {
    	private int c_scrollDelay = 0;
    	
    	private TextScrollDelay(int delay)
    	{
    		c_scrollDelay = delay;
    	}
        /**
      	 * \brief This method returns the delaying time of this class.
      	 * 
      	 * \returns ScrollDelay time.
      	 */
    	public int getScrollDelay()
    	{
    		return c_scrollDelay;
    	}
    }
    
    /**
  	 * \brief This property specifies the hyperlink of text that describes the hypertext information.
  	 */
    public class TextHyperText
    {
    	private short c_startcharoffset;
    	private short c_endcharoffset;
    	private String c_URLString;
    	private String c_altString;
    	
    	private TextHyperText(short startOffset, short endOffset, String URL, String alt)
    	{
    		c_startcharoffset = startOffset;
    		c_endcharoffset = endOffset;
    		c_URLString = URL;
    		c_altString = alt;
    	}
    	/**
    	 * \brief This property specifies the starting position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getStartOffset()
    	{
    		return c_startcharoffset;
    	}
    	/**
    	 * \brief This property specifies the ending position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getEndOffset()
    	{
    		return c_endcharoffset;
    	}
    	/**
    	 * \brief The link to URL.
    	 *
    	 * \returns URL link  of this  class. 
    	 */
    	public String getURL()
    	{
    		return c_URLString;
    	}
    	/**
    	 * \brief An 'alt' string for user display.
    	 * 
    	 * The altString is a tool-tip or other visual clue.
    	 *
    	 * \returns alt String of this class. 
    	 */
    	public String getAlt()
    	{
    		return c_altString; 
    	}
    }
    
    
    /**
  	 * \brief This property requests blinking text for the indicated character range.
  	 * 
  	 */
    public class TextBlink
    {
    	private short c_startcharoffset = 0;
    	private short c_endcharoffset = 0;
    	
    	private TextBlink(short start, short end)
    	{
    		c_startcharoffset = start;
    		c_endcharoffset = end;
    	}
    	/**
    	 * \brief This property specifies the starting position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getStartOffset()
    	{
    		return c_startcharoffset;
    	}
    	/**
    	 * \brief This property specifies the ending position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getEndOffset()
    	{
    		return c_endcharoffset;
    	}
    }
    
    /**
  	 * \brief This property specifies the text wrap behavior.
  	 */
    public enum TextWrap
    {
    	NO_WRAP(0),
    	AUTOMATIC_SOFT_WRAP(1);
    	
    	private int m_value;

		private TextWrap( int value ) {
			m_value = value;
		}

		public int getValue() {
			return m_value;
		}
		
		public static TextWrap fromValue( int value ) {
			for( TextWrap item : values() ) {
				if( item.getValue() == value )
					return item;
			}
			return null;
		}
    	
    }
    
    private TextStyle m_textStyle = null;
    private TextHighlight m_textHighlight = null;
    private TextHighlightColor m_textHighlightColor = null;
    private TextKaraoke m_textKaraoke = null;
    private TextScrollDelay m_textScrollDelay = null;
    private TextHyperText m_textHyperText = null;
    private Rect m_TextBox = null;
    private ArrayList<TextBlink> m_textBlink = null;
    private TextWrap m_textWrap = null;
    private float[] m_Matrix = null;
    
    private ArrayList<CharSequence> m_fontTableLable = new ArrayList<CharSequence>();
    private ArrayList<Integer> m_fontTableIndex = new ArrayList<Integer>();
    
    private NexClosedCaption(int region_tx,
    						int region_ty,
    						int region_width,
    						int region_height,
    						int background_color,
    						byte [] textData)
    {
    	m_3gppTT_TextBuffer = textData;
    	m_3gppTTRegionTX = region_tx;
    	m_3gppTTRegionTY = region_ty;
    	m_3gppTTRegionWidth = region_width;
    	m_3gppTTRegionHeight = region_height;
    	m_3gppTTBGColor = background_color;
    	mTextType = TEXT_TYPE_3GPP_TIMEDTEXT;
    	if(m_textBlink != null)
    		m_textBlink = null;
    	m_textBlink = new ArrayList<TextBlink>();
    }
    // This method is called by native.
    @SuppressWarnings("unused")
	private void setSampleModifier_CreateTextStyle(int count)
    {
    	if(m_textStyle != null)
    	{
    		m_textStyle = null;
    	}
    	m_textStyle = new TextStyle(count);
    }
    // This method is called by native.
    @SuppressWarnings("unused")
	private void setSampleModifier_AddTextStyleEntry(TextStyleEntry entry)
    {
    	if(m_textStyle == null)
    	{
    		return;
    	}
    	if(entry != null)
    		m_textStyle.setTextStyleEntry(entry);
//    	else
//    	{
//    		m_textStyle.setCount(m_textStyle.getCurrentCount());
//    	}
    }
 // This method is called by native.
    @SuppressWarnings("unused")
	private void setSampleModifier_Karaoke(int startTime, int count)
    {
    	if(m_textKaraoke != null)
    		m_textKaraoke = null;
    	
    	m_textKaraoke = new TextKaraoke(startTime, count);
    }
 // This method is called by native.
    @SuppressWarnings("unused")
	private void setSampleModifier_KaraokeEntry(TextKaraokeEntry entry)
    {
    	m_textKaraoke.setKaraokeEntry(entry);
    }
 // This method is called by native.
    @SuppressWarnings("unused")
	private void setSampleModifier_HyperText(short startOffset, short endOffset, byte[] byteURL, byte[] byteAlt)
    {
    	if(m_textHyperText != null)
    		m_textHyperText = null;
    	
    	String strURL = null;
    	String strAlt = null;
    	
    	try {
			strURL = new String(byteURL, 0, byteURL.length, "UTF-8");
	    	strAlt = new String(byteAlt, 0, byteAlt.length, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e)
		{
			e.printStackTrace();
		}
    	
    	m_textHyperText = new TextHyperText(startOffset, endOffset, strURL, strAlt);
    }
 // This method is called by native.
    @SuppressWarnings("unused")
	private void setMatrix(float[] matrix)
    {
    	m_Matrix = matrix;
    }
 // This method is called by native.
    @SuppressWarnings("unused")
	private void setSample_FontTable(int fontID, byte[] fontName)
    {
    	m_fontTableLable.add(fontName.toString());
    	m_fontTableIndex.add(fontID);
    }
 // This method is called by native.
    @SuppressWarnings("unused")
	private void setSampleModifier_General(int sampleModifierID, int uUserData1, int uUserData2, int uUserData3, int uUserData4)
    {
    	Log.d("NexClosedCaption", "Call setSampleModifier_General, id : " + sampleModifierID + " " + uUserData1 + " " + uUserData2 + " " + uUserData3 + " " + uUserData4);
    	switch(sampleModifierID)
    	{
	    	case SampleModifier_TEXTSTYLE:
	    	{
	    		break;
	    	}
	    	case SampleModifier_TEXTHIGHLIGHT:
	    	{
	    		if(m_textHighlight != null)
	    		{
	    			m_textHighlight = null;
	    			//gc will collecting conventional data.
	    		}
	    		m_textHighlight = new TextHighlight((short)uUserData1, (short)uUserData2);
	    		break;
	    	}
	    	case SampleModifier_TEXTHILIGHTCOLOR:
	    	{
	    		if(m_textHighlightColor != null)
	    		{
	    			m_textHighlightColor = null;
	    			//gc will collecting conventional data.
	    		}
	    		m_textHighlightColor = new TextHighlightColor(uUserData1);
	    		break;
	    	}
	    	case SampleModifier_TEXTKARAOKE:
	    	{
	    		break;
	    	}
	    	case SampleModifier_TEXTSCROLLDELAY:
	    	{
	    		if(m_textScrollDelay != null)
	    		{
	    			m_textScrollDelay = null;
	    		}
	    		m_textScrollDelay = new TextScrollDelay(uUserData1);
	    		m_startTime = uUserData2;
	    		m_endTime = uUserData3;
	    		break;
	    	}
	    	case SampleModifier_TEXTHYPERTEXT:
	    	{
	    		break;
	    	}
	    	case SampleModifier_TEXTTEXTBOX://obj = null, t, l, b, r
	    	{
	    		m_TextBox = new Rect(uUserData2, uUserData1, uUserData4, uUserData3);//left top right bottom
	    		break;
	    	}
	    	case SampleModifier_TEXTBLINK:
	    	{
	    		m_textBlink.add(new TextBlink((short)uUserData1, (short)uUserData2));
	    		break;
	    	}
	    	case SampleModifier_TEXTTEXTWRAP://obj = null, flag
	    	{
	    		m_textWrap = TextWrap.fromValue(uUserData1);
	    		break;
	    	}
	    	case SampleModifier_VerticalJustification:
	    	{
	    		m_VerticalJustification = uUserData1;
	    		break;
	    	}
	    	case SampleModifier_HorizontalJustification:
	    	{
	    		m_HorizontalJustification = uUserData1;
	    		break;
	    	}
	    	case  SampleModifier_ScrollIN:
	    	{
	    		if(uUserData1 != 0)
	    			isScrollIn = true;
	    		else
	    			isScrollIn = false;
	    		break;
	    	}
	    	case SampleModifier_ScrollOUT:
	    	{
	    		if(uUserData1 != 0)
	    			isScrollOut = true;
	    		else
	    			isScrollOut = false;
	    		break;
	    	}
	    	case SampleModifier_ScrollDirection:
	    	{
	    		isScrollDirection = uUserData1;
	    		break;
	    	}
	    	case SampleModifier_ContinuousKaraoke:
	    	{
	    		if(uUserData1 != 0)
	    			isContinuousKaraoke = true;
	    		else
	    			isContinuousKaraoke = false;
	    		break;
	    	}
	    	case SampleModifier_WriteVertically:
	    	{
	    		if(uUserData1 != 0)
	    			isWriteVertically = true;
	    		else
	    			isWriteVertically = false;
	    		break;
	    	}
	    	case SampleModifier_FillTextRegion:
	    	{
	    		if(uUserData1 != 0)
	    			isFillTextRegion = true;
	    		else
	    			isFillTextRegion = false;
	    		break;
	    	}
	    	default:
	    	{
	    		break;
	    	}
    	}
    }
	/**
	 * \brief This property gets the current TextStyle class.
	 * 
	 * \returns The current TextStyle class.
	 */
    public TextStyle getTextStyle()
    {
    	return m_textStyle;
    }
    /**
	 * \brief This property gets the current TextHighlight class.
	 * 
	 * \returns The current TextHighlight class.
	 */
    public TextHighlight getTextHighlight()
    {
    	return m_textHighlight;
    }
    /**
	 * \brief This property gets the current TextHighlightColor class.
	 * 
	 * \returns The current TextHighlightColor class.
	 */
    public TextHighlightColor getTextHighlightColor()
    {
    	return m_textHighlightColor;
    }
    /**
	 * \brief This property gets the current TextKaraoke class.
	 * 
	 * \returns The current TextKaraoke class.
	 */
    public TextKaraoke getTextKaraoke()
    {
    	return m_textKaraoke;
    }
    /**
	 * \brief This property gets the current TextScrollDelay class.
	 * 
	 * \returns The current TextScrollDelay class.
	 */
    public TextScrollDelay getTextScrollDelay()
    {
    	return m_textScrollDelay;
    }
    /**
	 * \brief This property gets the current TextHyperText class.
	 * 
	 * \returns The current TextHyperText class.
	 */
    public TextHyperText getTextHyperText()
    {
    	return m_textHyperText;
    }
    /**
	 * \brief This property gets the rectangle box for text drawing.
	 * 
	 * \returns The rectangle box.
	 */
    public Rect getTextBox()
    {
    	return m_TextBox;
    }
    /**
	 * \brief This property gets the array of the current TextBlink class.
	 * 
	 * \returns The array of the current TextBlink class.
	 */
    public TextBlink[] getTextBlink()
    {
    	TextBlink[] blink = new TextBlink[m_textBlink.size()];
    	try{
    		m_textBlink.toArray(blink);
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	return blink;
    }
    /**
	 * \brief This property gets the current TextWrap class.
	 * 
	 * \returns The current TextWrap class.
	 */
    public TextWrap getTextWrap()
    {
    	return m_textWrap;
    }
    /**
	 * \brief This property gets the coordination of the rectangle text box.
	 * 
	 * \returns The coordination of the rectangle text box.
	 */
    public int[] getTextboxCoordinatesFor3GPPTT()
    {
    	int[] texCoord = new int[4];
    	texCoord[0] = m_3gppTTRegionTX;
    	texCoord[1] = m_3gppTTRegionTY;
    	texCoord[2] = m_3gppTTRegionWidth;
    	texCoord[3] = m_3gppTTRegionHeight;
    	
    	return texCoord;
    }
    
    /**
	 * \brief This property gets default color of the text.
	 * 
	 * \returns The color of the  text.
	 */
    public int getForegroundColorFor3GPPTT()
    {
    	return m_3gppTTTextColor;
    }
    /**
   	 * \brief This property gets color of the rectangle text box.
   	 * 
   	 * \returns The color of the rectangle text box.
   	 */
    public int getBackgroundColorFor3GPPTT()
    {
    	return m_3gppTTBGColor;
    }
    /**
   	 * \brief This property gets data of the text.
   	 * 
   	 * \returns Data of the text.
   	 */
    public byte[] getTextDataFor3GPPTT()
    {
    	return m_3gppTT_TextBuffer;
    }
    
    @Deprecated
    public float[] getMatrix()
    {
    	return m_Matrix;
    }
    /**
   	 * \brief This property justify the alignment of the text vertically, which are top, centre and bottom.
   	 * 
   	 * \returns The alignment of the text. This will be one of the following values:
     *              - <b>Top		(0)</b>
     *              - <b>Centre		(1)</b>
     *              - <b>Bottom		(-1)</b>
   	 */
    public int getVerticalJustification()
    {
    	return m_VerticalJustification;
    }
    /**
   	 * \brief This property justify the alignment of the text horizontally, which are left, centre and right.
   	 * 
   	 * \returns The alignment of the text. This will be one of the following values:
     *              - <b>Left		(0)</b>
     *              - <b>Centre		(1)</b>
     *              - <b>Right		(-1)</b>
   	 */
    public int getHorizontalJustification()
    {
    	return m_HorizontalJustification;
    }
    /**
	 * \brief This property specifies the text entering from outside the rectangle box.
	 * 
	 * \returns TRUE if <b>ON</b>, FALSE if OFF.
	 */
    public boolean getScrollIn()
    {
    	return isScrollIn;
    }
    /**
	 * \brief This property specifies the text exiting from inside the rectangle box.
	 * 
	 * \returns TRUE if <b>ON</b>, FALSE if OFF.
	 */
    public boolean getScrollOut()
    {
    	return isScrollOut;
    }
    /**
   	 * \brief This property specifies the direction of the text entering or exiting the rectangle text box.
   	 * 
   	 * \returns The direction of the text. This will be one of the following values:
     *              - <b>Bottom to top		(0)</b>
     *              - <b>Right to left		(1)</b>
     *              - <b>Top to bottom		(2)</b>
     *              - <b>Left to right		(3)</b>
   	 */
    public int getScrollDirection()
    {
    	return isScrollDirection;
    }
    /**
	 * \brief This property specifies the ContinuousKaraoke.
	 * 
	 * \returns TRUE if <b>YES</b>, FALSE if No.
	 */
    public boolean getContinuousKaraoke()
    {
    	return isContinuousKaraoke;
    }
    /**
	 * \brief This property vertical position the text will be written in.
	 * 
	 * \returns TRUE if <b>YES</b>, FALSE if No.
	 */
    public boolean getWritingVertically()
    {
    	return isWriteVertically;
    }
    /**
	 * \brief This property bacground region of the text.
	 * 
	 * \returns TRUE if <b>YES</b>, FALSE if No.
	 */
    public boolean getFillTextRegion()
    {
    	return isFillTextRegion;
    }
    
    /**
  	 * \brief This property describes the font index of the text. See spec 5.16 font table.
  	 */
    public int[] getFontTableIndex()
    {
    	int[] fontIndex = new int[m_fontTableIndex.size()];
    	Integer[] iIndex = new Integer[m_fontTableIndex.size()];
    	m_fontTableIndex.toArray(iIndex);
    	for(int i=0;i<iIndex.length;i++)
    	{
    		fontIndex[i] = iIndex[i].intValue();
    	}
    	return fontIndex;
    }
    
    /**
  	 * \brief This property specifies the caption time of the text.
  	 */
    public int[] getCaptionTime()
    {
    	int[] cTime = new int [2];
    	cTime[0] = m_startTime;
    	cTime[1] = m_endTime;
    	return cTime;
    }

}
