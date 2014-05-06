package com.ooyala.android.visualontest;

import java.util.HashMap;
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

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.PlayerDomain;
import com.ooyala.android.ui.OoyalaPlayerLayoutController;

public class VisualOnTestAppActivity extends Activity implements Observer {
  /** Called when the activity is first created. */
  OoyalaPlayer player;
  ArrayAdapter<String> playerAdapter;
  Spinner playerSpinner;
  Spinner embedSpinner;
  HashMap<String, String> embedMap;
  ArrayAdapter<String> embedAdapter;

//  private String EMBEDCODE = "5jNzJuazpFtKmloYZQmgPeC_tqDKHX9r"; //Ooyala Playready Sample VOD
  private String EMBEDCODE = "dqZGhyazpuZePSDwyVR2AxtuLFzqRB68"; // Telstra Playready Live Stream
//  private String EMBEDCODE = "N0dXJ3azp-cKR8gG_SxAGVi3im8O0c8T"; //Telstra Clear Live Stream
//  private String EMBEDCODE = "A1MXN3azpsp0sPbGTsIZLknwSFsFPnL2"; //Telstra Clear Single Bitrate Live Stream
//  private String EMBEDCODE = "tkZmhyazr-ekNG8wb5kNWA_LV3E8QiPY"; //Playready-Provided Sample VOD

  final String PCODE  = "42Zms6h4wdcI1R1uFzepD-KZ0kkk";
  final String DOMAIN = "www.ooyala.com";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    //Initialize the bottom controls
    embedSpinner = (Spinner) findViewById(R.id.embedSpinner);
    playerSpinner = (Spinner) findViewById(R.id.playerSpinner);
    playerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
    playerSpinner.setAdapter(playerAdapter);
    playerAdapter.add("VisualOn");
    playerAdapter.add("Native Player");
    playerAdapter.notifyDataSetChanged();


    //Populate the embed map
    embedMap = new HashMap<String, String>();
    embedMap.put("Telstra Encrypted Multi Bitrate Playready Live Stream",    "dqZGhyazpuZePSDwyVR2AxtuLFzqRB68");
    embedMap.put("Telstra Clear Multi Bitrate Live Stream", "N0dXJ3azp-cKR8gG_SxAGVi3im8O0c8T");
    embedMap.put("Telstra Clear Single Bitrate Live Stream",       "A1MXN3azpsp0sPbGTsIZLknwSFsFPnL2");
    embedMap.put("Ooyala Playready Sample VOD",    "5jNzJuazpFtKmloYZQmgPeC_tqDKHX9r");
    embedMap.put("Playready-Provided Sample VOD",      "tkZmhyazr-ekNG8wb5kNWA_LV3E8QiPY");

    embedAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
    embedSpinner.setAdapter(embedAdapter);

    //Update the spinner with the embed map
    embedAdapter.addAll(embedMap.keySet());
    embedAdapter.notifyDataSetChanged();

    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    OoyalaPlayerLayoutController playerLayoutController = new OoyalaPlayerLayoutController(playerLayout,
        PCODE, new PlayerDomain(DOMAIN));
    player = playerLayoutController.getPlayer();

    player.addObserver(this);
    Button setButton = (Button) findViewById(R.id.setButton);

//    EditText embedText = (EditText) findViewById(R.id.embedText);
//    embedText.setText(EMBEDCODE);
    setButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
//        EditText embedText = (EditText) findViewById(R.id.embedText);

        if (player.setEmbedCode(embedMap.get(embedSpinner.getSelectedItem()))) {
          TextView urlText = (TextView) findViewById(R.id.urlText);
          urlText.setText("");

          if(playerSpinner.getSelectedItem().toString()  == "Native Player") {
            OoyalaPlayer.enableCustomHLSPlayer = false;
          } else {
            OoyalaPlayer.enableCustomHLSPlayer = true;
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
    OoyalaPlayer player = (OoyalaPlayer) observable;
    String notification = data.toString();
    // TODO Auto-generated method stub
//    if (notification.equals(OoyalaPlayer.AUTHORIZATION_READY_NOTIFICATION)) {
//      TextView urlText = (TextView) findViewById(R.id.urlText);
//      urlText.setText(player.getCurrentItem().getStream().decodedURL().toString());
//    }
  }
}