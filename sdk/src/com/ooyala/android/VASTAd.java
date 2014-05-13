package com.ooyala.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import android.util.Log;

public class VASTAd {
  static final double MINIMUM_SUPPORTED_VAST_VERSION = 2.0;
  static final String ELEMENT_VAST = "VAST";
  static final String ELEMENT_AD = "Ad";
  static final String ELEMENT_IN_LINE = "InLine";
  static final String ELEMENT_WRAPPER = "Wrapper";
  static final String ELEMENT_AD_SYSTEM = "AdSystem";
  static final String ELEMENT_AD_TITLE = "AdTitle";
  static final String ELEMENT_DESCRIPTION = "Description";
  static final String ELEMENT_SURVEY = "Survey";
  static final String ELEMENT_ERROR = "Error";
  static final String ELEMENT_IMPRESSION = "Impression";
  static final String ELEMENT_CREATIVES = "Creatives";
  static final String ELEMENT_CREATIVE = "Creative";
  static final String ELEMENT_LINEAR = "Linear";
  static final String ELEMENT_NON_LINEAR_ADS = "NonLinearAds";
  static final String ELEMENT_COMPANION_ADS = "CompanionAds";
  static final String ELEMENT_EXTENSIONS = "Extensions";
  static final String ELEMENT_DURATION = "Duration";
  static final String ELEMENT_TRACKING_EVENTS = "TrackingEvents";
  static final String ELEMENT_TRACKING = "Tracking";
  static final String ELEMENT_AD_PARAMETERS = "AdParameters";
  static final String ELEMENT_VIDEO_CLICKS = "VideoClicks";
  static final String ELEMENT_CLICK_THROUGH = "ClickThrough";
  static final String ELEMENT_CLICK_TRACKING = "ClickTracking";
  static final String ELEMENT_CUSTOM_CLICK = "CustomClick";
  static final String ELEMENT_MEDIA_FILES = "MediaFiles";
  static final String ELEMENT_MEDIA_FILE = "MediaFile";
  static final String ELEMENT_VAST_AD_TAG_URI = "VASTAdTagURI";

  static final String ATTRIBUTE_VERSION = "version";
  static final String ATTRIBUTE_ID = "id";
  static final String ATTRIBUTE_SEQUENCE = "sequence";
  static final String ATTRIBUTE_EVENT = "event";
  static final String ATTRIBUTE_DELIVERY = "delivery";
  static final String ATTRIBUTE_TYPE = "type";
  static final String ATTRIBUTE_BITRATE = "bitrate";
  static final String ATTRIBUTE_WIDTH = "width";
  static final String ATTRIBUTE_HEIGHT = "height";
  static final String ATTRIBUTE_SCALABLE = "scalable";
  static final String ATTRIBUTE_MAINTAIN_ASPECT_RATIO = "maintainAspectRatio";
  static final String ATTRIBUTE_API_FRAMEWORK = "apiFramework";

  static final String MIME_TYPE_MP4 = "video/mp4";
  static final String MIME_TYPE_M3U8 = "application/x-mpegURL";
  static final String MIME_TYPE_WIDEVINE = "video/wvm";

  static final String KEY_SIGNATURE = "signature";
  static final String KEY_URL = "url";

  /** the ID of the Ad */
  private String _adID;
  /** the System */
  protected String _system;
  /** the System Version */
  protected String _systemVersion;
  /** the title of the Ad */
  protected String _title;
  /** the description of the Ad */
  protected String _description;
  /** the survey URLs of the Ad */
  protected List<String> _surveyURLs = new ArrayList<String>();
  /** the error URLs of the Ad */
  protected List<String> _errorURLs = new ArrayList<String>();
  /** the impression URLs of the Ad */
  protected List<String> _impressionURLs = new ArrayList<String>();
  /** the ordered sequence of the Ad (List of VASTSequenceItem) */
  protected List<VASTSequenceItem> _sequence = new ArrayList<VASTSequenceItem>();
  /** the extensions of the Ad */
  protected Element _extensions;
  /** the number of linear creatives without sequence numbers */
  private int _numOfLinear = 0;

  /**
   * Initialize a VASTAd using the specified xml (subclasses should override this)
   * @param data the Element containing the xml to use to initialize this VASTAd
   */
  VASTAd(Element data) {
    if (!data.getTagName().equals(ELEMENT_AD)) { return; }
    _adID = data.getAttribute(ATTRIBUTE_ID);
    update(data);
  }

