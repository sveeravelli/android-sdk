package com.ooyala.android.visualontest;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OoyalaPlayerLayoutController;
import com.ooyala.android.visualontest.R;

public class VisualOnTestAppActivity extends Activity implements Observer {
  /** Called when the activity is first created. */
  OoyalaPlayer player;
  ArrayAdapter<String> playerAdapter;
  Spinner playerSpinner;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

//    private String EMBEDCODE = "lsaTl4ZDqr8k8Jw72UwBaUAB_hw4Ogc9"; //Telstra Playback
    //private String EMBEDCODE = "5jNzJuazpFtKmloYZQmgPeC_tqDKHX9r"; //Ooyala Playback


    playerSpinner = (Spinner) findViewById(R.id.playerSpinner);
    playerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
    playerSpinner.setAdapter(playerAdapter);
    playerAdapter.add("VisualOn");
    playerAdapter.add("Native Player");
    playerAdapter.notifyDataSetChanged();

    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    OoyalaPlayerLayoutController playerLayoutController = new OoyalaPlayerLayoutController(playerLayout,
    		"42Zms6h4wdcI1R1uFzepD-KZ0kkk", "Nr9o5l05rnycXeJ3qY699KJzH8PQiSUh51gd0YTq");
    player = playerLayoutController.getPlayer();

    player.addObserver(this);
    Button setButton = (Button) findViewById(R.id.setButton);

    EditText embedText = (EditText) findViewById(R.id.embedText);
    embedText.setText("5jNzJuazpFtKmloYZQmgPeC_tqDKHX9r");
    setButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        EditText embedText = (EditText) findViewById(R.id.embedText);

        if (player.setEmbedCode(embedText.getText().toString())) {
          // The Embed Code works: "l2d291ZDrxW9OMBaqz-0HeEhtLEM8MOa"
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