package com.ooyala.android;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class VASTLinearAd implements PlayableItem {
  /** The duration of the ad in seconds */
  private double _duration;
  /** The tracking events in a HashMap of event name to Set of String */
  private HashMap<String, Set<String>> _trackingEvents = new HashMap<String, Set<String>>();
  /** The additional ad parameters */
  private String _parameters;
  /** The click through url */
  private String _clickThroughURL;
  /** The click tracking urls in an Set of String */
  private Set<String> _clickTrackingURLs = new HashSet<String>();
  /** The custom click urls in an Set of String */
  private Set<String> _customClickURLs = new HashSet<String>();
  /** The streams in an HashSet of Stream */
  private Set<Stream> _streams = new HashSet<Stream>();

  /**
   * Initialize a VASTLinearAd using the specified xml (subclasses should override this)
   * @param data the Element containing the xml to use to initialize this VASTLinearAd
   */
  VASTLinearAd(Element data) {
    if (!data.getTagName().equals(Constants.ELEMENT_LINEAR)) { return; }
    Node child = data.getFirstChild();
    while (child != null) {
      if (!(child instanceof Element)) {
        child = child.getNextSibling();
        continue;
      }
      if (!Utils.isNullOrEmpty(child.getTextContent())
          && ((Element) child).getTagName().equals(Constants.ELEMENT_DURATION)) {
        _duration = Utils.secondsFromTimeString(child.getTextContent());
      } else if (!Utils.isNullOrEmpty(child.getTextContent())
          && ((Element) child).getTagName().equals(Constants.ELEMENT_AD_PARAMETERS)) {
        _parameters = child.getTextContent();
      } else if (((Element) child).getTagName().equals(Constants.ELEMENT_TRACKING_EVENTS)) {
        Node trackingChild = child.getFirstChild();
        while (trackingChild != null) {
          if (!(trackingChild instanceof Element) || Utils.isNullOrEmpty(trackingChild.getTextContent())) {
            trackingChild = trackingChild.getNextSibling();
            continue;
          }
          String event = ((Element) trackingChild).getAttribute(Constants.ATTRIBUTE_EVENT);
          Set<String> urls = _trackingEvents.get(event);
          if (urls != null) {
            urls.add(trackingChild.getTextContent());
          } else {
            urls = new HashSet<String>();
            urls.add(trackingChild.getTextContent());
            _trackingEvents.put(event, urls);
          }
          trackingChild = trackingChild.getNextSibling();
        }
      } else if (((Element) child).getTagName().equals(Constants.ELEMENT_VIDEO_CLICKS)) {
        Node clickChild = child.getFirstChild();
        while (clickChild != null) {
          if (!(clickChild instanceof Element) || Utils.isNullOrEmpty(clickChild.getTextContent())) {
            clickChild = clickChild.getNextSibling();
            continue;
          }
          if (((Element) clickChild).getTagName().equals(Constants.ELEMENT_CLICK_THROUGH)) {
            _clickThroughURL = clickChild.getTextContent();
          } else if (((Element) clickChild).getTagName().equals(Constants.ELEMENT_CLICK_TRACKING)) {
            _clickTrackingURLs.add(clickChild.getTextContent());
          } else if (((Element) clickChild).getTagName().equals(Constants.ELEMENT_CUSTOM_CLICK)) {
            _customClickURLs.add(clickChild.getTextContent());
          }
          clickChild = clickChild.getNextSibling();
        }
      } else if (((Element) child).getTagName().equals(Constants.ELEMENT_MEDIA_FILES)) {
        Node fileChild = child.getFirstChild();
        while (fileChild != null) {
          if (!(fileChild instanceof Element)) {
            fileChild = fileChild.getNextSibling();
            continue;
          }
          VASTStream stream = new VASTStream((Element) fileChild);
          _streams.add(stream);
          fileChild = fileChild.getNextSibling();
        }
      }
      child = child.getNextSibling();
    }
  }

  /**
   * Return the stream to play
   * @return the lowest bitrate mp4 stream
   */
  @Override
  public Stream getStream() {
    return Stream.bestStream(_streams);
  }

  /**
   * Fetch the duration of this VASTLinearAd in seconds.
   * @return the duration in seconds.
   */
  public double getDuration() {
    return _duration;
  }

  /**
   * Fetch a HashMap containing the tracking events.
   * @return a HashMap containing the tracking events. The key is the event name and the value is a Set of
   *         Strings.
   */
  public HashMap<String, Set<String>> getTrackingEvents() {
    return _trackingEvents;
  }

  /**
   * Fetch the additional ad parameters.
   * @return the additional ad parameters.
   */
  public String getParameters() {
    return _parameters;
  }

  /**
   * Fetch the URL go to when the user clicks this ad.
   * @return the URL go to when the user clicks this ad.
   */
  public String getClickThroughURL() {
    return _clickThroughURL;
  }

  /**
   * Fetch the Set of URLs to ping when the user clicks this ad.
   * @return The Set of URLs to ping when the user clicks this ad.
   */
  public Set<String> getClickTrackingURLs() {
    return _clickTrackingURLs;
  }

  /**
   * Fetch the custom click URLs as a Set of Strings
   * @return the custom click URLs as a Set of Strings
   */
  public Set<String> getCustomClickURLs() {
    return _customClickURLs;
  }

  /**
   * Fetch all the streams associated with this VASTLinearAd.
   * @return a Set of Stream objects
   */
  public Set<Stream> getStreams() {
    return _streams;
  }
}
