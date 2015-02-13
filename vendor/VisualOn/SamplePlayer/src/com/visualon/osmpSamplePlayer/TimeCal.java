/************************************************************************
VisualOn Proprietary
Copyright (c) 2013, VisualOn Incorporated. All Rights Reserved

VisualOn, Inc., 4675 Stevens Creek Blvd, Santa Clara, CA 95051, USA

All data and information contained in or disclosed by this document are
confidential and proprietary information of VisualOn, and all rights
therein are expressly reserved. By accepting this material, the
recipient agrees that this material and the information contained
therein are held in confidence and in trust. The material may only be
used and/or disclosed as authorized in a license agreement controlling
such use and disclosure.
************************************************************************/

package com.visualon.osmpSamplePlayer;

import com.visualon.OSMPUtils.voLog;

public class TimeCal {
	
	private static final 	String TAG  			= "TimeCal";
	private static boolean  m_bEnable 				= true;
	
	private static long 	m_lLastTime;
	private static boolean  m_bStart				= false;
	
	public static void printTime(String strTitle) {
		
		if (m_bEnable == false)
			return;
		
		long lCurTime = System.currentTimeMillis();
		
		if (m_bStart == false) { 
			m_bStart = true;
			
			m_lLastTime = lCurTime;
			
			voLog.i(TAG + " " + strTitle, "Current Time: " + lCurTime + " ms");
		} else {
		
			long lTimeCost = lCurTime - m_lLastTime;
			voLog.i(TAG + " " + strTitle, "Current Time: " + lCurTime + " ms. Time delta: " + lTimeCost + " ms");
		}
		
		m_lLastTime = lCurTime;
		
		return;
    }
}
