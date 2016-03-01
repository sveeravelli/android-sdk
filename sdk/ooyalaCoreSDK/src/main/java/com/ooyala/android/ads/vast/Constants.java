package com.ooyala.android.ads.vast;

/**
 * Created by zchen on 2/29/16.
 */
public class Constants {
  static final double MINIMUM_SUPPORTED_VAST_VERSION = 2.0;
  static final double MAXIMUM_SUPPORTED_VAST_VERSION = 3.0;
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
  static final String ELEMENT_ICONS = "Icons";
  static final String ELEMENT_ICON = "Icon";
  static final String ELEMENT_ICON_CLICKS = "IconClicks";
  static final String ELEMENT_ICON_CLICK_THROUGH = "IconClickThrough";
  static final String ELEMENT_ICON_CLICK_TRACKING = "IconClickTracking";
  static final String ELEMENT_ICON_VIEW_TRACKING = "IconViewTracking";
  static final String ELEMENT_STATIC_RESOURCE = "StaticResource";
  static final String ELEMENT_IFRAME_RESOURCE = "IFrameResource";
  static final String ELEMENT_HTML_RESOURCE = "HTMLResource";

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
  static final String ATTRIBUTE_SKIPOFFSET = "skipoffset";
  static final String ATTRIBUTE_PROGRAM = "program";
  static final String ATTRIBUTE_XPOSITION = "xPosition";
  static final String ATTRIBUTE_YPOSITION = "yPosition";
  static final String ATTRIBUTE_OFFSET = "offset";
  static final String ATTRIBUTE_DURATION = "duration";
  static final String ATTRIBUTE_CREATIVE_TYPE = "creativeType";

  static final String MIME_TYPE_MP4 = "video/mp4";
  static final String MIME_TYPE_M3U8 = "application/x-mpegURL";
  static final String MIME_TYPE_WIDEVINE = "video/wvm";

  static final String KEY_SIGNATURE = "signature";
  static final String KEY_URL = "url";
}
