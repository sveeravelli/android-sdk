/************************************************************************
VisualOn Proprietary
Copyright (c) 2014, VisualOn Incorporated. All Rights Reserved

VisualOn, Inc., 4675 Stevens Creek Blvd, Santa Clara, CA 95051, USA

All data and information contained in or disclosed by this document are
confidential and proprietary information of VisualOn, and all rights
therein are expressly reserved. By accepting this material, the
recipient agrees that this material and the information contained
therein are held in confidence and in trust. The material may only be
used and/or disclosed as authorized in a license agreement controlling
such use and disclosure.
************************************************************************/

package com.visualon.AppPlayerCommonFeatures;


import java.util.HashMap;




public class TimeCal {
	
	private static final 	String          TAG      =       "[APP][Time]";
	private static HashMap<String, Long>    m_hmTime =       new HashMap<String, Long>();
	private static String[]                 m_sTime  =       new String[]{"Init", "Open","Open complete", "Open to render", "Run", "Buffer", "Seek",
	     "Seek complete", "Seek to last chunk", "Pause", "Start", "Stop", "Play complete", "Select video","Select video complete", "Select audio",
	     "Select audio complete", "Select subtitle", "Select subtitle complete", "Commit selection"};
	public static enum API_TIME_TYPE {
	            INIT_TIME                                       (0),
	            OPEN_TIME                                       (1),
	            OPEN_COMPLETE_TIME                              (2),
	            OPEN_RENDER_TIME                                (3),
	            RUN_TIME                                        (4),
	            BUFFER_TIME                                     (5),
	            SEEK_TIME                                       (6),
	            SEEK_COMPLETE_TIME                              (7),
	            SEEK_TO_LATEST_TRUNK                            (8),
	            PAUSE_TIME                                      (9),
	            START_TIME                                      (10),
	            STOP_TIME                                       (11),
	            PLAY_COMPLETE                                   (12),
	            SELECT_VIDEO                                    (13),
	            SELECT_VIDEO_COMPLETE                           (14),
	            SELECT_AUDIO                                    (15),
	            SELECT_AUDIO_COMPLETE                           (16),
	            SELECT_SUBTITLE                                 (17),
	            SELECT_SUBTITLE_COMPLETE                        (18),
	            COMMIT_SELECTION                                (19),
	            API_TIME_MAX_ID                                 (0xFFFFFFFF);
	            
	            private int value;
	            API_TIME_TYPE(int value)
	            {
	                this.value = value;
	            }  
	            
	            public int getValue()
	            {
	                return value;
	            }
	            
	            public static API_TIME_TYPE valueOf(int value)
	            {
	                for (int i = 0; i < API_TIME_TYPE.values().length; i ++)
	                {
	                    if (API_TIME_TYPE.values()[i].getValue() == value)
	                        return API_TIME_TYPE.values()[i];
	                }
	                
	                return API_TIME_MAX_ID;
	            }
	 } 

	public static void printStartTime(API_TIME_TYPE type){
	    long startTime = System.currentTimeMillis();
	    String title = m_sTime[type.getValue()];
	    m_hmTime.put(title, startTime);
	    voLog.i(TAG , title + " start time: " + startTime + " ms");
	    
	}
	public static void resetStartTime(API_TIME_TYPE type){
	    String title = m_sTime[type.getValue()];
	    m_hmTime.put(title, null);
	}
    public static void printUsingTime(API_TIME_TYPE type){
        long stopTime = System.currentTimeMillis();
        API_TIME_TYPE tmpType = type;
        String title = m_sTime[type.getValue()];
        if (type == API_TIME_TYPE.OPEN_RENDER_TIME || type == API_TIME_TYPE.OPEN_COMPLETE_TIME) 
            tmpType = API_TIME_TYPE.OPEN_TIME;
        
        if (type == API_TIME_TYPE.SEEK_TO_LATEST_TRUNK || type == API_TIME_TYPE.SEEK_COMPLETE_TIME)
            tmpType = API_TIME_TYPE.SEEK_TIME;
        if (type == API_TIME_TYPE.SELECT_VIDEO_COMPLETE)
            tmpType = API_TIME_TYPE.SELECT_VIDEO;
        if (type == API_TIME_TYPE.SELECT_AUDIO_COMPLETE)
            tmpType = API_TIME_TYPE.SELECT_AUDIO;
        if (type == API_TIME_TYPE.SELECT_SUBTITLE_COMPLETE)
            tmpType = API_TIME_TYPE.SELECT_SUBTITLE;
        
        if (type == API_TIME_TYPE.PLAY_COMPLETE) {
            voLog.i(TAG , "play complete time :" + stopTime + " ms");
            return;
        }
        
        String tempTitle = m_sTime[tmpType.getValue()];
        if (m_hmTime.get(tempTitle) != null){
        long startTime = m_hmTime.get(tempTitle);
        long deltaTime = stopTime - startTime;
        voLog.i(TAG, title + " using time: " + deltaTime + " ms, current time: " + stopTime + " ms");
        }
        
    }
}