  /**
   * Update the VASTAd using the specified xml (subclasses should override this)
   * Assumptions:
   *  1) There can be multiple wrappers (we set no limit on the redirects).
   *  2) If the wrapper has multiple linear creatives with sequence numbers AND the child has multiple linear creatives
   *     with corresponding sequence numbers, we put the right tracking events to the correct linear creatives of the child.
   *  3) If the wrapper has multiple linear creatives but the child's linear creatives do not have sequence numbers we assume that
   *     the order that the creatives are given is the order that we want to match with the wrapper's sequence numbers
   * @param xml the TBXMLElement containing the xml to use to update this VASTAd
   * @return YES if the XML was properly formatter, NO if not
   */
  boolean update(Element xml) {
    Node type = xml.getFirstChild();
    boolean found = false;
    while (type != null) {
      if (!(type instanceof Element)) {
        type = type.getNextSibling();
        continue;
      }
      boolean isInLine = ((Element) type).getTagName().equals(ELEMENT_IN_LINE);
      boolean isWrapper = ((Element) type).getTagName().equals(ELEMENT_WRAPPER);

      if (isWrapper) {
        found = true;

        //If it is a wrapper ad, create a new wrapper ad with the current xml. This will call the VASTAd() constructor which will in turn
        //call the overridden update function in VASTWrapperAd subclass. Then get the child ad's xml from the wrapper and call update on it
        VASTWrapperAd wrapperAd = new VASTWrapperAd(xml);
        update((Element) wrapperAd.getChildAdXML());

        //Add the impression URLs of the wrapper to the child
        _impressionURLs.addAll(wrapperAd.getImpressionURLs());

        //Go through the sequence array in the inline ad (the final child of all wrappers)
        for (VASTSequenceItem item : _sequence) {
          int currentSequenceNum = item.getNumber();
          //Loop through the wrapper's sequence to find the matching sequence number
          for (VASTSequenceItem wrapper : wrapperAd._sequence) {
            VASTLinearAd wrapperLinear = wrapper.getLinear();
            int wrapperSequenceNum = wrapper.getNumber();
            if (wrapperLinear != null && wrapperSequenceNum == currentSequenceNum) {
              //If the sequence numbers match, update the tracking events of child with the tracking events of the wrapper
              item.getLinear().updateTrackingEvents(wrapperLinear.getTrackingEvents());
              item.getLinear().updateClickTrackingURLs(wrapperLinear.getClickTrackingURLs());
            }
          }
        }
      } else if (isInLine) {
      	found = true;
        Node child = type.getFirstChild();
        while (child != null) {
          if (!(child instanceof Element)) {
            child = child.getNextSibling();
            continue;
          }
          String text = child.getTextContent().trim();
          boolean textExists = text != null;
          if (textExists && ((Element) child).getTagName().equals(ELEMENT_AD_SYSTEM)) {
            _system = text;
            _systemVersion = ((Element) child).getAttribute(ATTRIBUTE_VERSION);
          } else if (textExists && ((Element) child).getTagName().equals(ELEMENT_AD_TITLE)) {
            _title = text;
          } else if (textExists && ((Element) child).getTagName().equals(ELEMENT_DESCRIPTION)) {
            _description = text;
          } else if (textExists && ((Element) child).getTagName().equals(ELEMENT_SURVEY)) {
            _surveyURLs.add(text);
          } else if (textExists && ((Element) child).getTagName().equals(ELEMENT_ERROR)) {
            _errorURLs.add(text);
          } else if (textExists && ((Element) child).getTagName().equals(ELEMENT_IMPRESSION)) {
            _impressionURLs.add(text);
          } else if (((Element) child).getTagName().equals(ELEMENT_EXTENSIONS)) {
            _extensions = (Element) child;
          } else if (((Element) child).getTagName().equals(ELEMENT_CREATIVES)) {
            Node creative = child.getFirstChild();
            while (creative != null) {
              if (creative instanceof Element) {
                addCreative((Element) creative);
              }
              creative = creative.getNextSibling();
            }
            Collections.sort(_sequence);
          }
          child = child.getNextSibling();
        }
      } else {
        //If not inline nor wrapper, error
        Log.e(VASTAd.class.getName(), "Error ad is not a wrapper or inline ad");
      }
      type = type.getNextSibling();
    }
    return found;
  }

