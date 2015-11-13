/*******************************************************************************
 * Copyright
 *  This code is strictly confidential and the receiver is obliged to use it
 *  exclusively for his or her own purposes. No part of Viaccess Orca code may be
 *  reproduced or transmitted in any form or by any means, electronic or
 *  mechanical, including photocopying, recording, or by any information storage
 *  and retrieval system, without permission in writing from Viaccess Orca.
 *  The information in this code is subject to change without notice. Viaccess Orca
 *  does not warrant that this code is error free. If you find any problems
 *  with this code or wish to make comments, please report them to Viaccess Orca.
 *  
 *  Trademarks
 *  Viaccess Orca is a registered trademark of Viaccess S.A in France and/or other
 *  countries. All other product and company names mentioned herein are the
 *  trademarks of their respective owners.
 *  Viaccess S.A may hold patents, patent applications, trademarks, copyrights
 *  or other intellectual property rights over the code hereafter. Unless
 *  expressly specified otherwise in a Viaccess Orca written license agreement, the
 *  delivery of this code does not imply the concession of any license over
 *  these patents, trademarks, copyrights or other intellectual property.
 *******************************************************************************/

package com.example.csp;

import android.content.Context;

import com.discretix.drmdlc.api.IDxRightsInfo;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Helps to localize the Rights information of a DRM license
 */
public class RightsInfoLocalizer {
    final static private long CSP_SECONDS_IN_MINUTE = 60;
    final static private long CSP_SECONDS_IN_HOUR = (CSP_SECONDS_IN_MINUTE * 60);
    final static private long CSP_SECONDS_IN_DAY = (CSP_SECONDS_IN_HOUR * 24);
    final static private long CSP_SECONDS_IN_MONTH = (CSP_SECONDS_IN_DAY * 30);
    final static private long CSP_SECONDS_IN_YEAR = (CSP_SECONDS_IN_DAY * 365);

