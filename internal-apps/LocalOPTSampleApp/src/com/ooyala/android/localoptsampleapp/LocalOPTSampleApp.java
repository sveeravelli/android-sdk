package com.ooyala.android.localoptsampleapp;

import java.net.URL;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.ooyala.android.EmbedTokenGenerator;
import com.ooyala.android.EmbedTokenGeneratorCallback;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.PlayerDomain;
import com.ooyala.android.ui.OoyalaPlayerLayoutController;

/**
 * This application shows a way to locally generate Ooyala Player Tokens.  This method is for debugging
 * only, as this will expose your API Secret publicly if used in an application.
 *
 */
public class LocalOPTSampleApp extends Activity implements EmbedTokenGenerator{

  final String EMBED  = "0yMjJ2ZDosUnthiqqIM3c8Eb8Ilx5r52";  //Embed Code, or Content ID
  final String PCODE  = "c0cTkxOqALQviQIGAHWY5hP0q9gU";
  final String DOMAIN = "http://www.ooyala.com";

  // These are only for Local OPT Generation when debugging
  // Do not distribute your API Key and Secret in any application
  private final String APIKEY = "__Your API Key__";
  private final String SECRET = "__Your Secret__";
  private final String ACCOUNT_ID = "__Your Account ID__";

  OoyalaPlayer player;
  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    player = new OoyalaPlayer(PCODE, new PlayerDomain(DOMAIN), this, null);
    OoyalaPlayerLayoutController playerLayoutController = new OoyalaPlayerLayoutController(playerLayout, player);

    if (player.setEmbedCode(EMBED)) {
      player.play();
    } else {
      Log.d(this.getClass().getName(), "Something Went Wrong!");
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

  // This method will be called whenever the Ooyala Player requires a new OPT.  You have to generate a token
  // and provide it through the EmbedTokenGeneratorCallback.setEmbedToken().
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
