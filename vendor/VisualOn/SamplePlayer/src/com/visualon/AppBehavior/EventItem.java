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

public class EventItem extends BaseItemImpl {

    private ArrayList<Param> param1s = null;
    private ArrayList<Param> param2s = null;
    
    public EventItem(){
        super();
        param1s = new ArrayList<Param>();
        param2s = new ArrayList<Param>();
    }
    
    public void addParam1(Param param) {
        param1s.add(param);
    }
    
    public void addParam2(Param param) {
        param2s.add(param);
    }
    
    public int getParam1Count() {
        return param1s.size();
    }
    
    public int getParam2Count() {
        return param2s.size();
    }
    
    public Param getParam1(int index) {
        return param1s.get(index);
    }
    
    public Param getParam2(int index) {
        return param2s.get(index);
    }
    
    class Param {
        long value = 0;
        String description = null;
        
        void setParamValue(long value) {
            this.value = value;
        }
        void setDescription(String description) {
            this.description = description;
        }
        long getParamValue() {
            return value;
        }
        String getDescription() {
            return description;
        }
    }
}