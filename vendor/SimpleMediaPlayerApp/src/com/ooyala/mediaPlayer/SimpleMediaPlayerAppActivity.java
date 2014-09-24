package com.ooyala.mediaPlayer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class SimpleMediaPlayerAppActivity extends Activity {

  SimpleMediaPlayer player = null;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sample_media_player_app);

     LinearLayout layout =  (LinearLayout) findViewById(R.id.main_layout);

    player = new SimpleMediaPlayer("http://ak.c.ooyala.com/h4aHB1ZDqV7hbmLEv4xSOx3FdUUuephx/DOcJ-FxaFrRg4gtDQwOmRiOjBrO7WKK2", layout);
//    player = new SimpleMediaPlayer("http://d3qyc75pm7cdm6.cloudfront.net/bbb_sunflower_2160p_60fps_normal_ooyala_HEVC_1500.mp4", layout);
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (player != null && player._player != null) {
      player._player.stop();
    }
  }
}
