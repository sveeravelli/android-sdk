package com.ooyala.android.nielsensample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

public class OptOutActivity extends Activity {

  private WebView webView;

  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView( R.layout.optout );

    final Button close = (Button) findViewById(R.id.btnOptOutClose);
    close.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v)
      {
//        bailOut("closeAll");
      }
    });

    final Bundle extras = getIntent().getExtras();

    String url = extras.getString(NielsenSampleAppActivity.OPT_OUT_URL_EXTRAS_KEY);
    webView = (WebView) findViewById(R.id.webView);
    webView.getSettings().setJavaScriptEnabled(true);

    // Handle webview scaling
    webView.setInitialScale( 1 );
    webView.getSettings().setBuiltInZoomControls( true );
    webView.getSettings().setSupportZoom( true );
    webView.getSettings().setDisplayZoomControls( false );
    webView.getSettings().setLoadWithOverviewMode( true );
    webView.getSettings().setUseWideViewPort( true );
    webView.getSettings().setLayoutAlgorithm( WebSettings.LayoutAlgorithm.SINGLE_COLUMN );
//    webView.setWebViewClient( new MonitorWebView() );
    webView.setWebChromeClient( new WebChromeClient() );

    Log.d( "WEB", "Launching: " + url );
    webView.loadUrl( url );
  }

}
