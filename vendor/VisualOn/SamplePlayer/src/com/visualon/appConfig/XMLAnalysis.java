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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.visualon.appConfig.AppBehaviorManager.APP_BEHAVIOR_EVENT_ID;
import com.visualon.appConfig.EventItem.Param;
import com.visualon.appConfig.OptionItem.ValueListItem;

public class XMLAnalysis extends DefaultHandler {
    
    private EventItem eventItem = null;
    private EventItem downloadEventItem = null;
    private ReturnCodeItem returnCodeItem = null;
    private EventItem appEventItem        = null;
    private OptionItem optionItem = null;
    private Param param1 = null;
    private Param param2 = null;
    private ValueListItem valueListItem = null;
    private ArrayList<EventItem> appEventItems = null;
    private ArrayList<EventItem> eventItems = null;
    private ArrayList<EventItem> downloadEventItems = null;
    private ArrayList<ReturnCodeItem> returnCodeItems = null;
    private ArrayList<OptionItem> optionItems = null;
    private ArrayList<EventItem> tempEventItems = null;
    private ArrayList<ReturnCodeItem> tempRcItems = null;
    private ArrayList<OptionItem> tempOptionItems = null;
    private StringBuffer buffer = new StringBuffer();
    private boolean isItemPlatform = false;
    private int nodeLevel1          = -1;
//    private int nodeLevel2          = -1;
    private int nodeLevel3          = -1;
    private static final int       L1_NODE_OSMPEVENT        = 0;
    private static final int       L1_NODE_DOWNLOADEVENT    = 1;
    private static final int       L1_NODE_RETURNCODE       = 2;
    private static final int       L1_NODE_OPTION           = 3;
    private static final int       L1_NODE_APPEVENT         = 4;
    private static final int       L3_NODE_PARAM1           = 0;
    private static final int       L3_NODE_PARAM2           = 1;
    
