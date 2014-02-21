package com.ooyala.android;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class Analytics {

  private static final String TAG = "Analytics";
  private static final String TMP_PREFIX = "pb2823";
  private static final String TMP_EXT = ".html";
  private static final String EMBED_MODULEPARAMS_HTML =
      "<html><head><script src=\"_HOST__URI_\"></script><script>function _init() {reporter = new Ooyala.Reporter('_PCODE_',_MODULE_PARAMS_);console.log('...onLoad: domain='+document.domain);};</script></script></head><body onLoad=\"_init();\"></body></html>";

  private boolean _ready;
  private boolean _failed;
  private WebView _jsAnalytics;
  private List<String> _queue = new ArrayList<String>();
  private String _defaultUserAgent = "";
  private String _userAgent = "";
  private TemporaryInternalStorageFileManager tmpBootHtmlFileManager;

  private static String generateEmbedHTML(PlayerAPIClient api) {

    final Map<String, String> moduleParams = new HashMap<String, String>();

    String url = "http://www.ooyala.com/analytics.html";
    try {
      url = new URL("http", api.getDomain(), "/").toString();
    }
    catch (MalformedURLException e) {
      System.out.println("falling back to default analytics URL " + url);
    }
    moduleParams.put(Constants.JS_ANALYTICS_DOCUMENT_URL, url);

    //If there is an account ID, add it to the Reporter.js initializer
    if(api.getUserInfo() != null && api.getUserInfo().getAccountId() != null) {
      moduleParams.put(Constants.JS_ANALYTICS_ACCOUNT_ID, api.getUserInfo().getAccountId());
    }

    return EMBED_MODULEPARAMS_HTML
        .replaceAll("_HOST_", Constants.JS_ANALYTICS_HOST)
        .replaceAll("_URI_", Constants.JS_ANALYTICS_URI)
        .replaceAll("_PCODE_", api.getPcode())
        .replaceAll("_MODULE_PARAMS_", new JSONObject(moduleParams).toString());
  }

  private static void setAllowUniversalAccessFromFileURLs( final WebSettings settings ) {
    for( Method m : settings.getClass().getMethods() ) {
      if( m.getName().equals( "setAllowUniversalAccessFromFileURLs" ) ) {
        try {
          m.invoke( settings, true );
        }
        catch (Exception e) {
          Log.d( TAG, "failed: " + e.getStackTrace() );
        }
        break;
      }
    }
  }

  /**
   * Initialize an Analytics using the specified api
   * @param context the context the initialize the internal WebView with
   * @param api the API to initialize this Analytics with
   */
  Analytics(Context context, PlayerAPIClient api) {
    this(context, generateEmbedHTML(api), api.getDomain());
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

    tmpBootHtmlFileManager = new TemporaryInternalStorageFileManager();

    _jsAnalytics = new WebView(context);

    _defaultUserAgent = String.format(Constants.JS_ANALYTICS_USER_AGENT, Constants.SDK_VERSION,
        _jsAnalytics.getSettings().getUserAgentString());
    _userAgent = _defaultUserAgent;
    _jsAnalytics.getSettings().setUserAgentString(_defaultUserAgent);
    _jsAnalytics.getSettings().setJavaScriptEnabled(true);
    setAllowUniversalAccessFromFileURLs( _jsAnalytics.getSettings() );

    _jsAnalytics.setWebViewClient( new WebViewClient() {
      @Override
      public void onPageFinished(WebView view, String url) {
        if (!_ready && !_failed) {
          _ready = true;
          Log.d(this.getClass().getName(), "Initialized Analytics.");
          performQueuedActions();
        }
      }
      @Override
      public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (!_failed) {
          _ready = false;
          _failed = true;
          Log.e(this.getClass().getName(), "ERROR: Failed to load js Analytics!");
        }
      }
    });

    _jsAnalytics.setWebChromeClient( new WebChromeClient() {
      @Override
      public void onConsoleMessage(String message, int lineNumber, String sourceID) {
        Log.v( TAG, "javascript: " + sourceID + "@" + lineNumber + ": " + message );
      }
      @Override
      public boolean onConsoleMessage(ConsoleMessage cm) {
        onConsoleMessage( cm.message(), cm.lineNumber(), cm.sourceId() );
        return true;
      }
    });

    bootHtml( context, embedDomain, embedHTML );

    Log.d(TAG, "Initialized Analytics with user agent: "
        + _jsAnalytics.getSettings().getUserAgentString());
  }

  private void bootHtml( final Context context, final String embedDomain, final String embedHTML ) {
    try {
      final TemporaryInternalStorageFile tmpBootHtmlFile = tmpBootHtmlFileManager.next( context, TMP_PREFIX, TMP_EXT );
      tmpBootHtmlFile.write( embedHTML );
      loadTmpBootHtmlFile( tmpBootHtmlFile );
    }
    catch (IOException e) {
      Log.e( TAG, "failed: " + e.getStackTrace() );
    }
    catch (IllegalArgumentException e) {
      Log.e( TAG, "failed: " + e.getStackTrace() );
    }
  }

  private void loadTmpBootHtmlFile( final TemporaryInternalStorageFile tmpBootHtmlFile ) {
    final String htmlUrlStr = "file://" + tmpBootHtmlFile.getAbsolutePath();
    Log.d( TAG, "trying to load: " + htmlUrlStr );

    try {
      final Scanner scanner = new Scanner( tmpBootHtmlFile.getFile() );
      try { while( true ) { Log.d( TAG, scanner.nextLine() ); } } catch( NoSuchElementException e ) { }
    }
    catch( FileNotFoundException e ) { }

    _jsAnalytics.loadUrl( htmlUrlStr );
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
