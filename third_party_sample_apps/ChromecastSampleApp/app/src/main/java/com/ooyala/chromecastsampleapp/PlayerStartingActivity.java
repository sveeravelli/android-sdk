package com.ooyala.chromecastsampleapp;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.PlayerDomain;
import com.ooyala.android.castsdk.OOCastManager;
import com.ooyala.android.ui.OoyalaPlayerLayoutController;

public class PlayerStartingActivity extends ActionBarActivity {
  
  private static final String TAG = "PlayerStartingActivity";
  private static final double DEFAULT_VOLUME_INCREMENT = 0.05;
  private String embedCode;
  final String PCODE  = "R2d3I6s06RyB712DN0_2GsQS-R-Y";
  final String DOMAIN = "http://www.ooyala.com";
  private OoyalaPlayer player;
  private OOCastManager castManager;
  private View _view;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "onCreate()");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    embedCode = getIntent().getExtras().getString("embedcode");
    String castState = getIntent().getExtras().getString("castState");
    
    // Initialize Ooyala Player
    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    PlayerDomain domain = new PlayerDomain(DOMAIN);
    player = new OoyalaPlayer(PCODE, domain);
    OoyalaPlayerLayoutController playerLayoutController = new OoyalaPlayerLayoutController(playerLayout, player);

    String[] namespaces = {"urn:x-cast:ooyala"};
    castManager = OOCastManager.initialize(this, "F3A32677", namespaces);
    castManager.destroyNotificationService(this);
    castManager.registerWithOoyalaPlayer(player);
    castManager.setTargetActivity(PlayerStartingActivity.class);
    
    // Initialize action bar
    ActionBar actionBar = getSupportActionBar();
    actionBar.setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setTitle(R.string.app_name);
    getSupportActionBar().setDisplayShowTitleEnabled(false);
    
    player.setEmbedCode(embedCode);
    player.play();
  }

  @Override
  public void onPause() {
    Log.d(TAG, "onPause()");
    ChromecastSampleAppActivity.activatedActivity --;
    super.onPause();
  }
  
  @Override
  protected void onStart() {
    Log.d(TAG, "onStart()");
    super.onStart();
  }

  @Override
  protected void onStop() {
    Log.d(TAG, "onStop()");
    super.onStop();
    if (ChromecastSampleAppActivity.activatedActivity == 0 && castManager != null) {
      castManager.createNotificationService(this, PlayerStartingActivity.class);
      castManager.registerLockScreenControls(this);
    }
  }

  @Override
  protected void onRestart() {
    Log.d(TAG, "onRestart()");
    super.onRestart();
  }

  @Override
  protected void onDestroy() {
    Log.d(TAG, "onDestroy()");
    castManager.destroyNotificationService(this);
    castManager.unregisterLockScreenControls();
    castManager.disconnectOoyalaPlayer();
//    castManager.clearCastView();
    super.onDestroy();
  }

  @Override
  protected void onResume() {
    Log.d(TAG, "onResume()");
    ChromecastSampleAppActivity.activatedActivity++;

    if (castManager != null && castManager.getCurrentCastPlayer() != null) {
      castManager.destroyNotificationService(this);
      castManager.unregisterLockScreenControls();
      castManager.onResume();
    } else if (player != null) {
      player.resume();
    }  
  super.onResume();
    
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    Log.d(TAG, "onCreateOptionsMenu()");
    super.onCreateOptionsMenu(menu);
    castManager.addCastButton(this, menu);
    return true;
  }

 
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (castManager != null && castManager.getCurrentCastPlayer() != null) {
      if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
        Log.w(TAG, "KeyEvent.KEYCODE_VOLUME_UP");
        onVolumeChange(DEFAULT_VOLUME_INCREMENT);
      } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
        Log.w(TAG, "KeyEvent.KEYCODE_VOLUME_DOWN");
        onVolumeChange(-DEFAULT_VOLUME_INCREMENT);
      } else {
        // we don't want to consume non-volume key events
        return super.onKeyDown(keyCode, event);
      }
    }
    return super.onKeyDown(keyCode, event);
  }

  private void onVolumeChange(double volumeIncrement) {
    if (castManager == null) {
      return;
    }
    try {
      Log.w(TAG, "Increase DeviceVolume!!!!!!!!!!!" + volumeIncrement);
      castManager.incrementDeviceVolume(volumeIncrement);
    } catch (Exception e) {
      Log.e(TAG, "onVolumeChange() Failed to change volume", e);
    }
  }

}