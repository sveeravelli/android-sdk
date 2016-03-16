package com.ooyala.android.ads.vast;

import android.os.AsyncTask;

import com.ooyala.android.apis.FetchPlaybackInfoCallback;
import com.ooyala.android.item.OoyalaManagedAdSpot;
import com.ooyala.android.util.DebugMode;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * A model of an VAST Ad spot, which can be played during video playback
 */
public class VASTAdSpot extends OoyalaManagedAdSpot {
  private static final String TAG = VASTAdSpot.class.getSimpleName();

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
  protected List<VASTAd> _poddedAds = new ArrayList<VASTAd>();
  protected List<VASTAd> _standAloneAds = new ArrayList<VASTAd>();
  protected List<VASTAdSpot> _vmapAdSpots;
  protected int _contentDuration;
  protected boolean _infoFetched;

  /**
   * for testing purpose only
   * package private on purpose
   * @param e the xml
   */
  VASTAdSpot(Element e) {
    this(0, 0, e);
  }

  /**
   * Initialize a VASTAdSpot using the specified data
   * @param timeOffset the time offset at which the VASTAdSpot should play
   * @param clickURL the clickthrough URL
   * @param trackingURLs the tracking URLs that should be pinged when this ad plays
   * @param vastURL the VAST URL containing the VAST compliant XML for this ad spot
   */
  public VASTAdSpot(int timeOffset, int duration, URL clickURL, List<URL> trackingURLs, URL vastURL) {
    super(timeOffset, clickURL, trackingURLs);
    _contentDuration = duration;
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
   * initialize a VASTAdSpot via XML document
   * @param timeOffset the time offset of the ad spot
   * @param duration the content duration
   * @param e the VAST document element
   */
  public VASTAdSpot(int timeOffset, int duration, Element e) {
    super(timeOffset, null, null);
    _contentDuration = duration;
    _infoFetched = parse(e);
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
    if (!data.isNull(Constants.KEY_DURATION)) {
      try {
        _contentDuration = data.getInt(Constants.KEY_DURATION);
      } catch (JSONException e) {
        DebugMode.logE(TAG, "unable to get content duration", e);
      }
    }
    if (data.isNull(Constants.KEY_SIGNATURE)) {
      DebugMode.logE(TAG, "ERROR: Fail to update VASTAd with dictionary because no signature exists!");
      return ReturnState.STATE_FAIL;
    }
    if (data.isNull(KEY_EXPIRES)) {
      DebugMode.logE(TAG, "ERROR: Fail to update VASTAd with dictionary because no expires exists!");
      return ReturnState.STATE_FAIL;
    }
    if (data.isNull(KEY_URL)) {
      DebugMode.logE(TAG, "ERROR: Fail to update VASTAd with dictionary because no url exists!");
      return ReturnState.STATE_FAIL;
    }
    try {
      _signature = data.getString(Constants.KEY_SIGNATURE);
      _expires = data.getInt(KEY_EXPIRES);
      _vastURL = VASTUtils.urlFromAdUrlString(data.getString(Constants.KEY_URL));
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
      return parse(vast);

    } catch (Exception e) {
      System.out.println("ERROR: Unable to fetch VAST ad tag info: " + e);
      return false;
    }
  }

  protected boolean parse(Element vast) {
    String tag = vast.getTagName();
    if (Constants.ELEMENT_VMAP.equals(tag)) {
      _vmapAdSpots = new ArrayList<VASTAdSpot>();
      return VASTHelper.parse(vast, _vmapAdSpots, _contentDuration);
    } else if (!Constants.ELEMENT_VAST.equals(tag)) {
      return false;
    }

    String vastVersion = vast.getAttribute(Constants.ATTRIBUTE_VERSION);
    if (vastVersion == null) {
      return false;
    }

    double version = 0;
    try {
      version = Double.parseDouble(vastVersion);
    } catch (NumberFormatException e) {
      return false;
    }

    if (version < Constants.MINIMUM_SUPPORTED_VAST_VERSION ||
        version > Constants.MAXIMUM_SUPPORTED_VAST_VERSION) {
      DebugMode.logE(TAG, "unsupported vast version" + vastVersion);
      return false;
    }

    for (Node node = vast.getFirstChild(); node != null; node = node.getNextSibling()) {
      if (node instanceof Element) {
        Element ad = (Element) node;
        String tagName = ad.getTagName();
        if (!Constants.ELEMENT_AD.equals(tagName)) {
          continue;
        }

        VASTAd vastAd = new VASTAd(ad);
        if (vastAd != null) {
          if (vastAd.getAdSequence() > 0) {
            _poddedAds.add(vastAd);
          } else {
            _standAloneAds.add(vastAd);
          }
        }
      }
    }

    if (_poddedAds.size() > 0) {
      // sort podded ads
      Collections.sort(_poddedAds);
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
    if (_poddedAds.size() > 0) {
      return _poddedAds;
    }
    return _standAloneAds;
  }

  public URL getVASTURL() {
    return _vastURL;
  }

  public List<VASTAdSpot> getVMAPAdSpots() {
    return _vmapAdSpots;
  }

  public boolean isInfoFetched() {
    return _infoFetched;
  }
}
