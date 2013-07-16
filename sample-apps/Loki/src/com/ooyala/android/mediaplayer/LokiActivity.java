package com.ooyala.android.mediaplayer;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ooyala.android.EmbedTokenGenerator;
import com.ooyala.android.EmbedTokenGeneratorCallback;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OoyalaPlayerLayoutController;

public class LokiActivity extends Activity implements EmbedTokenGenerator, Observer {
  /** Called when the activity is first created. */

  String token = null;
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    Uri data = getIntent().getData();
    List<String> params = null;
    String embedCode = null;
    String pcode = null;
    if (data != null) { 
      params = data.getPathSegments();
      pcode = data.getHost();
      embedCode = params.get(0);
      token = params.size() > 1 ? params.get(1) : null;
    }
    
    if (embedCode == null || pcode == null) {
      CharSequence text = "Please navigate back to your website and play the video";
      int duration = Toast.LENGTH_LONG;
      Toast toast = Toast.makeText(this, text, duration);
      toast.show();
      this.finish();
      return;
    }
    
    //String embedCode = params == null ? "lrZmRiMzrr8cP77PPW0W8AsjjhMJ1BBe" : data.getHost();
    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    
    OoyalaPlayerLayoutController playerLayoutController;
    if(token != null) {
     playerLayoutController = new OoyalaPlayerLayoutController(playerLayout, pcode, "www.ooyala.com", this);
    }
    else {
       playerLayoutController = new OoyalaPlayerLayoutController(playerLayout, pcode, "www.ooyala.com");
    }
    OoyalaPlayer player = playerLayoutController.getPlayer();
    //this will be used in the future to enable Third party HLS for Loki
    //player.setLoki();
    if (embedCode != null && player.setEmbedCode(embedCode)) {
      // The Embed Code works
      player.addObserver(this);
      player.play();
    } else {
      CharSequence text = "Invalid embed code";
      int duration = Toast.LENGTH_LONG;
      Toast toast = Toast.makeText(this, text, duration);
      toast.show();
      Log.d(this.getClass().getName(), "Playback Failed");
    }
  }
  @Override
  public void getTokenForEmbedCodes(List<String> arg0,
      EmbedTokenGeneratorCallback callback) {
        if (token == null) return;
        callback.setEmbedToken(token);
    // TODO Auto-generated method stub
    
  }
  @Override
  public void update(Observable observable, Object data) {
    if (data.toString() == "playCompleted") {
      this.finish();
    }
  }
}