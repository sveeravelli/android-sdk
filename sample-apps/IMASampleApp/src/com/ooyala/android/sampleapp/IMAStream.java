package com.ooyala.android.sampleapp;


import com.ooyala.android.Stream;

public class IMAStream extends Stream {
  /**
   * Initialize a Stream using the specified VAST MediaFile XML (subclasses should override this)
   * @param data the Element containing the xml to use to initialize this Stream
   */
  IMAStream(String url) {
    super();
    this._deliveryType = "mp4";
    this._urlFormat = "text";
    this._url = url;
  }
}
