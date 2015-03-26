package com.ooyala.ooyalachromecastsampleapp;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.RemoteControlClient;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ooyala.android.castsdk.OOCastManager;
import com.ooyala.android.castsdk.OOMiniController;

import java.util.ArrayList;
import java.util.List;

public class ChromecastSampleAppActivity extends ActionBarActivity {
  
  public static int activatedActivity = 0;
  
  private RemoteControlClient remoteControlClient;
  private static final String TAG = "ChromscastSampleAppActivty";
  private final String NAMESPACE = "urn:x-cast:ooyala";
  private final String APP_ID = "F3A32677";
  private OOCastManager castManager;
  private OOMiniController defualtMiniController;
  private OOMiniController customizedMiniController;
  private View castView;
  private List<Integer> castViewImages;
  ListView _listView;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "onCreate()");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.start_view);
    String[] namespaces = {NAMESPACE};
    castManager = OOCastManager.initialize(this, APP_ID, namespaces);
    castManager.setStopOnDisconnect(false);
    castManager.setNotificationMiniControllerLayout(R.layout.custom_notification);
    castManager.setNotificationImageResourceId(R.drawable.ic_ooyala);
    
    castViewImages = new ArrayList<Integer>();
    castViewImages.add(R.drawable.test1);
    castViewImages.add(R.drawable.test2);
    castViewImages.add(R.drawable.test3);
    castViewImages.add(R.drawable.test4);
    castViewImages.add(R.drawable.test5);
    
    Video video_list[] = new Video[] {
        new Video(R.drawable.test1, "Ooyala Mexico Harlem Shake"),
        new Video(R.drawable.test2, "Super Corgi"),
        new Video(R.drawable.test3, "Arcade Fire - Reflecktor"),
        new Video(R.drawable.test4, "Google I/O Keynote"),
        new Video(R.drawable.test5, "Sweater Weather")
    };

    VideoListAdapter adapter = new VideoListAdapter(this, R.layout.listview_item_row, video_list);
    
    _listView = (ListView) findViewById(R.id.listView);
    _listView.setAdapter(adapter);
    

    LayoutInflater inflater = getLayoutInflater();
    castView = inflater.inflate(R.layout.cast_video_view, null);

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
        updateCastUI(position);
        castManager.setCastView(castView);
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
  
  
  private void updateCastUI(int position) {
    Log.d(TAG, "update cast mode UI infos");
    final ImageView castBackgroundImage = (ImageView) castView.findViewById(R.id.castBackgroundImage);
    castBackgroundImage.setImageResource(castViewImages.get(position));
    TextView videoTitle = (TextView) castView.findViewById(R.id.videoTitle);
    videoTitle.setText("TITLE");
    TextView videoDescription = (TextView) castView.findViewById(R.id.videoDescription);
    videoDescription.setText("VIDEO DESCRIPTION");
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    castManager.addCastButton(this, menu);
    return true;
  }

  @Override
  protected void onResume() {
    Log.d(TAG, "onResume()");
    ChromecastSampleAppActivity.activatedActivity++;
    castManager.addMiniController(defualtMiniController);
    castManager.addMiniController(customizedMiniController);
    this.castManager.onResume();
    super.onResume();
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
    castManager.destroy(this);
    castManager = null;
    super.onDestroy();
  }
  
  @Override
  public void onPause() {
    super.onPause();
    ChromecastSampleAppActivity.activatedActivity--;
    Log.d(TAG, "onPause()");
    castManager.removeMiniController(defualtMiniController);
    castManager.removeMiniController(customizedMiniController);
  }
  

}
