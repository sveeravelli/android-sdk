package com.ooyala.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Analytics {
  private boolean _ready = false;
  private boolean _failed = false;
  private WebView _jsAnalytics;
  private List<String> _queue = new ArrayList<String>();

  private static final String EMBED_HTML = "<html><head><script src=\"_HOST__URI_\"></script></head><body onLoad=\"reporter = new Ooyala.Reporter('_PCODE_');\"></body></html>";

  /**
   * Initialize an Analytics using the specified api
   * @param context the context the initialize the internal WebView with
   * @param api the API to initialize this Analytics with
   */
  public Analytics(Context context, PlayerAPIClient api) {
    this(context, EMBED_HTML.replaceAll("_HOST_", Constants.JS_ANALYTICS_HOST).replaceAll("_URI_", Constants.JS_ANALYTICS_URI).replaceAll("_PCODE_", api.getPcode()));
  }

  /**
   * Initialize an Analytics using the specified api and HTML (used for testing only)
   * @param context the context the initialize the internal WebView with
   * @param embedHTML the HTML to use when initializing this Analytics
   */
  public Analytics(Context context, String embedHTML) {
    _jsAnalytics = new WebView(context);
    _jsAnalytics.getSettings().setUserAgentString("Ooyala Android SDK");
    _jsAnalytics.getSettings().setJavaScriptEnabled(true);
    _jsAnalytics.setWebViewClient(new WebViewClient() {
      public void onPageFinished(WebView view, String url) {
        if (!_ready && !_failed) {
          _ready = true;
          performQueuedActions();
        }
      }

      public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (!_failed) {
          _ready = false;
          _failed = true;
          Log.e(this.getClass().getName(),"ERROR: Failed to load js Analytics!");
        }
      }
    });
    //give dummy url to allow for cookie setting
    _jsAnalytics.loadDataWithBaseURL("http://www.ooyala.com/analytics.html", embedHTML, "text/html", "UTF-8", "");
  }

  /**
   * Report a new video being initialized with the given embed code and duration
   * @param embedCode the embed code of the new video
   * @param duration the duration (in seconds) of the new video
   */
  public void initializeVideo(String embedCode, double duration) {
    if (_failed) { return; }
    String action = "javascript:reporter.initializeVideo('"+embedCode+"',"+duration+");";
    if (!_ready) {
      queue(action);
    } else {
      _jsAnalytics.loadUrl(action);
    }
  }

  /**
   * Report a player load
   */
  public void reportPlayerLoad() {
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
  public void reportPlayheadUpdate(double time) {
    if (_failed) { return; }
    String action = "javascript:reporter.reportPlayheadUpdate("+time*1000+");";
    if (!_ready) {
      queue(action);
    } else {
      _jsAnalytics.loadUrl(action);
    }
  }

  /**
   * Report that the player has started playing
   */
  public void reportPlayStarted() {
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
  public void reportReplay() {
    if (_failed) { return; }
    String action = "javascript:reporter.reportReplay();";
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
}
