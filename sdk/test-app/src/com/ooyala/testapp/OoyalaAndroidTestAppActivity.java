package com.ooyala.testapp;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.ooyala.android.AdvertisingIdUtils;
import com.ooyala.android.EmbedTokenGenerator;
import com.ooyala.android.EmbedTokenGeneratorCallback;
import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.PlayerDomain;
import com.ooyala.android.testapp.R;
import com.ooyala.android.ui.OptimizedOoyalaPlayerLayoutController;

public class OoyalaAndroidTestAppActivity extends Activity implements OnClickListener, Observer, EmbedTokenGenerator, AdvertisingIdUtils.IAdvertisingIdListener {
  private static final String TAG = "OoyalaSampleApp";
  private OoyalaPlayer player;

  private Button skipAd;
  private Button insertAd;
  private Button setEmbed;

  private String APIKEY = "";
  private String SECRET = "";
  private String PCODE = "NoeGo6WRLOIhAQ1x9F7zzKKKuFOh";
  private String EMBEDCODE = "poNTl5ZDoOwns_09h8NxYZg24onVl1V6";
  private String ACCOUNT_ID = "pbk-373@ooyala.com";
  private String PLAYERDOMAIN = "http://www.ooyala.com";

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
    PlayerDomain domain = null;
    try {
      domain = new PlayerDomain(PLAYERDOMAIN);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    OptimizedOoyalaPlayerLayoutController layoutController = new OptimizedOoyalaPlayerLayoutController(
        (OoyalaPlayerLayout) findViewById(R.id.player), PCODE, domain, this);
    player = layoutController.getPlayer();
    player.setAdsSeekable(true); // this will help us skip ads if need be.
    player.addObserver(this);
    player.addObserver(this);
    int r = player.beginFetchingAdvertisingId(this, this);
    Log.d( TAG, "initAdvertisingId: " + r );
  }

  public void onAdvertisingIdSuccess( String adId ) {
    Log.d( TAG, "onAdvertisingIdSuccess: " + adId );
  }

  public void onAdvertisingIdError( OoyalaException oe ) {
    Log.e( TAG, "onAdvertisingIdError", oe );
  }

  private void setEmbedCode() {

    if (player.setEmbedCode(EMBEDCODE)) {
      player.play();
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
