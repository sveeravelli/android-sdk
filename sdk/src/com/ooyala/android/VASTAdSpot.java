package com.ooyala.android;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ooyala.android.Constants.ReturnState;

public class VASTAdSpot extends AdSpot {
  protected String _signature;                           /**< The signature for the vast request */
  protected long _expires;                               /**< The expires for the vast request */
  protected URL _vastURL;                                /**< The url for the vast request */
  protected List<VASTAd> _ads = new ArrayList<VASTAd>(); /**< The actual ads (List of VASTAd) */

  /** @internal
   * Initialize a VASTAdSpot using the specified data (subclasses should override this)
   * @param[in] data the NSDictionary containing the data to use to initialize this VASTAdSpot
   * @param[in] theAPI the PlayerAPIClient that was used to fetch this VASTAd
   * @returns the initialized VASTAdSpot
   */
  public VASTAdSpot(JSONObject data, PlayerAPIClient api) {
    _api = api;
    update(data);
  }

  /** @internal
   * Update the VASTAdSpot using the specified data (subclasses should override and call this)
   * @param[in] data the NSDictionary containing the data to use to update this VASTAdSpot
   * @returns ReturnState.STATE_FAIL if the parsing failed, ReturnState.STATE_MATCHED if it was successful
   */
  public ReturnState update(JSONObject data) {
    switch (super.update(data)) {
    case STATE_FAIL:
      return ReturnState.STATE_FAIL;
    case STATE_UNMATCHED:
      return ReturnState.STATE_UNMATCHED;
    default:
      break;
  }
  if (data.isNull(Constants.KEY_SIGNATURE)) {
    System.err.println("ERROR: Fail to update VASTAd with dictionary because no signature exists!");
    return ReturnState.STATE_FAIL;
  }
  if (data.isNull(Constants.KEY_EXPIRES)) {
    System.err.println("ERROR: Fail to update VASTAd with dictionary because no expires exists!");
    return ReturnState.STATE_FAIL;
  }
  if (data.isNull(Constants.KEY_URL)) {
    System.err.println("ERROR: Fail to update VASTAd with dictionary because no url exists!");
    return ReturnState.STATE_FAIL;
  }
  try {
    _signature = data.getString(Constants.KEY_SIGNATURE);
    _expires = data.getInt(Constants.KEY_EXPIRES);
    try {
      _vastURL = new URL(data.getString(Constants.KEY_URL));
    } catch (MalformedURLException exception) {
      System.out.println("Malformed VAST URL: " + data.getString(Constants.KEY_URL));
      return ReturnState.STATE_FAIL;
    }
  } catch (JSONException exception) {
    System.out.println("JSONException: " + exception);
    return ReturnState.STATE_FAIL;
  }
  return ReturnState.STATE_MATCHED;
  }

  /** @internal
   * Fetch the additional required info for the ad
   * @note As of right now, we only support VAST 2.0 Linear Ads. Information about Non-Linear and Companion Ads are stored in the dictionaries nonLinear and companion respectively.
   * @returns NO if errors occurred, YES if successful
   */
  @Override
  public boolean fetchPlaybackInfo() {
    if (_vastURL == null) { return false; }
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(_vastURL.toString());
      Element vast = doc.getDocumentElement();
      if (!vast.getTagName().equals(Constants.ELEMENT_VAST)) { return false; }
      String vastVersion = vast.getAttribute(Constants.ATTRIBUTE_VERSION);
      if (Double.parseDouble(vastVersion) < Constants.MINIMUM_SUPPORTED_VAST_VERSION) { return false; }
      Node ad = vast.getFirstChild();
      while (ad != null) {
        if (!(ad instanceof Element) || !((Element)ad).getTagName().equals(Constants.ELEMENT_AD)) { ad = ad.getNextSibling(); continue; }
        VASTAd vastAd = new VASTAd((Element)ad);
        if (vastAd != null) { _ads.add(vastAd); }
        ad = ad.getNextSibling();
      }
    } catch (Exception e) {
      System.out.println("ERROR: Unable to fetch VAST ad tag info: " + e);
      return false;
    }
    return true;
  }

  public List<VASTAd> getAds() {
    return _ads;
  }

  public URL getVASTURL() {
    return _vastURL;
  }

}