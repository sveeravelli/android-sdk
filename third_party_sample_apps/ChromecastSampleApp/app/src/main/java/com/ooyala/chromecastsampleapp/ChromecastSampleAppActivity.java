package com.ooyala.chromecastsampleapp;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.RemoteControlClient;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ooyala.android.castsdk.OOCastManager;
import com.ooyala.android.castsdk.OOMiniController;

import java.util.List;

public class ChromecastSampleAppActivity extends ActionBarActivity {
  
  public static int activatedActivity = 0;
  
  private RemoteControlClient remoteControlClient;
  private static final String TAG = "ChromscastSampleAppActivty";
  private final String NAMESPACE = "urn:x-cast:ooyala";
  private final String APP_ID = "46147917";
  private OOCastManager castManager;
  private OOMiniController defualtMiniController;
  private OOMiniController customizedMiniController;
  private List<Integer> castViewImages;
  ListView _listView;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "onCreate()");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.start_view);
    castManager = OOCastManager.initialize(this, APP_ID, NAMESPACE);
    castManager.setStopOnDisconnect(false);
    castManager.setNotificationMiniControllerLayout(R.layout.custom_notification);
    castManager.setNotificationImageResourceId(R.drawable.ic_ooyala);
    
    Video videoList[] = new Video[] {
        new Video(R.drawable.test1, "Ooyala Mexico Harlem Shake"),
        new Video(R.drawable.test2, "Super Corgi"),
        new Video(R.drawable.test3, "Arcade Fire - Reflecktor"),
        new Video(R.drawable.test4, "Google I/O Keynote"),
        new Video(R.drawable.test5, "Sweater Weather")
    };

    VideoListAdapter adapter = new VideoListAdapter(this, R.layout.listview_item_row, videoList);
    
    _listView = (ListView) findViewById(R.id.listView);
    _listView.setAdapter(adapter);



    final Intent intent = new Intent(this, PlayerStartingActivity.class);
    _listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        if (position == 0) {
          // smooth asset
          intent.putExtra("embedcode", "wxaWd5bTrJFI--Ga7TgbJtzcPrbzENBV");
        } else if (position == 1) {
          intent.putExtra("embedcode", "IzNGg3bzoHHjEfnJP-fj2jB0-oci0Jnm");
        } else if (position == 2) {
          intent.putExtra("embedcode", "xiNmg3bzpFkkwsYqkb5UtGvNOpcwiOCS");
        } else if (position == 3) {
          intent.putExtra("embedcode", "Y4OWg3bzoNtSZ9TOg3wl9BPUspXZiMYc");
        } else {
          intent.putExtra("embedcode", "o0OWg3bzrLBNfadaXSaCA7HbknPLFRPP");
        }
        startActivity(intent);
      }
    });
    
    ActionBar actionBar = getSupportActionBar();
    actionBar.setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    getSupportActionBar().setDisplayShowTitleEnabled(false);
    
    
    defualtMiniController = (OOMiniController) findViewById(R.id.miniController1);
    castManager.addMiniController(defualtMiniController);
    
    customizedMiniController = (OOMiniController) findViewById(R.id.miniController2);
    castManager.addMiniController(customizedMiniController);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    castManager.addCastButton(this, menu);
    return true;
  }

  @Override
  protected void onStop() {
    Log.d(TAG, "onStop()");
    super.onStop();
    if (ChromecastSampleAppActivity.activatedActivity == 0 && castManager != null && castManager.isInCastMode()) {
      castManager.createNotificationService(this, PlayerStartingActivity.class);
      castManager.registerLockScreenControls(this);
    }
  }
  
  @Override
  protected void onRestart() {
    Log.d(TAG, "onRestart()");
    super.onRestart();
    castManager.destroyNotificationService(this);
    castManager.unregisterLockScreenControls();
  }

  
  @Override
  protected void onStart() {
    Log.d(TAG, "onStart()");
    super.onStart();
  }
  
  @Override
  protected void onDestroy() {
    Log.d(TAG, "onDestroy()");
//    castManager.destroy(this);
//    castManager = null;
    super.onDestroy();
  }

  @Override
  public void onResume() {
    super.onResume();
    ChromecastSampleAppActivity.activatedActivity++;
    if (castManager != null && castManager.isInCastMode()){
      this.defualtMiniController.show();
      this.customizedMiniController.show();
      castManager.onResume();
    }
    Log.d(TAG, "onResume()");
  }
  
  @Override
  public void onPause() {
    super.onPause();
    ChromecastSampleAppActivity.activatedActivity--;
    Log.d(TAG, "onPause()");
  }
  

}