    private static final String    APPEVENT             = "AppEvent";
    private static final String    OSMPEVENT            = "OSMPEvent";
    private static final String    DOWNLOADEVENT        = "DownloadEvent";
    private static final String    RETURNCODE           = "ReturnCode";
    private static final String    OPTION               = "Option";
    private static final String    EVENT                = "Event";
    private static final String    UIEVENT              = "UIEvent";
    private static final String    DLEVENT              = "DLEvent";
    private static final String    RC                   = "RC";
    private static final String    OPTIONITEM           = "Optionitem";
    private static final String    PARAM1               = "Param1";
    private static final String    PARAM2               = "Param2";
    private static final String    ITEM                 = "item";
    private static final String    EVENTID              = "EventId";
    private static final String    ENABLE               = "Enable";
    private static final String    ACTION               = "Action";
    private static final String    APPBEHAVIOR          = "AppBehavior";
    private static final String    DESCRIPTION          = "Description";
    private static final String    ERRORCODE            = "ErrorCode";
    private static final String    VALUE                = "value";
    private static final String    OPTIONID             = "OptionId";
    private static final String    TITLE                = "Title";
    private static final String    UITYPE               = "UIType";
    private static final String    VALUELIST            = "ValueList";
    private static final String    ITEMTITLE            = "ItemTitle";
    private static final String    OPTIONITEMVALUE      = "Value";
    private static final String    ITEMTEXT             = "Text";
    private static final String    PLATFORM             = "Platform";
    private static final String    SELECTED             = "Selected";
    private static final String    MAXVALUE             = "MaxValue";
    private static final String    MINVALUE             = "MinValue";
    
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        // TODO Auto-generated method stub
        buffer.append(ch, start, length);
        super.characters(ch, start, length);
    }

    @Override
    public void endDocument() throws SAXException {
        // TODO Auto-generated method stub
        super.endDocument();
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        // TODO Auto-generated method stub
        if(nodeLevel1 == L1_NODE_OSMPEVENT) { //Event
            
            fillEventInfo(localName,eventItem);
            
        } else if(nodeLevel1 == L1_NODE_DOWNLOADEVENT) { //Download Event
            
            fillEventInfo(localName,downloadEventItem);
            
        } else if(nodeLevel1 == L1_NODE_RETURNCODE) { //Return Code
            
            fillRCInfo(localName,returnCodeItem);
            
        } else if(nodeLevel1 == L1_NODE_OPTION) { //Option
            
            fillOptionInfo(localName,optionItem);
            
        } else if(nodeLevel1 == L1_NODE_APPEVENT)
            fillEventInfo(localName,appEventItem);
        buffer.setLength(0);
        super.endElement(uri, localName, qName);
    }

    private void fillEventInfo(String localName, EventItem item) {
        if(localName.equals(EVENTID)) {
            
            String temp = buffer.toString().trim();
            temp = temp.substring(2, temp.length());
            item.setId(Long.parseLong(temp, 16));
            
        } else if(localName.equals(ENABLE)) {
            
            item.setEnable(Integer.parseInt(buffer.toString().trim()) == 0? false:true);
            
        } else if(localName.equals(ACTION)) {
            
            item.setType(Integer.parseInt(buffer.toString().trim()));
            
        } else if(localName.equals(APPBEHAVIOR)) {
            
            item.setAppBehavior(APP_BEHAVIOR_EVENT_ID.valueOf(Integer.parseInt(buffer.toString().trim())));
            
        } else if(localName.equals(DESCRIPTION)) {
            
            item.setDescription(buffer.toString().trim());
            
        } else if(localName.equals(ITEM)) {
            
            if(nodeLevel3 == L3_NODE_PARAM1) {
                param1.setDescription(buffer.toString().trim());
                item.addParam1(param1);
            } else if(nodeLevel3 == L3_NODE_PARAM2) {
                param2.setDescription(buffer.toString().trim());
                item.addParam2(param2);
            }
            
        } else if(localName.equals(EVENT)) {
            
            int tempListSize = tempEventItems.size();
            if(tempListSize > 1)
            {
                tempEventItems.get(tempListSize-2).addChildItem(tempEventItems.get(tempListSize-1));
                tempEventItems.remove(tempEventItems.get(tempListSize-1));
            }
            else
            {
                eventItems.add(tempEventItems.get(0));
                tempEventItems.remove(0);
            }
            
        } else if(localName.equals(DLEVENT)) {
            
            int tempListSize = tempEventItems.size();
            if(tempListSize > 1)
            {
                tempEventItems.get(tempListSize-2).addChildItem(tempEventItems.get(tempListSize-1));
                tempEventItems.remove(tempEventItems.get(tempListSize-1));
            }
            else
            {
                downloadEventItems.add(tempEventItems.get(0));
                tempEventItems.remove(0);
            }
        }else if(localName.equals(UIEVENT)) {
            
            int tempListSize = tempEventItems.size();
            if(tempListSize > 1)
            {
                tempEventItems.get(tempListSize-2).addChildItem(tempEventItems.get(tempListSize-1));
                tempEventItems.remove(tempEventItems.get(tempListSize-1));
            }
            else
            {
                appEventItems.add(tempEventItems.get(0));
                tempEventItems.remove(0);
            }
        }
    }

    private void fillRCInfo(String localName, ReturnCodeItem item)
    {
        if(localName.equals(ERRORCODE)) {
            
            String temp = buffer.toString().trim();
            temp = temp.substring(2, temp.length());
            item.setId(Long.parseLong(temp, 16));
            
        } else if(localName.equals(ENABLE)) {
            
            item.setEnable(Integer.parseInt(buffer.toString().trim()) == 0? false:true);
            
        } else if(localName.equals(ACTION)) {
            
            item.setType(Integer.parseInt(buffer.toString().trim()));
            
        } else if(localName.equals(APPBEHAVIOR)) {
            
            item.setAppBehavior(APP_BEHAVIOR_EVENT_ID.valueOf(Integer.parseInt(buffer.toString().trim())));
            
        } else if(localName.equals(DESCRIPTION)) {
            
            item.setDescription(buffer.toString().trim());
            
        } else if(localName.equals(RC)) {
            
            int tempListSize = tempRcItems.size();
            if(tempListSize > 1)
            {
                tempRcItems.get(tempListSize-2).addChildItem(tempRcItems.get(tempListSize-1));
                tempRcItems.remove(tempRcItems.get(tempListSize-1));
            }
            else
            {
                returnCodeItems.add(tempRcItems.get(0));
                tempRcItems.remove(0);
            }
        }
    }
    
    private void fillOptionInfo(String localName, OptionItem item) {
        if(localName.equals(OPTIONID))
        {
            
            optionItem.setId(Integer.parseInt(buffer.toString().trim()));
            
        } else if(localName.equals(ENABLE)) {
            
            optionItem.setEnable(Integer.parseInt(buffer.toString().trim()) == 0? false:true);
            
        } else if(localName.equals(TITLE)) {

            optionItem.setTitle(buffer.toString().trim());
            
        } else if(localName.equals(DESCRIPTION)) {
            
            optionItem.setDescription(buffer.toString().trim());
            
        } else if(localName.equals(UITYPE)) {
            
            optionItem.setUIType(Integer.parseInt(buffer.toString().trim()));
            
        } else if(localName.equals(VALUELIST)) {
            
//            optionItem.addValueListItem(valueListItem);
            
        } else if(localName.equals(ITEM)) {
            
            optionItem.addValueListItem(valueListItem);
            
        } else if(localName.equals(ITEMTITLE)) {
            
            valueListItem.setTitle(buffer.toString().trim());
            
        } else if(localName.equals(OPTIONITEMVALUE)) {
            
            valueListItem.setValue(Integer.parseInt(buffer.toString().trim()));
            
        } else if(localName.equals(ITEMTEXT)) {
           
            valueListItem.setText(buffer.toString().trim());
            
        } 
        else if(localName.equals(PLATFORM)) {
            
            if(isItemPlatform)
            {
                valueListItem.setPlatform(Integer.parseInt(buffer.toString().trim()));
                isItemPlatform = false;
            }
            else
            {
                optionItem.setPlatform(Integer.parseInt(buffer.toString().trim()));
            }
            
        } else if(localName.equals(MAXVALUE)) {
            
            valueListItem.setMaxValue(Integer.parseInt(buffer.toString().trim()));
            
        } else if(localName.equals(MINVALUE)) {
            
            valueListItem.setMinValue(Integer.parseInt(buffer.toString().trim()));
            
        } else if(localName.equals(SELECTED)) {
            
            optionItem.setSelect(Integer.parseInt(buffer.toString().trim()));
            
        } else if(localName.equals(OPTIONITEM)) {
            
            int tempListSize = tempOptionItems.size();
            if(tempListSize > 1)
            {
                tempOptionItems.get(tempListSize-1).addParentItem(tempOptionItems.get(tempListSize-2));
                tempOptionItems.get(tempListSize-2).addChildItem(tempOptionItems.get(tempListSize-1));
                tempOptionItems.remove(tempOptionItems.get(tempListSize-1));
            }
            else
            {
                optionItems.add(tempOptionItems.get(0));
                tempOptionItems.remove(0);
            }
            
        } 
    }
    
    @Override
    public void startDocument() throws SAXException {
        tempEventItems = new ArrayList<EventItem>();
        tempRcItems = new ArrayList<ReturnCodeItem>();
        tempOptionItems = new ArrayList<OptionItem>();
        super.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if(localName.equals(OSMPEVENT)) {
            
            eventItems = new ArrayList<EventItem>();
            nodeLevel1 = L1_NODE_OSMPEVENT;
            
        } else if(localName.equals(DOWNLOADEVENT)) {
            
            downloadEventItems = new ArrayList<EventItem>();
            nodeLevel1 = L1_NODE_DOWNLOADEVENT;
            
        }else if(localName.equals(APPEVENT)) {
            
            appEventItems = new ArrayList<EventItem>();
            nodeLevel1 = L1_NODE_APPEVENT;
            
        } 
        else if(localName.equals(RETURNCODE)) {
            
            returnCodeItems = new ArrayList<ReturnCodeItem>();
            nodeLevel1 = L1_NODE_RETURNCODE;
            
        } else if(localName.equals(OPTION)) {
            
            optionItems = new ArrayList<OptionItem>();
            nodeLevel1 = L1_NODE_OPTION;
            
        } else if(localName.equals(EVENT)) {
            
            eventItem = new EventItem();
            tempEventItems.add(eventItem);
            
        } else if(localName.equals(DLEVENT)) {
            
            downloadEventItem = new EventItem();
            tempEventItems.add(downloadEventItem);
            
        }else if(localName.equals(UIEVENT)) {
            
            appEventItem = new EventItem();
            tempEventItems.add(appEventItem);
            
        }
        else if(localName.equals(RC)) {
            
            returnCodeItem = new ReturnCodeItem();
            tempRcItems.add(returnCodeItem);
            
        } else if(localName.equals(OPTIONITEM)) {
            
            optionItem = new OptionItem();
            tempOptionItems.add(optionItem);
            
        } else if(localName.equals(PARAM1)) {
            
            nodeLevel3 = L3_NODE_PARAM1;
            
        } else if(localName.equals(PARAM2)) {
            
            nodeLevel3 = L3_NODE_PARAM2;
            
        } else if(localName.equals(ITEM)) {
            if(nodeLevel1 == L1_NODE_OSMPEVENT || nodeLevel1 == L1_NODE_DOWNLOADEVENT||
                    nodeLevel1 == L1_NODE_APPEVENT)
            {
                if(nodeLevel3 == L3_NODE_PARAM1) {
                    
                    param1 = eventItem.new Param();
                    param1.setParamValue(Long.parseLong(attributes.getValue(VALUE)));
                    
                } else if(nodeLevel3 == L3_NODE_PARAM2) {
                    
                    param2 = eventItem.new Param();
                    param2.setParamValue(Long.parseLong(attributes.getValue(VALUE)));
                    
                } 
            }else if(nodeLevel1 == L1_NODE_OPTION) {
                isItemPlatform = true;
                valueListItem = optionItem.new ValueListItem();
                
            }
        }
        
            
        super.startElement(uri, localName, qName, attributes);
    }
    
    public ArrayList<EventItem> getEventItems() {
        return eventItems;
    }
    
    public ArrayList<EventItem> getDownloadEventItems() {
        return downloadEventItems;
    }
    public ArrayList<EventItem> getAppEventItems() {
        return appEventItems;
    }
    public ArrayList<ReturnCodeItem> getReturnCodeItems() {
        return returnCodeItems;
    }
    
    public ArrayList<OptionItem> getOptionItems() {
        return optionItems;
    }
}