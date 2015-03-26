package com.ooyala.android.castsdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class CastUtils {

  /**
   * make JSON String to pass in information
   * @param action: Play/Pause/Seek
   * @return
   */
  public static String makeActionJSON(String action) {
    JSONObject newAction = new JSONObject();
    try {
      newAction.put("action", action);
    } catch (JSONException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    return newAction.toString();
  }

  /**
   * Takes in a URL and decode the URL into a Bitmap
   * @param url
   * @return
   */
  public static Bitmap decodeImageFromURL(String url) {
    Bitmap bitmap = null;
    try {
      bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return bitmap;
  }
}
