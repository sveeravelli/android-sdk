package com.ooyala.android.ads.vast;

/**
 * Created by zchen on 2/29/16.
 */
public class Constants {
  public static final double MINIMUM_SUPPORTED_VAST_VERSION = 2.0;
  public static final double MAXIMUM_SUPPORTED_VAST_VERSION = 3.0;
  public static final double MINIMUM_SUPPORTED_VMAP_VERSION = 1.0;
  public static final double MAXIMUM_SUPPORTED_VMAP_VERSION = 1.0;

  public static final String ELEMENT_VAST = "VAST";
  public static final String ELEMENT_AD = "Ad";
  public static final String ELEMENT_IN_LINE = "InLine";
  public static final String ELEMENT_WRAPPER = "Wrapper";
  public static final String ELEMENT_AD_SYSTEM = "AdSystem";
  public static final String ELEMENT_AD_TITLE = "AdTitle";
  public static final String ELEMENT_DESCRIPTION = "Description";
  public static final String ELEMENT_SURVEY = "Survey";
  public static final String ELEMENT_ERROR = "Error";
  public static final String ELEMENT_IMPRESSION = "Impression";
  public static final String ELEMENT_CREATIVES = "Creatives";
  public static final String ELEMENT_CREATIVE = "Creative";
  public static final String ELEMENT_LINEAR = "Linear";
  public static final String ELEMENT_NON_LINEAR_ADS = "NonLinearAds";
  public static final String ELEMENT_COMPANION_ADS = "CompanionAds";
  public static final String ELEMENT_EXTENSIONS = "Extensions";
  public static final String ELEMENT_DURATION = "Duration";
  public static final String ELEMENT_TRACKING_EVENTS = "TrackingEvents";
  public static final String ELEMENT_TRACKING = "Tracking";
  public static final String ELEMENT_AD_PARAMETERS = "AdParameters";
  public static final String ELEMENT_VIDEO_CLICKS = "VideoClicks";
  public static final String ELEMENT_CLICK_THROUGH = "ClickThrough";
  public static final String ELEMENT_CLICK_TRACKING = "ClickTracking";
  public static final String ELEMENT_CUSTOM_CLICK = "CustomClick";
  public static final String ELEMENT_MEDIA_FILES = "MediaFiles";
  public static final String ELEMENT_MEDIA_FILE = "MediaFile";
  public static final String ELEMENT_VAST_AD_TAG_URI = "VASTAdTagURI";
  public static final String ELEMENT_ICONS = "Icons";
  public static final String ELEMENT_ICON = "Icon";
  public static final String ELEMENT_ICON_CLICKS = "IconClicks";
  public static final String ELEMENT_ICON_CLICK_THROUGH = "IconClickThrough";
  public static final String ELEMENT_ICON_CLICK_TRACKING = "IconClickTracking";
  public static final String ELEMENT_ICON_VIEW_TRACKING = "IconViewTracking";
  public static final String ELEMENT_STATIC_RESOURCE = "StaticResource";
  public static final String ELEMENT_IFRAME_RESOURCE = "IFrameResource";
  public static final String ELEMENT_HTML_RESOURCE = "HTMLResource";
  // vmap elements
  public static final String ELEMENT_VMAP = "vmap:VMAP";
  public static final String ELEMENT_ADBREAK = "vmap:AdBreak";
  public static final String ELEMENT_ADSOURCE = "vmap:AdSource";
  public static final String ELEMENT_ADTAGURI = "vmap:AdTagURI";
  public static final String ELEMENT_VASTADDATA = "vmap:VASTAdData";
  public static final String ELEMENT_CUSTOMDATA = "vmap:CustomAdData";

  public static final String ATTRIBUTE_VERSION = "version";
  public static final String ATTRIBUTE_ID = "id";
  public static final String ATTRIBUTE_SEQUENCE = "sequence";
  public static final String ATTRIBUTE_EVENT = "event";
  public static final String ATTRIBUTE_DELIVERY = "delivery";
  public static final String ATTRIBUTE_TYPE = "type";
  public static final String ATTRIBUTE_BITRATE = "bitrate";
  public static final String ATTRIBUTE_WIDTH = "width";
  public static final String ATTRIBUTE_HEIGHT = "height";
  public static final String ATTRIBUTE_SCALABLE = "scalable";
  public static final String ATTRIBUTE_MAINTAIN_ASPECT_RATIO = "maintainAspectRatio";
  public static final String ATTRIBUTE_API_FRAMEWORK = "apiFramework";
  public static final String ATTRIBUTE_SKIPOFFSET = "skipoffset";
  public static final String ATTRIBUTE_PROGRAM = "program";
  public static final String ATTRIBUTE_XPOSITION = "xPosition";
  public static final String ATTRIBUTE_YPOSITION = "yPosition";
  public static final String ATTRIBUTE_OFFSET = "offset";
  public static final String ATTRIBUTE_DURATION = "duration";
  public static final String ATTRIBUTE_CREATIVE_TYPE = "creativeType";
  // vmap attributes
  public static final String ATTRIBUTE_TIMEOFFSET = "timeOffset";
  public static final String ATTRIBUTE_BREAKTYPE = "breakType";
  public static final String ATTRIBUTE_BREAKID = "breakId";
  public static final String ATTRIBUTE_REPEAT_AFTER = "repeatAfter";
  public static final String ATTRIBUTE_ALLOW_MULTIPLE_ADS = "allowMultipleAds";
  public static final String ATTRIBUTE_FOLLOW_REDIRECTS = "followRedirects";
  public static final String ATTRIBUTE_TEMPLATE_TYPE = "templateType";

  public static final String MIME_TYPE_MP4 = "video/mp4";
  public static final String MIME_TYPE_M3U8 = "application/x-mpegURL";
  public static final String MIME_TYPE_WIDEVINE = "video/wvm";

  public static final String KEY_SIGNATURE = "signature";
  public static final String KEY_URL = "url";
  public static final String KEY_DURATION = "duration";
}
