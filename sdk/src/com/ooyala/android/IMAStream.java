package com.ooyala.android;

import org.w3c.dom.Element;

public class IMAStream extends Stream {
  /** if this stream must maintain the aspect ratio */
  private boolean _maintainAspectRatio;
  /**
   * Initialize a Stream using the specified VAST MediaFile XML (subclasses should override this)
   * @param data the Element containing the xml to use to initialize this Stream
   */
  IMAStream(String url) {
    this._deliveryType = Constants.DELIVERY_TYPE_MP4;
    this._urlFormat = "text";
    this._url = url;
  }
}
