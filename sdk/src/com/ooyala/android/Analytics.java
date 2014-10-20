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

  private static final String JS_ANALYTICS_URI = "/reporter.js";
  private static final String JS_ANALYTICS_USER_AGENT = "Ooyala Android SDK v%s [%s]";
  private static final String JS_ANALYTICS_ACCOUNT_ID = "accountId";
  private static final String JS_ANALYTICS_GUID = "guid";
  private static final String JS_ANALYTICS_DOCUMENT_URL = "documentUrl";

  private boolean _ready;
  private boolean _failed;
  private boolean _initialPlay = true;
  private boolean _shouldReportPlayRequest = false;
  private boolean _shouldReportPlayStart = false;
  private WebView _jsAnalytics;
  private List<String> _queue = new ArrayList<String>();
  private String _defaultUserAgent = "";
  private String _userAgent = "";
  private TemporaryInternalStorageFileManager tmpBootHtmlFileManager;

  private static String generateEmbedHTML(PlayerAPIClient api, Context context) {

    final Map<String, String> moduleParams = new HashMap<String, String>();

    String url = "http://www.ooyala.com/analytics.html";
    try {
      url = new URL("http", api.getDomain().toString(), "/").toString();
    }
    catch (MalformedURLException e) {
      System.out.println("falling back to default analytics URL " + url);
    }
    moduleParams.put(JS_ANALYTICS_DOCUMENT_URL, url);

    //If there is an account ID, add it to the Reporter.js initializer
    if(api.getUserInfo() != null && api.getUserInfo().getAccountId() != null) {
      moduleParams.put(JS_ANALYTICS_ACCOUNT_ID, api.getUserInfo().getAccountId());
    }
    
    String clientId = ClientId.getId(context);
    String encryptedId = Utils.encryptString(clientId);
    moduleParams.put(JS_ANALYTICS_GUID, encryptedId);

    return EMBED_MODULEPARAMS_HTML
        .replaceAll("_HOST_", Environment.JS_ANALYTICS_HOST)
        .replaceAll("_URI_", JS_ANALYTICS_URI)
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
          DebugMode.logD( TAG, "failed: " + e.getStackTrace() );
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
    this(context, generateEmbedHTML(api, context), api.getDomain().toString());
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

    _defaultUserAgent = String.format(JS_ANALYTICS_USER_AGENT, OoyalaPlayer.getVersion(),
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
          DebugMode.logD(this.getClass().getName(), "Initialized Analytics.");
          performQueuedActions();
        }
      }
      @Override
      public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (!_failed) {
          _ready = false;
          _failed = true;
          DebugMode.logE(this.getClass().getName(), "ERROR: Failed to load js Analytics!");
        }
      }
    });

    _jsAnalytics.setWebChromeClient( new WebChromeClient() {
      @Override
      public void onConsoleMessage(String message, int lineNumber, String sourceID) {
        DebugMode.logV( TAG, "javascript: " + sourceID + "@" + lineNumber + ": " + message );
      }
      @Override
      public boolean onConsoleMessage(ConsoleMessage cm) {
        onConsoleMessage( cm.message(), cm.lineNumber(), cm.sourceId() );
        return true;
      }
    });

    bootHtml( context, embedDomain, embedHTML );

    DebugMode.logD(TAG, "Initialized Analytics with user agent: "
        + _jsAnalytics.getSettings().getUserAgentString());
    reportPlayerLoad();
  }

  private void bootHtml( final Context context, final String embedDomain, final String embedHTML ) {
    try {
      final TemporaryInternalStorageFile tmpBootHtmlFile = tmpBootHtmlFileManager.next( context, TMP_PREFIX, TMP_EXT );
      tmpBootHtmlFile.write( embedHTML );
      loadTmpBootHtmlFile( tmpBootHtmlFile );
    }
    catch (IOException e) {
      DebugMode.logE( TAG, "failed: " + e.getStackTrace() );
    }
    catch (IllegalArgumentException e) {
      DebugMode.logE( TAG, "failed: " + e.getStackTrace() );
    }
  }

  private void loadTmpBootHtmlFile( final TemporaryInternalStorageFile tmpBootHtmlFile ) {
    final String htmlUrlStr = "file://" + tmpBootHtmlFile.getAbsolutePath();
    DebugMode.logD( TAG, "trying to load: " + htmlUrlStr );

    // this is purely for our own debugging purposes...
    try {
      final Scanner scanner = new Scanner( tmpBootHtmlFile.getFile() );
      try { while( true ) { DebugMode.logD( TAG, scanner.nextLine() ); } }
      catch( NoSuchElementException e ) {}
      finally { scanner.close(); }
    }
    catch( FileNotFoundException e ) { }
    // ...this is purely for our own debugging purposes.

    _jsAnalytics.loadUrl( htmlUrlStr );
  }

  /**
   * Helper function to report a player load
   */
  private void report(String action) {
    if (_failed) { return; }
    if (!_ready) {
      queue(action);
    } else {
    DebugMode.logD(TAG, "report:" + action);
      _jsAnalytics.loadUrl(action);
    }
  }

  /**
   * Report a new video being initialized with the given embed code and duration
   * @param embedCode the embed code of the new video
   * @param duration the duration (in seconds) of the new video
   */
  void initializeVideo(String embedCode, double duration) {
    String action = "javascript:reporter.initializeVideo('" + embedCode + "'," + duration + ");";
    _shouldReportPlayRequest = true;
    _shouldReportPlayStart = true;
    report(action);
  }

  /**
   * Report a player load
   */
  private void reportPlayerLoad() {
    report("javascript:reporter.reportPlayerLoad();");
  }

  /**
   * Report a playhead update to the specified time
   * @param time the new playhead time (in seconds)
   */
  void reportPlayheadUpdate(double time) {
    String action = "javascript:reporter.reportPlayheadUpdate(" + time * 1000 + ");";
    report(action);
  }

  /**
   * Report that the player has started playing
   */
  void reportPlayStarted() {
    if (!_shouldReportPlayStart) {
      return;
    }
    _shouldReportPlayStart = false;
    report("javascript:reporter.reportPlayStarted();");
  }

  /**
   * Report that the player was asked to replay
   */
  void reportReplay() {
    report("javascript:reporter.reportReplay();");
  }

  void reportPlayRequested() {
    if (!_shouldReportPlayRequest) {
      return;
    }
    String action = "javascript:reporter.reportPlayStarted();";
    _initialPlay = false;
    _shouldReportPlayRequest = false;
     report(action);
  }

  void setTags(List<String> tags) {
    String action = "javascript:reporter.setTags([\"" + Utils.join(tags, "\",\"") + "\"]);";
    report(action);
  }

  private void queue(String action) {
    _queue.add(action);
  }

  private void performQueuedActions() {
    for (String action : _queue) {
      DebugMode.logI(TAG, "reporting:" + action);
      _jsAnalytics.loadUrl(action);
    }
    _queue.clear();
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
