package com.ooyala.testapp;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.ooyala.android.EmbedTokenGenerator;
import com.ooyala.android.EmbedTokenGeneratorCallback;
import com.ooyala.android.OoyalaAdSpot;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OptimizedOoyalaPlayerLayoutController;
import com.ooyala.android.testapp.R;
import com.ooyala.android.BaseStreamPlayer;

public class OoyalaAndroidTestAppActivity extends Activity implements OnClickListener, Observer, EmbedTokenGenerator {
  private static final String TAG = "OoyalaSampleApp";
  private OoyalaPlayer player;

  private Button skipAd;
  private Button insertAd;
  private Button setEmbed;

  private String APIKEY = "l4cGYxOngWCpNkwu6BKkna3XCPa6.FkFdk";
  private String SECRET = "gps8XQlMt2Qu5sQ0v7Km0b9sXWLnBB7zGbXiga3o";
  private String PCODE = "l4cGYxOngWCpNkwu6BKkna3XCPa6";
  private String EMBEDCODE = "J2MXl4ZjqXrUeKUkvXp8yXcxB6aQewHs";
  private String ACCOUNT_ID = "playbackDemo";
  private String PLAYERDOMAIN = "backlot.ooyala.com";

  private boolean metadataReady = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "TEST - onCreate");
    super.onCreate(savedInstanceState);
    Thread.setDefaultUncaughtExceptionHandler(onUncaughtException);
    try {
      setContentView(R.layout.main);
    } catch (Exception e) {
      e.printStackTrace();
    }

    skipAd = (Button) findViewById(R.id.skipAd);
    skipAd.setOnClickListener(this);
    insertAd = (Button) findViewById(R.id.insertAd);
    insertAd.setOnClickListener(this);
    setEmbed = (Button) findViewById(R.id.setEmbed);
    setEmbed.setOnClickListener(this);

    // optional localization
    // LocalizationSupport.useLocalizedStrings(LocalizationSupport.loadLocalizedStrings("ja_JP"));

    OptimizedOoyalaPlayerLayoutController layoutController = new OptimizedOoyalaPlayerLayoutController(
        (OoyalaPlayerLayout) findViewById(R.id.player), PCODE, PLAYERDOMAIN, this);
    player = layoutController.getPlayer();
    player.setAdsSeekable(true); // this will help us skip ads if need be.
    player.addObserver(this);
    player.addObserver(this);
  }

  private void setEmbedCode() {

    if (player.setEmbedCode(EMBEDCODE)) {
      player.play(60000);
    } else {
      Log.d(TAG, "setEmbedCode failed");
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (player != null) {
      player.suspend();
    }
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    if (player != null) {
      player.resume();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    player = null;
  }

  private Thread.UncaughtExceptionHandler onUncaughtException = new Thread.UncaughtExceptionHandler() {
    public void uncaughtException(Thread thread, Throwable ex) {
      Log.e(TAG, "Uncaught exception", ex);
      showErrorDialog(ex);
    }
  };

  private void showErrorDialog(Throwable t) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    builder.setTitle("Exception!");
    builder.setMessage(t.toString());
    builder.setPositiveButton("OK", null);
    builder.show();
  }

  @Override
  public void onClick(View arg0) {
    if (player != null && arg0 == setEmbed) {
      setEmbedCode();
    }
  }

  @Override
  public void update(Observable arg0, Object arg1) {
    Log.d(TAG, "Notification Recieved: " + arg1 + " - state: " + player.getState());
    if (arg1 == OoyalaPlayer.CONTENT_TREE_READY_NOTIFICATION) {
      metadataReady = true;
      Log.d(TAG, "AD - metadata true!");
    } else if (arg1 == OoyalaPlayer.METADATA_READY_NOTIFICATION) {
      Log.d(TAG, "Woot, here is the current metadata: " + player.getMetadata());
    }
  }

  @Override
  public void getTokenForEmbedCodes(List<String> embedCodes,
      EmbedTokenGeneratorCallback callback) {
    String embedCodesString = "";
    for (String ec : embedCodes) {
      if(ec.equals("")) embedCodesString += ",";
      embedCodesString += ec;
    }

    HashMap<String, String> params = new HashMap<String, String>();
    params.put("account_id", ACCOUNT_ID);

    String uri = "/sas/embed_token/" + PCODE + "/" + embedCodesString;
    EmbeddedSecureURLGenerator urlGen = new EmbeddedSecureURLGenerator(APIKEY, SECRET);

    URL tokenUrl  = urlGen.secureURL("http://player.ooyala.com", uri, params);

    callback.setEmbedToken(tokenUrl.toString());
  }


}
