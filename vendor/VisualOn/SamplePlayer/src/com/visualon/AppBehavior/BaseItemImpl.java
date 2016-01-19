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

import com.visualon.AppBehavior.AppBehaviorManager.APP_BEHAVIOR_EVENT_ID;
import com.visualon.AppBehavior.AppBehaviorManager.BaseItem;

public class BaseItemImpl implements BaseItem {

    private long id = 0;
    private boolean enable = false;
    private int actionType = 0;
    private String description = null;
    private int platform = 0xFFFF;
    private ArrayList<Object> childItemList = null;
    private APP_BEHAVIOR_EVENT_ID appBehavior = null;
    
    public BaseItemImpl(){
        super();
        childItemList = new ArrayList<Object>();
    }
    
    @Override
    public void setId(long id) {
        // TODO Auto-generated method stub
        this.id = id;
    }

    @Override
    public void setEnable(boolean enable) {
        // TODO Auto-generated method stub
        this.enable = enable;
    }

    @Override
    public void setType(int type) {
        // TODO Auto-generated method stub
        this.actionType = type;
    }

    @Override
    public void setDescription(String description) {
        // TODO Auto-generated method stub
        this.description = description;
    }


    @Override
    public void setAppBehavior(APP_BEHAVIOR_EVENT_ID behaviorType) {
        // TODO Auto-generated method stub
        this.appBehavior = behaviorType;
        
    }
    
    @Override
    public void setPlatform(int platform) {
        // TODO Auto-generated method stub
        this.platform = platform;
    }
    
    @Override
    public long getId() {
        // TODO Auto-generated method stub
        return id;
    }

    @Override
    public boolean getEnable() {
        // TODO Auto-generated method stub
        return enable;
    }

    @Override
    public int getType() {
        // TODO Auto-generated method stub
        return actionType;
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return description;
    }

    
    @Override
    public APP_BEHAVIOR_EVENT_ID getAppBehavior() {
        // TODO Auto-generated method stub
        return appBehavior;
    }

    @Override
    public void addChildItem(Object item) {
        // TODO Auto-generated method stub
        childItemList.add(item);
    }

    @Override
    public int getChildCount() {
        // TODO Auto-generated method stub
        return childItemList.size();
    }

    @Override
    public Object getChild(int index) {
        // TODO Auto-generated method stub
        return childItemList.get(index);
    }

    @Override
    public int getPlatform() {
        // TODO Auto-generated method stub
        return platform;
    }
}