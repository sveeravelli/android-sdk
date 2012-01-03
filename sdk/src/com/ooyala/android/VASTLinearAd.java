package com.ooyala.android;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class VASTLinearAd implements PlayableItem {
  private double _duration;                                                                /**< The duration of the ad in seconds */
  private HashMap<String,Set<String>> _trackingEvents = new HashMap<String,Set<String>>(); /**< The tracking events in a HashMap of event name to Set of String */
  private String _parameters;                                                              /**< The additional ad parameters */
  private String _clickThroughURL;                                                         /**< The click through url */
  private Set<String> _clickTrackingURLs = new HashSet<String>();                          /**< The click tracking urls in an Set of String */
  private Set<String> _customClickURLs = new HashSet<String>();                            /**< The custom click urls in an Set of String */
  private Set<Stream> _streams = new HashSet<Stream>();                                    /**< The streams in an HashSet of Stream */          

  /** @internal
   * Initialize a VASTLinearAd using the specified xml (subclasses should override this)
   * @param[in] data the Element containing the xml to use to initialize this VASTLinearAd
   * @returns the initialized VASTLinearAd
   */
  public VASTLinearAd(Element data) {
    if (!data.getTagName().equals(Constants.ELEMENT_LINEAR)) { return; }
    Node child = data.getFirstChild();
    while (child != null) {
      if (!(child instanceof Element)) { child = child.getNextSibling(); continue; }
      if (!Utils.isNullOrEmpty(child.getTextContent()) && ((Element)child).getTagName().equals(Constants.ELEMENT_DURATION)) {
        _duration = Utils.secondsFromTimeString(child.getTextContent());
      } else if (!Utils.isNullOrEmpty(child.getTextContent()) && ((Element)child).getTagName().equals(Constants.ELEMENT_AD_PARAMETERS)) {
        _parameters = child.getTextContent();
      } else if (((Element)child).getTagName().equals(Constants.ELEMENT_TRACKING_EVENTS)) {
        Node trackingChild = child.getFirstChild();
        while (trackingChild != null) {
          if (!(trackingChild instanceof Element) || Utils.isNullOrEmpty(trackingChild.getTextContent())) { trackingChild = trackingChild.getNextSibling(); continue; }
          String event = ((Element)trackingChild).getAttribute(Constants.ATTRIBUTE_EVENT);
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
      } else if (((Element)child).getTagName().equals(Constants.ELEMENT_VIDEO_CLICKS)) {
        Node clickChild = child.getFirstChild();
        while (clickChild != null) {
          if (!(clickChild instanceof Element) || Utils.isNullOrEmpty(clickChild.getTextContent())) { clickChild = clickChild.getNextSibling(); continue; }
          if (((Element)clickChild).getTagName().equals(Constants.ELEMENT_CLICK_THROUGH)) {
            _clickThroughURL = clickChild.getTextContent();
          } else if (((Element)clickChild).getTagName().equals(Constants.ELEMENT_CLICK_TRACKING)) {
            _clickTrackingURLs.add(clickChild.getTextContent());
          } else if (((Element)clickChild).getTagName().equals(Constants.ELEMENT_CUSTOM_CLICK)) {
            _customClickURLs.add(clickChild.getTextContent());
          }
          clickChild = clickChild.getNextSibling();
        }
      } else if (((Element)child).getTagName().equals(Constants.ELEMENT_MEDIA_FILES)) {
        Node fileChild = child.getFirstChild();
        while (fileChild != null) {
          if (!(fileChild instanceof Element)) { fileChild = fileChild.getNextSibling(); continue; }
          VASTStream stream = new VASTStream((Element)fileChild);
          _streams.add(stream);
          fileChild = fileChild.getNextSibling();
        }
      }
      child = child.getNextSibling();
    }
  }

  /** @internal
   * Return the stream to play
   * @returns The hls stream if it exists, otherwise the lowest bitrate mp4 stream
   */
  @Override
  public Stream getStream() {
    return Stream.bestStream(_streams);
  }

  public double getDuration() {
    return _duration;
  }

  public void setDuration(double duration) {
    this._duration = duration;
  }

  public HashMap<String, Set<String>> getTrackingEvents() {
    return _trackingEvents;
  }

  public void setTrackingEvents(
      HashMap<String, Set<String>> trackingEvents) {
    this._trackingEvents = trackingEvents;
  }

  public String getParameters() {
    return _parameters;
  }

  public void setParameters(String parameters) {
    this._parameters = parameters;
  }

  public String getClickThroughURL() {
    return _clickThroughURL;
  }

  public void setClickThroughURL(String clickThroughURL) {
    this._clickThroughURL = clickThroughURL;
  }

  public Set<String> getClickTrackingURLs() {
    return _clickTrackingURLs;
  }

  public void setClickTrackingURLs(Set<String> clickTrackingURLs) {
    this._clickTrackingURLs = clickTrackingURLs;
  }

  public Set<String> getCustomClickURLs() {
    return _customClickURLs;
  }

  public void setCustomClickURLs(Set<String> customClickURLs) {
    this._customClickURLs = customClickURLs;
  }

  public Set<Stream> getStreams() {
    return _streams;
  }

  public void setStreams(Set<Stream> streams) {
    this._streams = streams;
  }

}
