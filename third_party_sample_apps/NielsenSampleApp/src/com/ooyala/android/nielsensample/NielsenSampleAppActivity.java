package com.ooyala.android.nielsensample;

import java.util.HashMap;
import java.util.LinkedHashMap;
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

import com.ooyala.android.DebugMode;
import com.ooyala.android.NielsenAnalytics;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.PlayerDomain;
import com.ooyala.android.ui.OoyalaPlayerLayoutController;

public class NielsenSampleAppActivity extends Activity implements Observer {
  OoyalaPlayer player;
  Spinner embedSpinner;
  HashMap<String, String> embedMap;
  ArrayAdapter<String> embedAdapter;

  final String PCODE  = "42Zms6h4wdcI1R1uFzepD-KZ0kkk";
  final String DOMAIN = "http://www.ooyala.com";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    DebugMode.setMode( DebugMode.Mode.LogAndAbort );

    setContentView(R.layout.main);
    //Initialize the bottom controls
    embedSpinner = (Spinner) findViewById(R.id.embedSpinner);
    //Populate the embed map
    embedMap = new LinkedHashMap<String, String>();
    embedMap.put("nielsen", "84aDVmcTqN3FrdLXClZgJq-GfFEDhS1a");
    embedAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
    embedSpinner.setAdapter(embedAdapter);
    //Update the spinner with the embed map
    for (String key : embedMap.keySet()) {
      embedAdapter.add(key);
    }
    embedAdapter.notifyDataSetChanged();

    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    OoyalaPlayerLayoutController playerLayoutController = new OoyalaPlayerLayoutController(playerLayout,
        PCODE, new PlayerDomain(DOMAIN));
    player = playerLayoutController.getPlayer();

    NielsenAnalytics.s_setNielsenConfiguration(this, "NielsenSampleApp", "0.1", "sfcode-unknown", "appid-unknown" );
    player.setNielsenAnalytics( new NielsenAnalytics( "testChannel" ) );

    player.addObserver(this);
    Button setButton = (Button) findViewById(R.id.setButton);

    setButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (player.setEmbedCode(embedMap.get(embedSpinner.getSelectedItem()))) {
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
    // TODO Implement to listen to Ooyala Notifications
  }
}