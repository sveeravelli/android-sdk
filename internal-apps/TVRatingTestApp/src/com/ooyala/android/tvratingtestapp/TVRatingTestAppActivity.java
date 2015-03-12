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
import com.ooyala.android.configuration.FCCTVRatingConfiguration.Position;
import com.ooyala.android.configuration.Options;
import com.ooyala.android.ui.AbstractOoyalaPlayerLayoutController.DefaultControlStyle;
import com.ooyala.android.ui.OoyalaPlayerLayoutController;

public class TVRatingTestAppActivity extends Activity implements Observer {

  private static final int DURATION = 3;

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

    //Populate the embed map
    embedMap = new HashMap<String, MyMapRow>();
    FCCTVRatingConfiguration.Builder builder = new FCCTVRatingConfiguration.Builder();
    embedMap.put("MidRoll null default", new MyMapRow( "pncmp0ZDp7OKlwTPJlMZzrI59j8Imefa", null ) );
    embedMap.put("MidRoll TopLeft", new MyMapRow( "pncmp0ZDp7OKlwTPJlMZzrI59j8Imefa", builder.setPosition(Position.TopLeft).setDurationSeconds(DURATION).build() ) );
    embedMap.put("Tall TopRight", new MyMapRow( "tnMG93bzr1X1IJn-Jcjehub0WlX138vg", builder.setPosition(Position.TopRight).setDurationSeconds(DURATION).build() ) );
    embedMap.put("Wide BottomLeft", new MyMapRow( "VyN293bzq5EbxnKI0ff696-PSgHGStM6", builder.setPosition(Position.BottomLeft).setDurationSeconds(DURATION).build() ) );
    embedMap.put("Square BottomRight", new MyMapRow( "5od253bzo-9DTMTY5q45pX-PRRXa5c4d", builder.setPosition(Position.BottomRight).setDurationSeconds(DURATION).build() ) );
    embedAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
    embedSpinner.setAdapter(embedAdapter);

    //Update the spinner with the embed map
    for (String key : embedMap.keySet()) {
      embedAdapter.add(key);
    }
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

//          player.play();
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
    Options options = tvRatingConfiguration == null ? null : new Options.Builder().setTVRatingConfiguration(tvRatingConfiguration).build();
    player = new OoyalaPlayer(PCODE, new PlayerDomain(DOMAIN), options);
    OoyalaPlayerLayoutController playerLayoutController = new OoyalaPlayerLayoutController( playerLayout, player, DefaultControlStyle.AUTO );
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