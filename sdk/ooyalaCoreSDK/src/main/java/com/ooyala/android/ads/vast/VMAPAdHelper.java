package com.ooyala.android.ads.vast;

import com.ooyala.android.util.DebugMode;

import org.w3c.dom.Element;

import java.util.List;

/**
 * Created by zchen on 3/10/16.
 */
public class VMAPAdHelper {
  private static final String TAG = VMAPAdHelper.class.getSimpleName();
  /**
   * parse the vmap xml according to the spec and generates a list of VASTAdSpots
   * @param e the root element of the vmap xml
   * @param spots a list of vast ad spots as output
   * @return true if pass succeeds, false if failed
   */
  public static boolean parse(Element e, List<VASTAdSpot> spots) {
    if (e == null || spots == null) {
      DebugMode.logE(TAG, "some of the arguments are null");
      return false;
    }

    if (!Constants.ELEMENT_VMAP.equals(e.getTagName())) {
      DebugMode.logE(TAG, "xml type is incorrect, tag is:" + e.getTagName());
    }
    return true;
  }
}
