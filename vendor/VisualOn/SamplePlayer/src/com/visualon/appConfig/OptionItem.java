/************************************************************************
VisualOn Proprietary
Copyright (c) 2012, VisualOn Incorporated. All Rights Reserved

VisualOn, Inc., 4675 Stevens Creek Blvd, Santa Clara, CA 95051, USA

All data and information contained in or disclosed by this document are
confidential and proprietary information of VisualOn, and all rights
therein are expressly reserved. By accepting this material, the
recipient agrees that this material and the information contained
therein are held in confidence and in trust. The material may only be
used and/or disclosed as authorized in a license agreement controlling
such use and disclosure.
************************************************************************/

package com.visualon.appConfig;

import java.util.ArrayList;

public class OptionItem extends BaseItemImpl{

    private String title = null;
    private int opEenable = 0;
    private ArrayList<ValueListItem>  mValueList = null;
    private int select = 0;
    private int opUIType = 0;
    private OptionItem parentItem = null;
    
    OptionItem() {
        super();
        mValueList = new ArrayList<ValueListItem>();
    }
    
//    public void setOptionEnable(int enable) {
//        this.opEenable = enable;
//    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setSelect(int select) {
        this.select = select;
    }
    
    public void setUIType(int type) {
        this.opUIType = type;
    }
    
//    public int getOptionEnable() {
//        return opEenable;
//    }
    
    public String getTitle() {
        return title;
    }
    
    public int getSelect() {
        return select;
    }
    
    public int getUIType() {
        return opUIType;
    }
    
    public int getValueListItemCount() {
        return mValueList.size();
    }
    
    public ValueListItem getValueListItem(int index) {
        return mValueList.get(index);
    }
    
    public void addValueListItem(ValueListItem valueListItem) {
        mValueList.add(valueListItem);
    }
    
    public void addParentItem(OptionItem item) {
        parentItem = item;
    }
    
    public OptionItem getParentItem() {
        return parentItem;
    }
    
    class ValueListItem {
        
        String title = null;
        int value = 0;
        int platform = 0;
        int maxValue = 0;
        int minValue = 0;
        String text  = null;
        void setTitle(String title) {
            this.title = title;
        }
        
        void setValue(int value) {
            this.value = value;
        }
        void setText(String text) {
            this.text = text;
        }
        void setPlatform(int platform) {
            this.platform = platform;
        }
        
        public void setMaxValue(int max) {
            this.maxValue = max;
        }
        
        public void setMinValue(int min) {
            this.minValue = min;
        }
        
        String getTitle() {
            return title;
        }
        
        int getValue() {
            return value;
        }
        String getText() {
            return text;
        }
        int getPlatform() {
            return platform;
        }
        
        public int getMaxValue() {
            return maxValue;
        }
        
        public int getMinValue() {
            return minValue;
        }
    }

}