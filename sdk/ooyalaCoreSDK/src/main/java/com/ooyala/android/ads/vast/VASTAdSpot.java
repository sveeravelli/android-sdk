package com.ooyala.android.ads.vast;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import android.os.AsyncTask;

import com.ooyala.android.util.DebugMode;
import com.ooyala.android.apis.FetchPlaybackInfoCallback;
import com.ooyala.android.item.OoyalaManagedAdSpot;

/**
 * A model of an VAST Ad spot, which can be played during video playback
 */
public class VASTAdSpot extends OoyalaManagedAdSpot {
  static final String KEY_EXPIRES = "expires";  //embedded, Vast, PAPI
  static final String KEY_SIGNATURE = "signature"; // embedded, VAST
  static final String KEY_URL = "url";  // CC, Stream, VAST

  /** The signature for the vast request */
  protected String _signature;
  /** The expires for the vast request */
  protected long _expires;
  /** The url for the vast request */
  protected URL _vastURL;
  /** The actual ads (List of VASTAd) */
  protected List<VASTAd> _ads = new ArrayList<VASTAd>();

  /**
   * Initialize a VASTAdSpot using the specified data
   * @param time the time at which the VASTAdSpot should play
   * @param clickURL the clickthrough URL
   * @param trackingURLs the tracking URLs that should be pinged when this ad plays
   * @param vastURL the VAST URL containing the VAST compliant XML for this ad spot
   */
  public VASTAdSpot(int time, URL clickURL, List<URL> trackingURLs, URL vastURL) {
    super(time, clickURL, trackingURLs);
    _vastURL = VASTUtils.urlFromAdUrlString(vastURL.toString());

  }

  /**
   * Initialize a VASTAdSpot using the specified data (subclasses should override this)
   * @param data the NSDictionary containing the data to use to initialize this VASTAdSpot
   */
  public VASTAdSpot(JSONObject data) {
    update(data);
  }

  /**
   * Update the VASTAdSpot using the specified data (subclasses should override and call this)
   * @param data the NSDictionary containing the data to use to update this VASTAdSpot
   * @return ReturnState.STATE_FAIL if the parsing failed, ReturnState.STATE_MATCHED if it was successful
   */
  @Override

  public ReturnState update(JSONObject data) {
    switch (super.update(data)) {
      case STATE_FAIL:
        return ReturnState.STATE_FAIL;
      case STATE_UNMATCHED:
        return ReturnState.STATE_UNMATCHED;
      default:
        break;
    }
    if (data.isNull(VASTAd.KEY_SIGNATURE)) {
      DebugMode.logE(this.getClass().getName(),
          "ERROR: Fail to update VASTAd with dictionary because no signature exists!");
      return ReturnState.STATE_FAIL;
    }
    if (data.isNull(KEY_EXPIRES)) {
      DebugMode.logE(this.getClass().getName(),
          "ERROR: Fail to update VASTAd with dictionary because no expires exists!");
      return ReturnState.STATE_FAIL;
    }
    if (data.isNull(KEY_URL)) {
      DebugMode.logE(this.getClass().getName(), "ERROR: Fail to update VASTAd with dictionary because no url exists!");
      return ReturnState.STATE_FAIL;
    }
    try {
      _signature = data.getString(VASTAd.KEY_SIGNATURE);
      _expires = data.getInt(KEY_EXPIRES);
      _vastURL = VASTUtils.urlFromAdUrlString(data.getString(VASTAd.KEY_URL));
      if (_vastURL == null) {
        return ReturnState.STATE_FAIL;
      }
    } catch (JSONException exception) {
      DebugMode.logD(this.getClass().getName(), "JSONException: " + exception);
      return ReturnState.STATE_FAIL;
    }

    return ReturnState.STATE_MATCHED;
  }

  /**
   * Fetch the additional required info for the ad NOTE: As of right now, we only support VAST 2.0 Linear Ads.
   * Information about Non-Linear and Companion Ads are stored in the dictionaries nonLinear and companion
   * respectively.
   * @return false if errors occurred, true if successful
   */
  @Override
  public boolean fetchPlaybackInfo() {
    if (_vastURL == null) { return false; }
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(_vastURL.toString());
      Element vast = doc.getDocumentElement();
      if (!vast.getTagName().equals(VASTAd.ELEMENT_VAST)) { return false; }
      String vastVersion = vast.getAttribute(VASTAd.ATTRIBUTE_VERSION);
      if (Double.parseDouble(vastVersion) < VASTAd.MINIMUM_SUPPORTED_VAST_VERSION) { return false; }
      Node ad = vast.getFirstChild();
      while (ad != null) {
        if (!(ad instanceof Element) || !((Element) ad).getTagName().equals(VASTAd.ELEMENT_AD)) {
          ad = ad.getNextSibling();
          continue;
        }
        VASTAd vastAd = new VASTAd((Element) ad);
        if (vastAd != null) {
          _ads.add(vastAd);
        }
        ad = ad.getNextSibling();
      }
    } catch (Exception e) {
      System.out.println("ERROR: Unable to fetch VAST ad tag info: " + e);
      return false;
    }
    return true;
  }

  private class FetchPlaybackInfoTask extends AsyncTask<Void, Integer, Boolean> {
    protected FetchPlaybackInfoCallback _callback = null;

    public FetchPlaybackInfoTask(FetchPlaybackInfoCallback callback) {
      super();
      _callback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
      return fetchPlaybackInfo();
    }

    @Override
    protected void onPostExecute(Boolean result) {
      _callback.callback(result.booleanValue());
    }
  }

  public Object fetchPlaybackInfo(FetchPlaybackInfoCallback callback) {
    FetchPlaybackInfoTask task = new FetchPlaybackInfoTask(callback);
    task.execute();
    return task;
  }

  public List<VASTAd> getAds() {
    return _ads;
  }

  public URL getVASTURL() {
    return _vastURL;
  }

}
