package com.ooyala.android.sampleapp;

import com.ooyala.android.Channel;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OoyalaPlayerLayoutController;
import com.ooyala.android.Video;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OoyalaAndroidSampleAppActivity extends Activity implements OnClickListener {
  private static final String TAG = "OoyalaAndroidSampleAppActivity";

  private OoyalaPlayer player = null;

  private class ChannelBrowserItemView extends TextView {
    private Video video = null;

    public ChannelBrowserItemView(Context context) {
      super(context);
    }

    public ChannelBrowserItemView(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    public ChannelBrowserItemView(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
    }

    public void setVideo(Video v, float weight) {
      video = v;
      this.setText(video.getTitle());
      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                   ViewGroup.LayoutParams.MATCH_PARENT,
                                                                   weight);
      this.setLayoutParams(lp);
      this.setGravity(Gravity.CENTER);
      this.requestLayout();
      this.setTextSize(18);
    }

    public Video getVideo() {
      return video;
    }
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    LinearLayout channelBrowser = (LinearLayout)findViewById(R.id.channelBrowser);
    OoyalaPlayerLayout layout = (OoyalaPlayerLayout)findViewById(R.id.ooyalaPlayer);
    // Initializer Params: (api key, secret, pcode, domain)
    OoyalaPlayerLayoutController layoutController = new OoyalaPlayerLayoutController(layout, "l1am06xhbSxa0OtyZsBTshW2DMtp.qDW-_", "GkUqcxL-5aeVBYG71aYQmlkMh62iBRgq8O-d6Y5w", "l1am06xhbSxa0OtyZsBTshW2DMtp", "www.ooyala.com");
    player = layoutController.getPlayer();
    if (player.setEmbedCode("B0eHAxMzqsbVRm0ZJROXw1Yaj73roQu6")){//"NueXAxMzqnfCtqVrgaEoD4-N8sFrt-nt")) { // this is a channel's embed code
      // The embed code was set properly, we can play the video and/or access the rootItem now.
      if (player.getRootItem() instanceof Channel) {
        // Here we set up a list of the embed codes in the channel, then set up an OnClickListener to change
        // to that embed code
        Channel rootItem = ((Channel)player.getRootItem());
        for(Video v : rootItem.getVideos()) {
          ChannelBrowserItemView browserItemView = new ChannelBrowserItemView(channelBrowser.getContext());
          browserItemView.setClickable(true);
          browserItemView.setOnClickListener(this); // see onClick for what happens when we click on this
          browserItemView.setVideo(v, 1f/((float)rootItem.getVideos().size()));
          channelBrowser.addView(browserItemView);
        }
      } else {
        // This will never happen. better safe than sorry though.
        Log.e(TAG,"This will never happen. But it did. Freaky.");
      }
      // and after we set up the list, lets play the first video
      player.play();
    } else {
      // Something wrong happened. check player.getError() for details
      Log.e(TAG,"Unable to set embed code: "+player.getError().getCode());
    }
  }

  @Override
  public void onClick(View arg0) {
    if (arg0 instanceof ChannelBrowserItemView) {
      if (player.changeCurrentVideo(((ChannelBrowserItemView)arg0).getVideo())) {
        // success
      } else {
        // something bad happened, check player.getError() for details
        Log.e(TAG,"Unable to change video.");
      }
    } else {
      // ignore. we didn't click on a ChannelBrowserItemView we set up
    }
  }
}