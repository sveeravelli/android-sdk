package com.ooyala.android.ads.vast;

import com.ooyala.android.util.DebugMode;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by zchen on 3/10/16.
 */
public class VASTHelper {
  private static final String TAG = VASTHelper.class.getSimpleName();
  /**
   * parse the vmap xml according to the spec and generates a list of VASTAdSpots
   * @param e the root element of the vmap xml
   * @param spots a list of vast ad spots as output
   * @param duration the content duration, used to compute percentage time offset
   * @return true if pass succeeds, false if failed
   */
  public static boolean parse(Element e, List<VASTAdSpot> spots, int duration) {
    if (e == null || spots == null) {
      DebugMode.logE(TAG, "some of the arguments are null");
      return false;
    }

    if (!Constants.ELEMENT_VMAP.equals(e.getTagName())) {
      DebugMode.logE(TAG, "xml type is incorrect, tag is:" + e.getTagName());
    }

    String version = e.getAttribute(Constants.ATTRIBUTE_VERSION);
    if (version == null) {
      return false;
    }
    double versionValue = 0;
    try {
      versionValue = Double.parseDouble(version);
    } catch (NumberFormatException ex) {
      return false;
    }

    if (versionValue < Constants.MINIMUM_SUPPORTED_VMAP_VERSION ||
        versionValue > Constants.MAXIMUM_SUPPORTED_VMAP_VERSION) {
      DebugMode.logE(TAG, "unsupported vast version" + versionValue);
      return false;
    }

    for (Node node = e.getFirstChild(); node != null; node = node.getNextSibling()) {
      if (node instanceof Element) {
        Element ad = (Element) node;
        String tagName = ad.getTagName();
        if (!Constants.ELEMENT_ADBREAK.equals(tagName)) {
          continue;
        }

        VMAPAdSpot adSpot = parseAdBreak(ad, duration);
        if (adSpot != null) {
          spots.add(adSpot);
        }
      }
    }

    return true;
  }

  private static VMAPAdSpot parseAdBreak(Element e, int duration) {
    String timeOffsetString = e.getAttribute(Constants.ATTRIBUTE_TIMEOFFSET);
    String repeatAfterString = e.getAttribute(Constants.ATTRIBUTE_REPEAT_AFTER);
    String breakId = e.getAttribute(Constants.ATTRIBUTE_BREAKID);
    String breakType = e.getAttribute(Constants.ATTRIBUTE_BREAKTYPE);
    if (timeOffsetString == null) {
      DebugMode.logE(TAG, "cannot find timeoffset");
      return null;
    }

    Offset timeOffset = Offset.parseOffset(timeOffsetString);
    if (timeOffset == null) {
      DebugMode.logE(TAG, "invalid timeOffset:" + timeOffsetString);
      return null;
    }

    double repeatAfter = -1;
    if (repeatAfterString != null && repeatAfterString.length() > 0) {
      repeatAfter = VASTUtils.secondsFromTimeString(repeatAfterString, -1);
    }

    Element adSource = null;
    for (Node node = e.getFirstChild(); node != null; node = node.getNextSibling()) {
      if (node instanceof Element && Constants.ELEMENT_ADSOURCE.equals(((Element) node).getTagName())) {
        adSource = (Element)node;
        break;
      }
    }

    if (adSource == null) {
      return null;
    }

    String adSourceId= adSource.getAttribute(Constants.ATTRIBUTE_ID);
    String allowMultiAdsString = adSource.getAttribute(Constants.ATTRIBUTE_ALLOW_MULTIPLE_ADS);
    String followRedirectsString = adSource.getAttribute(Constants.ATTRIBUTE_FOLLOW_REDIRECTS);
    boolean allowMultiAds = true;
    boolean followRedirects = true;
    if (allowMultiAdsString != null) {
      allowMultiAds = Boolean.parseBoolean(allowMultiAdsString);
    }
    if (followRedirectsString != null) {
      followRedirects = Boolean.parseBoolean(followRedirectsString);
    }

    for (Node n = adSource.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (!(n instanceof Element)) {
        continue;
      }
      Element vast = (Element)n;
      String tag = vast.getTagName();
      if (Constants.ELEMENT_VASTADDATA.equals(tag)) {
        return new VMAPAdSpot(timeOffset, duration, repeatAfter, breakType, breakId, adSourceId, allowMultiAds, followRedirects, (Element)vast.getFirstChild());
      } else if (Constants.ELEMENT_ADTAGURI.equals(tag)) {
        String uri = vast.getTextContent().trim();
        try {
          URL url = new URL(uri);
          return new VMAPAdSpot(timeOffset, duration, repeatAfter, breakType, breakId, adSourceId, allowMultiAds, followRedirects, url);
        } catch (MalformedURLException ex) {
          DebugMode.logE(TAG, "invalid uri:" + ex.getMessage(), ex);
          return null;
        }
      } else if (Constants.ELEMENT_CUSTOMDATA.equals(tag)) {
        // not implemented
        return null;
      }
    }
    return null;
  }
}
