package com.ooyala.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.ooyala.android.LocalizationSupport;
import com.ooyala.android.OoyalaNotification;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.PlayerDomain;
import com.ooyala.android.configuration.Options;
import com.ooyala.android.performance.PerformanceMonitor;
import com.ooyala.android.ui.OptimizedOoyalaPlayerLayoutController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class BaseInternalTestAppActivity extends Activity implements OnClickListener, Observer {

  protected Map<String, String> embedMap;
  final String TAG = this.getClass().toString();
  final String PCODE  = "R2d3I6s06RyB712DN0_2GsQS-R-Y";
  final String DOMAIN = "http://ooyala.com";

  protected OptimizedOoyalaPlayerLayoutController playerLayoutController;
  protected OoyalaPlayer player;
  protected Spinner embedSpinner;
  protected Button setButton;
  protected ArrayAdapter<String> embedAdapter;
  protected PerformanceMonitor performanceMonitor;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    String localeString = getResources().getConfiguration().locale.toString();
    Log.d(TAG, "locale is " + localeString);
    LocalizationSupport.useLocalizedStrings(LocalizationSupport
        .loadLocalizedStrings(localeString));

    super.onCreate(savedInstanceState);
    setContentView(R.layout.test_case_layout);

    //Initialize the player
    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    PlayerDomain domain = new PlayerDomain(DOMAIN);
    Options options = new Options.Builder().setShowAdsControls(false)
        .setShowCuePoints(false).setShowPromoImage(true)
        .setPreloadContent(false).setUseExoPlayer(true).build();
    player = new OoyalaPlayer(PCODE, domain, options);
    playerLayoutController = new OptimizedOoyalaPlayerLayoutController(playerLayout, player);
    player.addObserver(this);
    performanceMonitor = PerformanceMonitor.getStandardMonitor( player );

    //Initialize the bottom controls
    embedMap = new LinkedHashMap<String, String>();
    embedSpinner = (Spinner) findViewById(R.id.embedSpinner);
    setButton = (Button) findViewById(R.id.setButton);
    embedAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
    embedSpinner.setAdapter(embedAdapter);

    setButton.setOnClickListener(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    Log.d(TAG, "App Paused");
    if (playerLayoutController.getPlayer() != null) {
      playerLayoutController.getPlayer().suspend();
    }
    if( performanceMonitor != null ) {
      Log.d( TAG, performanceMonitor.buildStatisticsSnapshot().generateReport() );
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if( performanceMonitor != null ) {
      performanceMonitor.destroy();
      performanceMonitor = null;
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.d(TAG, "App Resumed");
    if (playerLayoutController.getPlayer() != null) {
      playerLayoutController.getPlayer().resume();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    for(String key : InternalTestApplication.getActivityMap().keySet()) {
      MenuItem item = menu.add(key);
      item.setTitleCondensed(InternalTestApplication.getCondensedActivityNameMap().get(key));
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Class<? extends Activity> selectedClass = InternalTestApplication.getActivityMap().get(item.getTitle().toString());

    if(selectedClass.equals(this.getClass())){
      Log.d(TAG, "Selected currently showing activity");
      return true;
    }

    Intent intent = new Intent(this, selectedClass);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    startActivity(intent);
    return true;
  }

  @Override
  public void onClick(View v) {
    player.setEmbedCode(embedMap.get(embedSpinner.getSelectedItem()));
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (playerLayoutController.onKeyUp(keyCode, event)) {
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public void update(Observable arg0, Object arg1) {
    final String notificationName = OoyalaNotification.getNameOrUnknown(arg1);
    if (notificationName == OoyalaPlayer.TIME_CHANGED_NOTIFICATION_NAME) {
      return;
    }
    Log.d(TAG, "Notification Recieved: " + arg1 + " - state: " + player.getState());
  }

}