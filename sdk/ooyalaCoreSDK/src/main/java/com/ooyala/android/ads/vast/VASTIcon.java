package com.ooyala.android.ads.vast;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zchen on 2/25/16.
 */
public class VASTIcon {
  private static final String TAG = VASTIcon.class.getSimpleName();
  public enum ResourceType {
    None,
    Static,
    IFrame,
    HTML
  };

  private String program;
  private int width;
  private int height;
  private int xPosition;
  private int yPosition;
  private double offset;
  private double duration;
  private String resourceUrl;
  private String creativeType;
  private ResourceType type;

  private List<String> clickTrackings = new ArrayList<String>();
  private List<String> viewTrackings = new ArrayList<String>();
  String clickThrough;

  VASTIcon(Element data) {
    if (data == null || !data.getTagName().equals(VASTAd.ELEMENT_ICON)) {
      return;
    }
    type = ResourceType.None;
    parseXml(data);
  }

  private void parseXml(Element xml) {
    program = xml.getAttribute(VASTAd.ATTRIBUTE_PROGRAM);
    width = VASTUtils.getIntAttribute(xml, VASTAd.ATTRIBUTE_WIDTH, 0);
    height = VASTUtils.getIntAttribute(xml, VASTAd.ATTRIBUTE_HEIGHT, 0);
    xPosition = VASTUtils.getIntAttribute(xml, VASTAd.ATTRIBUTE_XPOSITION, 0);
    yPosition = VASTUtils.getIntAttribute(xml, VASTAd.ATTRIBUTE_YPOSITION, 0);
    duration = VASTUtils.secondsFromTimeString(xml.getAttribute(VASTAd.ATTRIBUTE_DURATION), 0);
    offset = VASTUtils.secondsFromTimeString(xml.getAttribute(VASTAd.ATTRIBUTE_OFFSET), 0);

    Node child = xml.getFirstChild();
    while (child != null) {
      if (child instanceof Element) {
        Element e = (Element)child;
        String tag = e.getTagName();
        if (tag.equals(VASTAd.ELEMENT_STATIC_RESOURCE)) {
          type = ResourceType.Static;
          creativeType = e.getAttribute(VASTAd.ATTRIBUTE_CREATIVE_TYPE);
          resourceUrl = e.getTextContent();
        } else if (tag.equals(VASTAd.ELEMENT_IFRAME_RESOURCE)) {
          type = ResourceType.IFrame;
          resourceUrl = e.getTextContent();
        } else if (tag.equals(VASTAd.ELEMENT_HTML_RESOURCE)) {
          type = ResourceType.HTML;
          resourceUrl = e.getTextContent();
        } else if (tag.equals(VASTAd.ELEMENT_ICON_VIEW_TRACKING)) {
          String viewTracking = e.getTextContent();
          if (viewTracking != null && viewTracking.length() > 0) {
            viewTrackings.add(viewTracking);
          }
        } else if (tag.equals(VASTAd.ELEMENT_ICON_CLICKS)) {
          parseClicks(e);
        }
      }
      child = child.getNextSibling();
    }
  }

  private void parseClicks(Element xml) {
    Node child = xml.getFirstChild();
    while (child != null) {
      if (child instanceof Element) {
        Element e = (Element)child;
        String tag = e.getTagName();
        if (tag.equals(VASTAd.ELEMENT_ICON_CLICK_THROUGH)) {
          clickThrough = e.getTextContent();
        } else if (tag.equals(VASTAd.ELEMENT_ICON_CLICK_TRACKING)) {
          String tracking = e.getTextContent();
          if (tracking != null && tracking.length() > 0) {
            clickTrackings.add(tracking);
          }
        }
      }
      child = child.getNextSibling();
    }
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getXPosition() {
    return xPosition;
  }

  public int getYPosition() {
    return yPosition;
  }

  public double getDuration() {
    return duration;
  }

  public double getOffset() {
    return offset;
  }

  public String getResourceUrl() {
    return resourceUrl;
  }

  public ResourceType getResourceType() {
    return type;
  }

  public String getCreativeType() {
    return creativeType;
  }

  public List<String> getClickTrackings() {
    return clickTrackings;
  }

  public List<String> getViewTrackings() {
    return viewTrackings;
  }

  public String getClickThrough() {
    return clickThrough;
  }
}
