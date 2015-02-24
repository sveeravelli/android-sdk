package com.ooyala.android.secureplayersample;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.ooyala.android.EmbedTokenGenerator;
import com.ooyala.android.EmbedTokenGeneratorCallback;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.PlayerDomain;
import com.ooyala.android.configuration.Options;
import com.ooyala.android.configuration.VisualOnConfiguration;
import com.ooyala.android.ui.AbstractOoyalaPlayerLayoutController.DefaultControlStyle;
import com.ooyala.android.ui.OoyalaPlayerLayoutController;

public class SecurePlayerSampleAppActivity extends Activity implements Observer, EmbedTokenGenerator {
  OoyalaPlayer player;
  ArrayAdapter<String> playerAdapter;
  Spinner playerSpinner;
  Spinner embedSpinner;
  HashMap<String, String> embedMap;
  ArrayAdapter<String> embedAdapter;


  private String APIKEY = "Use this for testing, don't keep your secret in the application";
  private String SECRET = "Use this for testing, don't keep your secret in the application";
  private String ACCOUNT_ID = "accountID";
  final String PCODE  = "N5dGEyOrMsKgdLgNp2B0wirtpqm7";
  final String DOMAIN = "http://www.ooyala.com";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

      OoyalaPlayer.setEnvironment(com.ooyala.android.Environment.EnvironmentType.STAGING);
      OoyalaPlayer.enableCustomPlayreadyPlayer = true;

    //Initialize the bottom controls
    embedSpinner = (Spinner) findViewById(R.id.embedSpinner);
    playerSpinner = (Spinner) findViewById(R.id.playerSpinner);
    playerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
    playerSpinner.setAdapter(playerAdapter);
    playerAdapter.add("VisualOn/SecurePlayer");
    playerAdapter.add("Native Player");
    playerAdapter.notifyDataSetChanged();

    //Populate the embed map
    embedMap = new LinkedHashMap<String, String>();
    embedMap.put("Device Management - Device Bind to Entitlement", "Q3NmpoczpUH__SVSKRI0BbFl3A9CtHSL");
    embedMap.put("Device Management - Device Limit", "0xNmpoczpeNkx6Pq8ZOPwPUu6CuzFKeY");
    embedMap.put("OPL Test - A150 C500 U301", "01Nmpoczq_GLtFUuTyy6mfQzkGjTIl9F");
    embedMap.put("OPL Test - A150 C500 U300", "0zNmpoczrbFOt-jK9wWNABrpKlSDduxN");
    embedMap.put("OPL Test - A150 C500 U250", "15NWpoczoxGzZRc2g_rqNA7WSMrSrdak");
    embedMap.put("OPL Test - A201 C500 U250", "13NWpoczpBVeg8eUyswxFioYmJIOzTje");

    embedAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
    embedSpinner.setAdapter(embedAdapter);

    //Update the spinner with the embed map
    embedAdapter.addAll(embedMap.keySet());
    embedAdapter.notifyDataSetChanged();

    VisualOnConfiguration visualOnConfiguration = new VisualOnConfiguration.Builder().setDisableLibraryVersionChecks(false).build();
    Options.Builder builder = new Options.Builder().setVisualOnConfiguration(visualOnConfiguration);
    Options options = builder.build();
    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    OoyalaPlayerLayoutController playerLayoutController = new OoyalaPlayerLayoutController(playerLayout,
        PCODE, new PlayerDomain(DOMAIN),DefaultControlStyle.AUTO, this, options);
    player = playerLayoutController.getPlayer();

    player.addObserver(this);
    Button setButton = (Button) findViewById(R.id.setButton);

    setButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {

        if (player.setEmbedCode(embedMap.get(embedSpinner.getSelectedItem()))) {
          TextView urlText = (TextView) findViewById(R.id.urlText);
          urlText.setText("");

          if(playerSpinner.getSelectedItem().toString()  == "Native Player") {
            OoyalaPlayer.enableCustomHLSPlayer = false;
            OoyalaPlayer.enableCustomPlayreadyPlayer = false;
          } else {
            OoyalaPlayer.enableCustomHLSPlayer = true;
            OoyalaPlayer.enableCustomPlayreadyPlayer = true;
          }

          player.play();
        } else {
          Log.d(this.getClass().getName(), "Something Went Wrong!");
        }
      }
    });

  }

  @Override
  protected void onPause() {
    super.onPause();
    if (player != null && player.getState() != State.SUSPENDED) {
      player.suspend();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (player != null && player.getState() == State.SUSPENDED) {
      player.resume();
    }
  }

  @Override
  public void update(Observable observable, Object data) {
    // TODO Implement to listen to Ooyala Notifications
  }

  // This is a local method of generating an embed token for debugging.
  // It is unsafe to have your key and secrets in a production application
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