package com.ooyala.android.loki;

import java.util.List;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.ooyala.android.EmbedTokenGenerator;
import com.ooyala.android.EmbedTokenGeneratorCallback;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OoyalaPlayerLayoutController;

public class LokiActivity extends Activity implements EmbedTokenGenerator {
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
      CharSequence text = "No embed code or pcode.";
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
     playerLayoutController = new OoyalaPlayerLayoutController(playerLayout,
        "R2d3I6s06RyB712DN0_2GsQS-R-Y.nCFrd", "UpmPCeDJspTKqLHO5IyZSRbsSiC7AM_rAqGztDRN",
        pcode, "www.ooyala.com", this);
    }
    else {
       playerLayoutController = new OoyalaPlayerLayoutController(playerLayout,
          "R2d3I6s06RyB712DN0_2GsQS-R-Y.nCFrd", "UpmPCeDJspTKqLHO5IyZSRbsSiC7AM_rAqGztDRN",
          pcode, "www.ooyala.com");
    }
    OoyalaPlayer player = playerLayoutController.getPlayer();
    if (embedCode != null && player.setEmbedCode(embedCode)) {
      // The Embed Code works
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
}