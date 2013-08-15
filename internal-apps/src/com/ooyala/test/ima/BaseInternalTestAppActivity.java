package com.ooyala.test.ima;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OptimizedOoyalaPlayerLayoutController;
import com.ooyala.android.imasdk.OoyalaIMAManager;
import com.ooyala.test.ima.R;

public class BaseInternalTestAppActivity extends Activity {

  Map<String, String> embedMap;
  Map<String, Class<? extends Activity>> activityMap;
  final String TAG = this.getClass().toString();
  final String PCODE  = "R2d3I6s06RyB712DN0_2GsQS-R-Y";
  final String DOMAIN = "ooyala.com";

  OptimizedOoyalaPlayerLayoutController playerLayoutController;
  OoyalaIMAManager imaManager;
  OoyalaPlayer player;
  Spinner embedSpinner;
  Button setButton;
  ArrayAdapter<String> embedAdapter;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {

    activityMap = new HashMap<String, Class<? extends Activity>>();
    activityMap.put(getString(R.string.coreItemName), CoreInternalTestAppActivity.class);
    activityMap.put(getString(R.string.imaItemName), IMAInternalTestAppActivity.class);
    activityMap.put(getString(R.string.ooyalaItemName), OoyalaAdsInternalTestAppActivity.class);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.test_set);

    //Initialize the player
    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    playerLayoutController = new OptimizedOoyalaPlayerLayoutController(playerLayout, PCODE, DOMAIN);
    player = playerLayoutController.getPlayer();

    //Initialize the bottom controls
    embedMap = new HashMap<String, String>();
    embedSpinner = (Spinner) findViewById(R.id.embedSpinner);
    setButton = (Button) findViewById(R.id.setButton);
    embedAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
    embedSpinner.setAdapter(embedAdapter);

    setButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        player.setEmbedCode(embedMap.get(embedSpinner.getSelectedItem()));
      }
    });
  }

  @Override
  protected void onStop() {
    super.onStop();
    Log.d(TAG, "App Stopped");
    if (playerLayoutController.getPlayer() != null) {
      playerLayoutController.getPlayer().suspend();
    }
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    Log.d(TAG, "App Restarted");
    if (playerLayoutController.getPlayer() != null) {
      playerLayoutController.getPlayer().resume();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.main_menu, menu);
      return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Class<? extends Activity> selectedClass = activityMap.get(item.getTitle().toString());

    if(selectedClass.equals(this.getClass())){
      Log.d(TAG, "Selected currently showing activity");
      return true;
    }

    Intent intent = new Intent(this, selectedClass);
    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    startActivity(intent);
    return true;
  }
}