    /**
     * Convert an array of {@link IDxRightsInfo} to string representation.
     * 
     * @param context Application context.
     * @param rights Array of {@link IDxRightsInfo} objects.
     * @return String concatenation of Rights with , separation
     */
    public static String toSringRightsInfo(Context context, IDxRightsInfo[] rights) {
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
     * Returns a string that represents the rights object. This string may be displayed to the user.
     * 
     * @param context Application context.
     * @param rights {@link IDxRightsInfo} object.
     * @return A string representation of Rights
     */
    public static final String toString(Context c, IDxRightsInfo rights) {
        String retStr = null;
        switch (rights.getStatus()) {
            case VALID:
                retStr = validRightsToString(c, rights);
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

    /**
     * Returns a string representation of a valid rights object. It will depends on several
     * constrains into the rights object.
     * 
     * @param c Application context.
     * @param rights {@link IDxRightsInfo} object.
     */
    private static final String validRightsToString(Context c, IDxRightsInfo rights) {
        String retStr = null;
        boolean countConstraint = rights.getCountLeft() > 0;
        boolean timeConstraint = (rights.getStartTime() != null) || (rights.getEndTime() != null);
        boolean intervalFromPlayConstraint = rights.getIntervalPeriodInSeconds() > 0;

        // If there are some constraint
        if ((countConstraint) || (timeConstraint) || (intervalFromPlayConstraint)) {
            String countStr = "";
            String timeStr = "";
            String intervalStr = "";

            if (countConstraint == true) {
                countStr = countToString(c, rights.getCountLeft());
            }

            if (timeConstraint) {
                timeStr = timeConstraintToString(c, rights.getStartTime(), rights.getEndTime());
            }

            if (intervalFromPlayConstraint) {
                String tempStr = durationToString(c, rights.getIntervalPeriodInSeconds());
                intervalStr = " "
                        + String.format(c.getString(R.string.RIGHTS_FROM_FIRST_USE), tempStr);
            }

            StringBuffer tmpBuffer = new StringBuffer();
            tmpBuffer.append(countStr);
            tmpBuffer.append(intervalStr);
            tmpBuffer.append(timeStr);

            retStr = c.getString(R.string.RIGHTS_PLAY_PREFIX) + tmpBuffer.toString();
        } else { // unlimited play
            retStr = c.getString(R.string.RIGHTS_UNLIMITED_PLAY);
        }

        return retStr;
    }

    /**
     * Returns a String representation of a time.
     * 
     * @param context Application context.
     * @param timeInSeconds time in seconds.
     * @return A string representation of a time interval.
     */
    private static String durationToString(Context c, long timeInSeconds) {
        int timeUnitIds = 0;
        long timeValue = 0;

        if (timeInSeconds < 2 * CSP_SECONDS_IN_MINUTE) // < 2 minute
        {
            timeUnitIds = (timeValue == 1) ? R.string.RIGHTS_SECOND : R.string.RIGHTS_SECONDS;
        } else if (timeInSeconds < 2 * CSP_SECONDS_IN_HOUR) // < 2 hours
        {
            timeValue = timeInSeconds / CSP_SECONDS_IN_MINUTE;
            timeUnitIds = (timeValue == 1) ? R.string.RIGHTS_MINUTE : R.string.RIGHTS_MINUTES;
        } else if (timeInSeconds < 2 * CSP_SECONDS_IN_DAY) // < 2 days
        {
            timeValue = timeInSeconds / CSP_SECONDS_IN_HOUR;
            timeUnitIds = (timeValue == 1) ? R.string.RIGHTS_HOUR : R.string.RIGHTS_HOURS;
        } else if (timeInSeconds < 2 * CSP_SECONDS_IN_MONTH) // < 2 months
        {
            timeValue = timeInSeconds / CSP_SECONDS_IN_DAY;
            timeUnitIds = (timeValue == 1) ? R.string.RIGHTS_DAY : R.string.RIGHTS_DAYS;
        } else if (timeInSeconds < 2 * CSP_SECONDS_IN_YEAR) // < 2 year
        {
            timeValue = timeInSeconds / CSP_SECONDS_IN_MONTH;
            timeUnitIds = (timeValue == 1) ? R.string.RIGHTS_MONTH : R.string.RIGHTS_MONTHS;
        } else {
            timeValue = timeInSeconds / CSP_SECONDS_IN_YEAR;
            timeUnitIds = (timeValue == 1) ? R.string.RIGHTS_YEAR : R.string.RIGHTS_YEARS;
        }

        return String.format(c.getString(timeUnitIds), java.lang.Long.toString(timeValue));
    }

    /**
     * Returns a String representation of a time constrain.
     * 
     * @param context Application context.
     * @param startTime Start time constraint.
     * @param endTime End time constrain.
     * @return A human understandable string representation of a time constrain.
     */
    public static String timeConstraintToString(Context c, Date startTime, Date endTime) {
        String tempStr;
        String fromStr = "";
        String untilStr = "";
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();

        try{
        	startCalendar.setTime(startTime);
        }catch (NullPointerException e)
        {
        	
        }
        if (startTime != null && startCalendar.get(Calendar.YEAR) != 0) {
            tempStr = dateTimeToString(startTime);
            fromStr = " " + String.format(c.getString(R.string.RIGHTS_FROM_TIME), tempStr);
        }

        try{
        	endCalendar.setTime(endTime);
        }catch (NullPointerException e)
        {
        	
        }
        if (endTime != null && endCalendar.get(Calendar.YEAR) < 0xFFFF) {
            tempStr = dateTimeToString(endTime);
            untilStr = " " + String.format(c.getString(R.string.RIGHTS_UNTIL_TIME), tempStr);
        }

        // If license is valid and start time is in the past - show only end
        // time, or unlimited permission (if end time not defined).
        if (startTime != null && startCalendar.get(Calendar.YEAR) != 0) {
            if (endTime != null && endCalendar.get(Calendar.YEAR) != 0) {
                fromStr = "";
            } else // end time not defined
            {
                return "";
            }
        }
        return fromStr + untilStr;
    }

    /**
     * Returns a String representation of a Date in {@link DateFormat#SHORT} format.
     * 
     * @param timeStruct {@link Date} object
     */
    public static final String dateTimeToString(Date timeStruct) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return dateFormat.format(timeStruct);
    }

    /**
     * Returns a String representation of a value accordingly to {@link R.string#RIGHTS_TIME}.
     * 
     * @param context Application context.
     * @param countValue Int value to transform.
     */
    public static String countToString(Context c, long countValue) {
        String countString = null;

        if (countValue == 1) {
            countString = String.format(c.getString(R.string.RIGHTS_TIME), countValue);
        } else {
            countString = String.format(c.getString(R.string.RIGHTS_TIMES), countValue);
        }

        return countString;
    }

}
