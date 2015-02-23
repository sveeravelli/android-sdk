package com.ooyala.android.nielsensample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.PlayerDomain;
import com.ooyala.android.nielsensdk.NielsenAnalytics;
import com.ooyala.android.ui.OoyalaPlayerLayoutController;
import com.ooyala.android.util.DebugMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Observable;
import java.util.Observer;

public class NielsenSampleAppActivity extends Activity implements Observer {
  private final static String TAG = NielsenSampleAppActivity.class.getSimpleName();
  private final static String PCODE = "42Zms6h4wdcI1R1uFzepD-KZ0kkk";
  private final static String DOMAIN = "http://www.ooyala.com";
  private final static String NIELSEN_SFCODE = "UAT-CERT";
  private final static String NIELSEN_APPID = "T70BC66D4-C904-4DA1-AB9D-BB658F70E9A7";

  OoyalaPlayer player;
  Spinner embedSpinner;
  HashMap<String, String> embedMap;
  ArrayAdapter<String> embedAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    DebugMode.setMode( DebugMode.Mode.LogAndAbort );

    setContentView(R.layout.main);
    embedSpinner = (Spinner) findViewById(R.id.embedSpinner);
    embedMap = new LinkedHashMap<String, String>();
    embedMap.put("Linear", "84aDVmcTqN3FrdLXClZgJq-GfFEDhS1a");
    embedMap.put("Dynamic", "M3bmM3czp1j9horxoTLGaJtgLmW57u4F");
    embedMap.put("CMS", "ZhMmkycjr4jlHIjvpIIimQSf_CjaQs48");
    embedAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
    embedSpinner.setAdapter(embedAdapter);
    for (String key : embedMap.keySet()) {
      embedAdapter.add(key);
    }
    embedAdapter.notifyDataSetChanged();

    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    OoyalaPlayerLayoutController playerLayoutController = new OoyalaPlayerLayoutController(playerLayout,
        PCODE, new PlayerDomain(DOMAIN));
    player = playerLayoutController.getPlayer();

    final NielsenAnalytics nielsenAnalytics = new NielsenAnalytics( this, player, "NielsenTestApp", "0.1", NIELSEN_SFCODE, NIELSEN_APPID, player.getID3TagNotifier(), getCustomConfig(), getCustomMetadata() );

    player.addObserver(this);
    Button setButton = (Button) findViewById(R.id.setButton);

    setButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        final String embed = embedMap.get(embedSpinner.getSelectedItem());
        if (player.setEmbedCode(embed)) {
          TextView urlText = (TextView) findViewById(R.id.urlText);
          urlText.setText("");
          OoyalaPlayer.enableCustomHLSPlayer = true;
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
    if (player != null && player.getState() != OoyalaPlayer.State.SUSPENDED) {
      player.suspend();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (player != null && player.getState() == OoyalaPlayer.State.SUSPENDED) {
      player.resume();
    }
  }

  @Override
  public void update(Observable observable, Object data) {
    Log.d( TAG, "update: " + data );
  }

  private JSONObject getCustomConfig() {
    final JSONObject json = new JSONObject();
    try {
      json.put( "tv", "false" );
      json.put( "nol_devDebug", "true" ); // do NOT do this for production apps!
    }
    catch( JSONException e ) {
      Log.e( TAG, "getCustomConfig()", e );
    }
    return json;
  }

  private JSONObject getCustomMetadata() {
    return null;
  }
}