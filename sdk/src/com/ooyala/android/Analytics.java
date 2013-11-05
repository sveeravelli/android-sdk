package com.ooyala.android;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class Analytics {
  private boolean _ready = false;
  private boolean _failed = false;
  private WebView _jsAnalytics;
  private List<String> _queue = new ArrayList<String>();
  private String _defaultUserAgent = "";
  private String _userAgent = "";

  private static final String EMBED_HTML = "<html><head><script src=\"_HOST__URI_\"></script></head><body onLoad=\"reporter = new Ooyala.Reporter('_PCODE_');\"></body></html>";
  private static final String EMBED_MODULEPARAMS_HTML = "<html><head><script src=\"_HOST__URI_\"></script></head><body onLoad='reporter = new Ooyala.Reporter(\"_PCODE_\",_MODULE_PARAMS_);'></body></html>";

  /**
   * Initialize an Analytics using the specified api
   * @param context the context the initialize the internal WebView with
   * @param api the API to initialize this Analytics with
   */
  Analytics(Context context, PlayerAPIClient api) {
    this(context, generateEmbedHTML(api), api.getDomain());
  }

  private static String generateEmbedHTML(PlayerAPIClient api) {

    //If there is an account ID, add it to the Reporter.js initializer
    if(api.getUserInfo() != null && api.getUserInfo().getAccountId() != null) {
      Map<String, String> moduleParams = new HashMap<String, String>();
      moduleParams.put(Constants.JS_ANALYTICS_ACCOUNT_ID, api.getUserInfo().getAccountId());

      return EMBED_MODULEPARAMS_HTML
          .replaceAll("_HOST_", Constants.JS_ANALYTICS_HOST)
          .replaceAll("_URI_", Constants.JS_ANALYTICS_URI)
          .replaceAll("_PCODE_", api.getPcode())
          .replaceAll("_MODULE_PARAMS_", new JSONObject(moduleParams).toString());
    } else {
      return EMBED_HTML
          .replaceAll("_HOST_", Constants.JS_ANALYTICS_HOST)
          .replaceAll("_URI_", Constants.JS_ANALYTICS_URI)
          .replaceAll("_PCODE_", api.getPcode());
    }
  }

  /**
   * Initialize an Analytics using the specified api and HTML (used for testing only)
   * @param context the context the initialize the internal WebView with
   * @param embedHTML the HTML to use when initializing this Analytics
   */
  Analytics(Context context, String embedHTML) {
    //compatible with old behavior.  only used for test..
    this(context, embedHTML, "http://www.ooyala.com/analytics.html");
  }

  /**
   * Initialize an Analytics using the specified api and HTML (used internally)
   * @param context the context the initialize the internal WebView with
   * @param embedHTML the HTML to use when initializing this Analytics
   * @param embedDomain the domain of the dummy page hosting reporter.js
   */
  @SuppressLint("SetJavaScriptEnabled")
  private Analytics(Context context, String embedHTML, String embedDomain) {
    _jsAnalytics = new WebView(context);
    _defaultUserAgent = String.format(Constants.JS_ANALYTICS_USER_AGENT, Constants.SDK_VERSION,
        _jsAnalytics.getSettings().getUserAgentString());
    _userAgent = _defaultUserAgent;
    _jsAnalytics.getSettings().setUserAgentString(_defaultUserAgent);
    _jsAnalytics.getSettings().setJavaScriptEnabled(true);
    _jsAnalytics.setWebViewClient(new WebViewClient() {
      public void onPageFinished(WebView view, String url) {
        if (!_ready && !_failed) {
          _ready = true;
          Log.d(this.getClass().getName(), "Initialized Analytics.");
          performQueuedActions();
        }
      }

      public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (!_failed) {
          _ready = false;
          _failed = true;
          Log.e(this.getClass().getName(), "ERROR: Failed to load js Analytics!");
        }
      }
    });
    // give dummy url to allow for cookie setting
    String url = "http://www.ooyala.com/analytics.html";

    try {
      url = new URL("http", embedDomain, "/").toString();
    } catch (MalformedURLException e) {
      System.out.println("falling back to default analytics URL");
    }

    _jsAnalytics.loadDataWithBaseURL(url, embedHTML, "text/html", "UTF-8", "");
    Log.d(this.getClass().getName(), "Initializing Analytics with user agent: "
        + _jsAnalytics.getSettings().getUserAgentString());
  }

  /**
   * Report a new video being initialized with the given embed code and duration
   * @param embedCode the embed code of the new video
   * @param duration the duration (in seconds) of the new video
   */
  void initializeVideo(String embedCode, double duration) {
    if (_failed) { return; }
    String action = "javascript:reporter.initializeVideo('" + embedCode + "'," + duration + ");";
    if (!_ready) {
      queue(action);
    } else {
      _jsAnalytics.loadUrl(action);
    }
  }

  /**
   * Report a player load
   */
  void reportPlayerLoad() {
    if (_failed) { return; }
    String action = "javascript:reporter.reportPlayerLoad();";
    if (!_ready) {
      queue(action);
    } else {
      _jsAnalytics.loadUrl(action);
    }
  }

  /**
   * Report a playhead update to the specified time
   * @param time the new playhead time (in seconds)
   */
  void reportPlayheadUpdate(double time) {
    if (_failed) { return; }
    String action = "javascript:reporter.reportPlayheadUpdate(" + time * 1000 + ");";
    if (!_ready) {
      queue(action);
    } else {
      _jsAnalytics.loadUrl(action);
    }
  }

  /**
   * Report that the player has started playing
   */
  void reportPlayStarted() {
    if (_failed) { return; }
    String action = "javascript:reporter.reportPlayStarted();";
    if (!_ready) {
      queue(action);
    } else {
      _jsAnalytics.loadUrl(action);
    }
  }

  /**
   * Report that the player was asked to replay
   */
  void reportReplay() {
    if (_failed) { return; }
    String action = "javascript:reporter.reportReplay();";
    if (!_ready) {
      queue(action);
    } else {
      _jsAnalytics.loadUrl(action);
    }
  }

  void setTags(List<String> tags) {
    if (_failed) { return; }
    String action = "javascript:reporter.setTags([\"" + Utils.join(tags, "\",\"") + "\"]);";
    if (!_ready) {
      queue(action);
    } else {
      _jsAnalytics.loadUrl(action);
    }
  }

  private void queue(String action) {
    _queue.add(action);
  }

  private void performQueuedActions() {
    for (String action : _queue) {
      _jsAnalytics.loadUrl(action);
    }
  }

  public void setUserAgent(String userAgent) {
    if (userAgent != null) {
      _userAgent = userAgent;
    }
    else {
      _userAgent = _defaultUserAgent;
    }
    _jsAnalytics.getSettings().setUserAgentString(_userAgent);
  }
}
