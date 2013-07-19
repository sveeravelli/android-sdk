package com.ooyala.android.imasdk;


import com.ooyala.android.Stream;

public class IMAStream extends Stream {
  /**
   *  Initialize an IMA Stream that is used to playback an IMA ad spot
   *  * @param url the URL of the media file to play as the advertisement
   */
  IMAStream(String url) {
    super();
    this._deliveryType = "mp4"; // mp4 may not be correct 100% of the time, but the IMA Ad manager actually
    this._urlFormat = "text";   // determines playable video types, and always returns playable video for android
    this._url = url;
  }
}
