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

package com.visualon.AppBehavior;

import java.util.ArrayList;


import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;

public interface AppBehaviorManager {
    
    /**
     * Enumeration of callback event IDs.
     * APP_BEHAVIOR_EVENT_ID
     */
    public enum APP_BEHAVIOR_EVENT_ID {
        /** App receive an error but can continue play */
        APP_BEHAVIOR_CONTINUE_PLAY         (0x00000000),
        /** App receive an error and should stop play */
        APP_BEHAVIOR_STOP_PLAY             (0x00000001),
        /** App receive an error and should pause play */
        APP_BEHAVIOR_PAUSE_PLAY            (0x00000002),
        /** App receive an error and should switch to SW */
        APP_BEHAVIOR_SWITCH_ENGINE         (0x00000003),
        /** Max value definition    */
        APP_BEHAVIOR_MAX                   (0xFFFFFFFF);
        
        private int value;
        
        APP_BEHAVIOR_EVENT_ID(int value)
        {
            this.value = value;
        }
        
        public int getValue()
        {
            return value;
        }
        
        public static APP_BEHAVIOR_EVENT_ID valueOf(int value)
        {
            for (int i = 0; i < APP_BEHAVIOR_EVENT_ID.values().length; i++)
            {
                if (value == APP_BEHAVIOR_EVENT_ID.values()[i].getValue())
                    return APP_BEHAVIOR_EVENT_ID.values()[i];
            }
            
            return APP_BEHAVIOR_MAX;
        }
    };
    
    /**
     * Set the app config module 
     * 
     * @param   delegate  [in] Refer to {@link AppBehaviorManagerDelegate}.
     *
     * @return  {@link VO_OSMP_RETURN_CODE#VO_OSMP_ERR_NONE} if successful.
     */
    VO_OSMP_RETURN_CODE setDelegate(AppBehaviorManagerDelegate delegate);
    
    /**
     * Load the appcfg.xml through the file path 
     *
     * @param   filePath   [in] the file path of appcfg.xml.
     *
     * @return  {@link VO_OSMP_RETURN_CODE#VO_OSMP_ERR_NONE} if successful.
     */
    ArrayList<OptionItem> loadCfgFile(String filePath);
    
    /**
     * Process the events
     *
     * @param   eventId   [in] the id of the event.
     *
     * @param   param1   [in] the param1 of the event.
     * 
     * @param   param2   [in] the param2 of the event.
     * 
     * @param   param3   [in] the param3 of the event.
     *
     * @return  0:continue; 1:return.
     */
    int processEvent(int eventId, int param1, int param2, Object param3);
    
    /**
     * Process the events
     *
     * @param   apiName   [in] the name of api.
     * 
     * @param   returnCode   [in] the id of the return code.
     *
     * @return  0:continue; 1:return.
     */
    int processReturnCode(String apiName, int returnCode);
    
    
    /**
     * Show/Hide option page
     *
     * @param   show   [in] Show/Hide; true to show,false to hide.
     *
     * @param   item   [in] the item of the option.
     *
     * @return  {@link VO_OSMP_RETURN_CODE#VO_OSMP_ERR_NONE} if successful.
     */
    VO_OSMP_RETURN_CODE showOptionPage(boolean show);
    
    /**
     * Get the option item by ID
     *
     * @param   optionId   [in] the id of option item.
     *
     * @return   item property {@link OptionItem} object if successful or null if failed.
     */
    OptionItem getOptionItemByID(int optionId);
    
   
    
    /**
     * Uninit the app config module 
     *
     * @return  {@link VO_OSMP_RETURN_CODE#VO_OSMP_ERR_NONE} if successful.
     */
    VO_OSMP_RETURN_CODE uninit();
    
    public interface BaseItem {
        
        public void setId(long id);
        public void setEnable(boolean enable);
        public void setType(int type);
        public void setAppBehavior(APP_BEHAVIOR_EVENT_ID behaviorType);
        public void setDescription(String description);
        public void setPlatform(int platform);
        public void addChildItem(Object item);
        
        public long getId();
        public boolean getEnable();
        public int getType();
        public APP_BEHAVIOR_EVENT_ID getAppBehavior();
        public String getDescription();
        public int getChildCount();
        public int getPlatform();
        public Object getChild(int index);
    }
}