  /**
   * Add a Creative TBXMLElement NOTE: this assumes that the element is in fact a creative.
   * @param creative the creative to add
   */
  protected void addCreative(Element creative) {
    Node type = creative.getFirstChild();
    while (type != null) {
      if (type == null || !(type instanceof Element)) {
        type = type.getNextSibling();
        continue;
      };
      String sequenceNumStr = creative.getAttribute(ATTRIBUTE_SEQUENCE);
      VASTLinearAd ad = null;
      Element nonLinears = null;
      Element companions = null;
      if (((Element) type).getTagName().equals(ELEMENT_LINEAR)) {
        ad = new VASTLinearAd((Element) type);
      } else if (((Element) type).getTagName().equals(ELEMENT_NON_LINEAR_ADS)) {
        nonLinears = (Element) type;
      } else if (((Element) type).getTagName().equals(ELEMENT_COMPANION_ADS)) {
        companions = (Element) type;
      }
      if (ad == null && nonLinears == null && companions == null) { return; }

      //If there is a sequence number, check if we have seen this sequence number before and add to the sequence list
      if (sequenceNumStr != null && sequenceNumStr.length() > 0) {
        int sequenceNum = Integer.parseInt(sequenceNumStr);
        boolean added = false;
        for (VASTSequenceItem item : _sequence) {
          int currentSequenceNum = item.getNumber();

          //If the incoming creative is matched to an already found sequence item,
          //attach this creative to that sequence item
          if (currentSequenceNum == sequenceNum) {
            if (ad != null) {
              item.setLinear(ad);
            } else if (nonLinears != null) {
              item.setNonLinears(nonLinears);
            } else if (companions != null) {
              item.setCompanions(companions);
            }
            added = true;
            break;
          }
        }
        //If we could not find a matching sequence item, it doesn't exist yet so create a new sequence item and add to it
        if (!added) {
          VASTSequenceItem item = new VASTSequenceItem();
          item.setNumber(sequenceNum);
          if (ad != null) {
            item.setLinear(ad);
          } else if (nonLinears != null) {
            item.setNonLinears(nonLinears);
          } else if (companions != null) {
            item.setCompanions(companions);
          }
          _sequence.add(item);
        }
      } else {
        //If there is no sequence number, start the sequence from 1 and increment every time we see a linear ad
        //Non-linear and companion ads go into the most recent sequence item
        VASTSequenceItem item = new VASTSequenceItem();
        if (ad != null) {

          //If we have a sequence item that has no linear.
          if (_sequence.size() > _numOfLinear) {
            item = _sequence.get(_numOfLinear);
            item.setLinear(ad);
            _numOfLinear++;
          } else {
            _numOfLinear++;
            item.setNumber(_numOfLinear);
            item.setLinear(ad);
            _sequence.add(item);
          }

          //If we see a nonlinear before the first linear ad
        } else if (nonLinears != null) {
          if (_sequence.size() == 0) {
            item.setNumber(1);
            item.setNonLinears(nonLinears);
            _sequence.add(item);
          } else {
            item = _sequence.get(_sequence.size() - 1);
            item.setNonLinears(nonLinears);
          }

          //If we see a companion before the first linear ad
        } else if (companions != null) {
          if (_sequence.size() == 0) {
            item.setNumber(1);
            item.setCompanions(companions);
            _sequence.add(item);
          } else {
            item = _sequence.get(_sequence.size() - 1);
            item.setCompanions(companions);
          }
        }
      }
      type = type.getNextSibling();
    }
  }

  /**
   * Fetch the id of this VASTAd. This doesn't really mean anything.
   * @return the id of this VASTAd.
   */
  public String getAdID() {
    return _adID;
  }

  /**
   * Fetch the VASTAd's System. This is a String defining which ad provider this VASTAd uses.
   * @return the VASTAd's System.
   */
  public String getSystem() {
    return _system;
  }

  /**
   * Fetch the VASTAd's System's Version.
   * @return the version of the VASTAd's System.
   */
  public String getSystemVersion() {
    return _systemVersion;
  }

  /**
   * Fetch the title of this VASTAd.
   * @return the title of this VASTAd.
   */
  public String getTitle() {
    return _title;
  }

  /**
   * Fetch the description of this VASTAd.
   * @return the description of this VASTAd.
   */
  public String getDescription() {
    return _description;
  }

  /**
   * Fetch the list of Survey URLs associated with this VASTAd.
   * @return the list of Survey URLs associated with this VASTAd.
   */
  public List<String> getSurveyURLs() {
    return _surveyURLs;
  }

  /**
   * Fetch the list of URLs to ping when this VASTAd throws an error.
   * @return the list of URls to ping when this VASTAd throws an error.
   */
  public List<String> getErrorURLs() {
    return _errorURLs;
  }

  /**
   * Fetch the list of URLs to ping when this VASTAd plays.
   * @return the list of URLs to ping when this VASTAd plays.
   */
  public List<String> getImpressionURLs() {
    return _impressionURLs;
  }

  /**
   * Fetch the list of VASTSequenceItem objects that make up this VASTAd. This list defines the order to play
   * or show the various different VASTAd types such as linear, non-linear and companion ads.
   * @return the list of VASTSequenceItem objects.
   */
  public List<VASTSequenceItem> getSequence() {
    return _sequence;
  }

  /**
   * Fetch the raw XML Element object for the VAST Extensions associated with this VASTAd.
   * @return the Element object containing the VAST Extensions.
   */
  public Element getExtensions() {
    return _extensions;
  }
}
