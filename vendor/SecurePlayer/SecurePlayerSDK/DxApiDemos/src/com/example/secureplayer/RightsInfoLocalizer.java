package com.example.secureplayer;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;

import com.discretix.drmdlc.api.IDxRightsInfo;

public class RightsInfoLocalizer {
	final static private long DX_SECONDS_IN_MINUTE = 60;
	final static private long DX_SECONDS_IN_HOUR = (DX_SECONDS_IN_MINUTE * 60);
	final static private long DX_SECONDS_IN_DAY = (DX_SECONDS_IN_HOUR * 24);
	final static private long DX_SECONDS_IN_MONTH = (DX_SECONDS_IN_DAY * 30);
	final static private long DX_SECONDS_IN_YEAR = (DX_SECONDS_IN_DAY * 365);

	public static String toSringRightsInfo(Context context,
			IDxRightsInfo[] rights) {
		String retStr = null;

		if (rights == null || rights.length == 0) {
			retStr = context.getString(R.string.RIGHTS_NONE);
		} else if (rights.length == 1) {
			retStr = toString(context, rights[0]);
		} else {
			StringBuffer tmp = new StringBuffer();
			tmp.append(toString(context, rights[0]));
			for (int i = 1; i < rights.length; i++) {
				tmp.append(",");
				tmp.append(toString(context, rights[i]));
			}
			retStr = tmp.toString();
		}

		return retStr;
	}

	/**
	 * This function returns a string that represents the rights object. This
	 * string may be displayed to the user.
	 */
	public static final String toString(Context c, IDxRightsInfo rights) {
		String retStr = null;
		switch (rights.getStatus()) {
		case VALID:
			retStr = toStringValidRights(c, rights);
			break;
		case FUTURE:
			retStr = c.getString(R.string.RIGHTS_FUTURE);
			break;
		case INVALID:
		case SECURE_CLOCK_NOT_SET:
			retStr = c.getString(R.string.RIGHTS_NOT_VALID);
			break;
		}
		return retStr;
	}

	private static final String toStringValidRights(Context c,
			IDxRightsInfo rights) {
		// StringBuilder SB = new StringBuilder();
		// SB.append(c.getString(R.string.RIGHTS_PLAY_PREFIX));

		String retStr = null;
		boolean countContraint = rights.getCountLeft() > 0;
		boolean timeContraint = (rights.getStartTime() != null)
				|| (rights.getEndTime() != null);
		boolean intervalFromPlayConstraint = rights
				.getIntervalPeriodInSeconds() > 0;

		// Check unlimited play or not
		if ((countContraint) || (timeContraint) || (intervalFromPlayConstraint)) {
			String countStr = "";
			String timeStr = "";
			String intervalStr = "";

			if (countContraint == true) {
				countStr = CountToString(c, rights.getCountLeft());
			}

			if (timeContraint) {
				timeStr = TimeConstraintToString(c, rights.getStartTime(),
						rights.getEndTime());
			}

			if (intervalFromPlayConstraint) {
				String tempStr = DurationToString(c,
						rights.getIntervalPeriodInSeconds());
				intervalStr = " "
						+ String.format(
								c.getString(R.string.RIGHTS_FROM_FIRST_USE),
								tempStr);
			}

			StringBuffer tmpBuffer = new StringBuffer();
			tmpBuffer.append(countStr);
			tmpBuffer.append(intervalStr);
			tmpBuffer.append(timeStr);

			retStr = c.getString(R.string.RIGHTS_PLAY_PREFIX)
					+ tmpBuffer.toString();
		} else { // unlimited play
			retStr = c.getString(R.string.RIGHTS_UNLIMITED_PLAY);
		}

		return retStr;
	}

	private static String DurationToString(Context c, long timeInSeconds) {
		int timeUnitIds = 0;
		long timeValue = 0;
		// String timeBuff = null;

		if (timeInSeconds < 2 * DX_SECONDS_IN_MINUTE) // < 2 minute
		{
			timeUnitIds = (timeValue == 1) ? R.string.RIGHTS_SECOND
					: R.string.RIGHTS_SECONDS;
		} else if (timeInSeconds < 2 * DX_SECONDS_IN_HOUR) // < 2 hours
		{
			timeValue = timeInSeconds / DX_SECONDS_IN_MINUTE;
			timeUnitIds = (timeValue == 1) ? R.string.RIGHTS_MINUTE
					: R.string.RIGHTS_MINUTES;
		} else if (timeInSeconds < 2 * DX_SECONDS_IN_DAY) // < 2 days
		{
			timeValue = timeInSeconds / DX_SECONDS_IN_HOUR;
			timeUnitIds = (timeValue == 1) ? R.string.RIGHTS_HOUR
					: R.string.RIGHTS_HOURS;
		} else if (timeInSeconds < 2 * DX_SECONDS_IN_MONTH) // < 2 months
		{
			timeValue = timeInSeconds / DX_SECONDS_IN_DAY;
			timeUnitIds = (timeValue == 1) ? R.string.RIGHTS_DAY
					: R.string.RIGHTS_DAYS;
		} else if (timeInSeconds < 2 * DX_SECONDS_IN_YEAR) // < 2 year
		{
			timeValue = timeInSeconds / DX_SECONDS_IN_MONTH;
			timeUnitIds = (timeValue == 1) ? R.string.RIGHTS_MONTH
					: R.string.RIGHTS_MONTHS;
		} else {
			timeValue = timeInSeconds / DX_SECONDS_IN_YEAR;
			timeUnitIds = (timeValue == 1) ? R.string.RIGHTS_YEAR
					: R.string.RIGHTS_YEARS;
		}

		return String.format(c.getString(timeUnitIds),
				java.lang.Long.toString(timeValue));
	}

	public static String TimeConstraintToString(Context c, Date startTime,
			Date endTime) {
		String tempStr;
		String fromStr = "";
		String untilStr = "";

		if (startTime != null && startTime.getYear() != 0) {
			tempStr = DateTimeToString(startTime);
			fromStr = " "
					+ String.format(c.getString(R.string.RIGHTS_FROM_TIME),
							tempStr);
		}

		if (endTime != null && endTime.getYear() < 0xFFFF) {
			tempStr = DateTimeToString(endTime);
			untilStr = " "
					+ String.format(c.getString(R.string.RIGHTS_UNTIL_TIME),
							tempStr);
		}

		// If license is valid and start time is in the past - show only end
		// time,
		// or unlimited permission (if end time not defined).
		if (startTime != null && startTime.getYear() != 0) {
			if (endTime != null && endTime.getYear() != 0) {
				fromStr = "";
			} else // end time not defined
			{
				return ""; // c.getString(R.string.RIGHTS_UNLIMITED_PLAY);
			}
		}
		return fromStr + untilStr;
	}

	public static final String DateTimeToString(Date timeStruct) {
		DateFormat dateFormat = DateFormat.getDateTimeInstance(
				DateFormat.SHORT, DateFormat.SHORT);
		return dateFormat.format(timeStruct);
	}

	public static String CountToString(Context c, long countValue) {
		String countString = null;

		if (countValue == 1) {
			countString = String.format(c.getString(R.string.RIGHTS_TIME),
					countValue);
		} else {
			countString = String.format(c.getString(R.string.RIGHTS_TIMES),
					countValue);
		}

		return countString;
	}

}
