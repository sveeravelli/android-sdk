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
  private String apiFramework;

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

    String xString = xml.getAttribute(VASTAd.ATTRIBUTE_XPOSITION);
    if ("left".equals(xString)) {
      xPosition = 0;
    } else if ("right".equals(xString)) {
      xPosition = Integer.MAX_VALUE;
    } else {
      xPosition = VASTUtils.getIntAttribute(xml, VASTAd.ATTRIBUTE_XPOSITION, 0);
    }

    String yString = xml.getAttribute(VASTAd.ATTRIBUTE_YPOSITION);
    if ("top".equals(yString)) {
      yPosition = 0;
    } else if ("bottom".equals(yString)) {
      yPosition = Integer.MAX_VALUE;
    } else {
      yPosition = VASTUtils.getIntAttribute(xml, VASTAd.ATTRIBUTE_YPOSITION, 0);
    }

    duration = VASTUtils.secondsFromTimeString(xml.getAttribute(VASTAd.ATTRIBUTE_DURATION), 0);
    offset = VASTUtils.secondsFromTimeString(xml.getAttribute(VASTAd.ATTRIBUTE_OFFSET), 0);
    apiFramework = xml.getAttribute(VASTAd.ATTRIBUTE_API_FRAMEWORK);

    Node child = xml.getFirstChild();
    while (child != null) {
      if (child instanceof Element) {
        Element e = (Element)child;
        String tag = e.getTagName();
        if (tag.equals(VASTAd.ELEMENT_STATIC_RESOURCE)) {
          type = ResourceType.Static;
          creativeType = e.getAttribute(VASTAd.ATTRIBUTE_CREATIVE_TYPE);
          resourceUrl = e.getTextContent().trim();
        } else if (tag.equals(VASTAd.ELEMENT_IFRAME_RESOURCE)) {
          type = ResourceType.IFrame;
          resourceUrl = e.getTextContent().trim();
        } else if (tag.equals(VASTAd.ELEMENT_HTML_RESOURCE)) {
          type = ResourceType.HTML;
          resourceUrl = e.getTextContent().trim();
        } else if (tag.equals(VASTAd.ELEMENT_ICON_VIEW_TRACKING)) {
          String viewTracking = e.getTextContent().trim();
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
          clickThrough = e.getTextContent().trim();
        } else if (tag.equals(VASTAd.ELEMENT_ICON_CLICK_TRACKING)) {
          String tracking = e.getTextContent().trim();
          if (tracking != null && tracking.length() > 0) {
            clickTrackings.add(tracking);
          }
        }
      }
      child = child.getNextSibling();
    }
  }

  /**
   * @return program
   */
  public String getProgram() {
    return program;
  }

  /**
   * @return width in pixels
   */
  public int getWidth() {
    return width;
  }

  /**
   * @return height in pixels
   */
  public int getHeight() {
    return height;
  }

  /**
   * @return xPosition in pixels
   */
  public int getXPosition() {
    return xPosition;
  }

  /**
   * @return yPosition in pixels
   */
  public int getYPosition() {
    return yPosition;
  }

  /**
   * @return duration in seconds
   */
  public double getDuration() {
    return duration;
  }

  /**
   * @return time offset in seconds
   */
  public double getOffset() {
    return offset;
  }

  /**
   * @return resource url
   */
  public String getResourceUrl() {
    return resourceUrl;
  }

  /**
   * @return resource type
   */
  public ResourceType getResourceType() {
    return type;
  }

  /**
   * @return creative type for static resource
   */
  public String getCreativeType() {
    return creativeType;
  }

  /**
   * @return api framework
   */
  public String getApiFramework() {
    return apiFramework;
  }

  /**
   * @return a list of click tracking urls
   */
  public List<String> getClickTrackings() {
    return clickTrackings;
  }

  /**
   * @return a list of viewing urls
   */
  public List<String> getViewTrackings() {
    return viewTrackings;
  }

  /**
   * @return click through url
   */
  public String getClickThrough() {
    return clickThrough;
  }
}
