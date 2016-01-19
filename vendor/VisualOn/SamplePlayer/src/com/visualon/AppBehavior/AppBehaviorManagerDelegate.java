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

import com.visualon.AppBehavior.AppBehaviorManager.APP_BEHAVIOR_EVENT_ID;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;

public interface AppBehaviorManagerDelegate {

    /**
     * Protocol to process events. The client can implement the listener to receive/manage events.
     *
     * @param nID    [out] Event type. Refer to {@link APP_BEHAVIOR_EVENT_ID}.
     *
     * @return {@link VO_OSMP_ERR_NONE} if successful.
     */
    VO_OSMP_RETURN_CODE handleBehaviorEvent(APP_BEHAVIOR_EVENT_ID nID,String str);
}