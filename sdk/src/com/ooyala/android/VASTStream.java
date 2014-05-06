package com.ooyala.android;

import org.w3c.dom.Element;

import com.ooyala.android.item.Stream;

public class VASTStream extends Stream {
  /** if this stream is scalable */
  private boolean _scalable;
  /** if this stream must maintain the aspect ratio */
  private boolean _maintainAspectRatio;
  /** the vast delivery type of this stream */
  private String _vastDeliveryType;
  /** the apiFramework of this stream */
  private String _apiFramework;

  /**
   * Initialize a Stream using the specified VAST MediaFile XML (subclasses should override this)
   * @param data the Element containing the xml to use to initialize this Stream
   */
  VASTStream(Element data) {
    if (!data.getTagName().equals(VASTAd.ELEMENT_MEDIA_FILE)) { return; }
    this._vastDeliveryType = data.getAttribute(VASTAd.ATTRIBUTE_DELIVERY);
    this._apiFramework = data.getAttribute(VASTAd.ATTRIBUTE_API_FRAMEWORK);
    String scalableStr = data.getAttribute(VASTAd.ATTRIBUTE_SCALABLE);
    if (!Utils.isNullOrEmpty(scalableStr)) {
      this._scalable = Boolean.getBoolean(scalableStr);
    }
    String maintainAspectRatioStr = data.getAttribute(VASTAd.ATTRIBUTE_MAINTAIN_ASPECT_RATIO);
    if (maintainAspectRatioStr != null) {
      this._maintainAspectRatio = Boolean.getBoolean(maintainAspectRatioStr);
    }
    String type = data.getAttribute(VASTAd.ATTRIBUTE_TYPE);
    if (type != null) {
      if (type.equals(VASTAd.MIME_TYPE_M3U8)) {
        this._deliveryType = Stream.DELIVERY_TYPE_HLS;
      }
      if (type.equals(VASTAd.MIME_TYPE_MP4)) {
        this._deliveryType = Stream.DELIVERY_TYPE_MP4;
      } else {
        this._deliveryType = type;
      }
    }
    String bitrate = data.getAttribute(VASTAd.ATTRIBUTE_BITRATE);
    if (!Utils.isNullOrEmpty(bitrate)) {
      this._videoBitrate = Integer.parseInt(bitrate);
    }
    String theWidth = data.getAttribute(VASTAd.ATTRIBUTE_WIDTH);
    if (!Utils.isNullOrEmpty(theWidth)) {
      this._width = Integer.parseInt(theWidth);
    }
    String theHeight = data.getAttribute(VASTAd.ATTRIBUTE_HEIGHT);
    if (!Utils.isNullOrEmpty(theHeight)) {
      this._height = Integer.parseInt(theHeight);
    }
    this._urlFormat = "text";
    this._url = data.getTextContent();
  }

  /**
   * Check whether this VASTStream is scalable or not.
   * @return true if it is, false if not.
   */
  public boolean isScalable() {
    return _scalable;
  }

  /**
   * Check whether this VASTStream should maintian its aspect ratio when scaling or not.
   * @return true if yes, false if no.
   */
  public boolean isMaintainAspectRatio() {
    return _maintainAspectRatio;
  }

  /**
   * Get the delivery type (format) of this VASTStream.
   * @return a String denoting the delivery type.
   */
  public String getVastDeliveryType() {
    return _vastDeliveryType;
  }

  /**
   * Get the API Framework of this VASTStream.
   * @return the API Framework as a String.
   */
  public String getApiFramework() {
    return _apiFramework;
  }
}
