package com.ooyala.android.tvratingtestapp;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.PlayerDomain;
import com.ooyala.android.configuration.FCCTVRatingConfiguration;
import com.ooyala.android.configuration.Options;
import com.ooyala.android.configuration.FCCTVRatingConfiguration.Position;
import com.ooyala.android.ui.OoyalaPlayerLayoutController;
import com.ooyala.android.ui.AbstractOoyalaPlayerLayoutController.DefaultControlStyle;

public class TVRatingTestAppActivity extends Activity implements Observer {
  
  // todo: player choice ie. visual on.
  private static class MyMapRow {
    public final String embedCode;
    public final FCCTVRatingConfiguration tvRatingConfiguration;
    public MyMapRow( String embedCode, FCCTVRatingConfiguration tvRatingConfiguration ) {
      this.embedCode = embedCode;
      this.tvRatingConfiguration = tvRatingConfiguration;
    }
  }
  
  OoyalaPlayerLayout playerLayout;
  OoyalaPlayer player;
  ArrayAdapter<String> playerAdapter;
  Spinner playerSpinner;
  Spinner embedSpinner;
  HashMap<String, MyMapRow> embedMap;
  ArrayAdapter<String> embedAdapter;

  final String PCODE  = "42Zms6h4wdcI1R1uFzepD-KZ0kkk";
  final String DOMAIN = "http://www.ooyala.com";

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
    embedMap = new HashMap<String, MyMapRow>();
    FCCTVRatingConfiguration.Builder builder = new FCCTVRatingConfiguration.Builder();
    embedMap.put("Square TopLeft", new MyMapRow( "5od253bzo-9DTMTY5q45pX-PRRXa5c4d", builder.setPosition(Position.TopLeft).setDurationSeconds(5).build() ) );
    embedMap.put("Tall TopRight", new MyMapRow( "tnMG93bzr1X1IJn-Jcjehub0WlX138vg", builder.setPosition(Position.TopRight).setDurationSeconds(5).build() ) );
    embedMap.put("Wide BottomLeft", new MyMapRow( "VyN293bzq5EbxnKI0ff696-PSgHGStM6", builder.setPosition(Position.BottomLeft).setDurationSeconds(5).build() ) );
    embedMap.put("Square BottomRight", new MyMapRow( "5od253bzo-9DTMTY5q45pX-PRRXa5c4d", builder.setPosition(Position.BottomRight).setDurationSeconds(5).build() ) );
    embedAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
    embedSpinner.setAdapter(embedAdapter);

    //Update the spinner with the embed map
    embedAdapter.addAll(embedMap.keySet());
    embedAdapter.notifyDataSetChanged();

    Button setButton = (Button) findViewById(R.id.setButton);
    setButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if( player != null ) {
          player.pause();
        }
        
        FCCTVRatingConfiguration tvRatingConfiguration = embedMap.get(embedSpinner.getSelectedItem()).tvRatingConfiguration;
        loadPlayer( tvRatingConfiguration );
        
        String embedCode = embedMap.get(embedSpinner.getSelectedItem()).embedCode;
        if (player.setEmbedCode(embedCode)) {
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
  
  private void loadPlayer( FCCTVRatingConfiguration tvRatingConfiguration ) {
    LinearLayout mainLayout = (LinearLayout)findViewById( R.id.mainLayout );
    if( player != null ) {
      mainLayout.removeView( playerLayout );
      player.deleteObserver( this );
    }
    playerLayout = new OoyalaPlayerLayout( this );
    LinearLayout.LayoutParams pllp = new LinearLayout.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );
    mainLayout.addView( playerLayout, pllp );
    Options options = new Options.Builder().setTVRatingConfiguration(tvRatingConfiguration).build();
    OoyalaPlayerLayoutController playerLayoutController = new OoyalaPlayerLayoutController( playerLayout, PCODE, new PlayerDomain(DOMAIN), DefaultControlStyle.AUTO, null, options );
    player = playerLayoutController.getPlayer();
    player.addObserver(this);
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
}