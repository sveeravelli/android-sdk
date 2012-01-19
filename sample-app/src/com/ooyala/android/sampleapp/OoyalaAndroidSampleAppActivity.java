package com.ooyala.android.sampleapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ooyala.android.Channel;
import com.ooyala.android.ContentItem;
import com.ooyala.android.OoyalaAPIClient;
import com.ooyala.android.OoyalaException;
import com.ooyala.android.Utils;

import com.ooyala.android.Video;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class OoyalaAndroidSampleAppActivity extends ListActivity {
  private static final String TAG = "OoyalaAndroidSampleAppActivity";

  public static final String PCODE = "R2d3I6s06RyB712DN0_2GsQS-R-Y";
  public static final String APIKEY = "R2d3I6s06RyB712DN0_2GsQS-R-Y.nCFrd";
  public static final String SECRETKEY = "UpmPCeDJspTKqLHO5IyZSRbsSiC7AM_rAqGztDRN";
  public static final String PLAYERDOMAIN = "www.ooyala.com";
  
  public static final String CHANNEL_CODE = "txaGRiMzqQZSmFpMML92QczdIYUrcYVe";
  
  public static OoyalaAPIClient api = new OoyalaAPIClient(APIKEY, SECRETKEY, PCODE, PLAYERDOMAIN);

  private String[]embedCodes = { CHANNEL_CODE };
  private Channel rootItem = null;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ContentItem item = null;
    try {
        item = api.contentTree(Arrays.asList(embedCodes));
    } catch (OoyalaException e) {
    	Log.e(TAG, "can not find content tree from api");
    }
    if (item != null && item instanceof Channel) {
    	rootItem = (Channel)item;
        setListAdapter(new OoyalaVideoListAdapter(this, getData(),
                R.layout.embed_list_item, new String[] { "title", "thumbnail", "duration" },
                new int[] { R.id.asset_title, R.id.asset_thumbnail, R.id.asset_duration }));
        getListView().setTextFilterEnabled(false);

    } else {
    	Log.e(TAG, "Should not be here!");
    }
    
  }

  protected List<Map<String, Object>> getData() {
	  if (rootItem == null) return null;
      List<Map<String, Object>> myData = new ArrayList<Map<String, Object>>();

  	  for(Video v : rootItem.getVideos()) {
  		addItem(myData, v.getTitle(), v.getDuration(), v.getPromoImageURL(50, 50), browseIntent(v.getEmbedCode()));
	  }
      return myData;
  }  
 
  protected Intent browseIntent(String embedCode) {
      Intent result = new Intent();
      result.setClass(this, PlayerDetailActivity.class);
      result.putExtra("com.ooyala.embedcode", embedCode);
      return result;
  }

  protected void addItem(List<Map<String, Object>> data, String name, int duration, String thumbnail, Intent intent) {
      Map<String, Object> temp = new HashMap<String, Object>();
      temp.put("title", name);
      temp.put("duration", Utils.timeStringFromMillis(duration));
      temp.put("thumbnail", thumbnail);
      temp.put("intent", intent);
      data.add(temp);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void onListItemClick(ListView l, View v, int position, long id) {
      Map<String, Object> map = (Map<String, Object>)l.getItemAtPosition(position);
      Intent intent = (Intent) map.get("intent");
      startActivity(intent);
  }  

}