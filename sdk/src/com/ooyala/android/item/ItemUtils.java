package com.ooyala.android.item;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

class ItemUtils {
  static final String SEPARATOR_TIME = ":";

  static Map<String, String> mapFromJSONObject(JSONObject obj) {
    Map<String, String> map = new HashMap<String, String>();

    if (obj == null) {
      return map;
    }

    Iterator<?> itr = obj.keys();
    while (itr.hasNext()) {
      String key = (String)itr.next();
      try {
        map.put(key, obj.getString(key));
      } catch (JSONException e) {
        //do nothing
      }
    }

    return map;
  }

  static boolean isNullOrEmpty(String string) {
    return string == null || string.equals("");
  }


  static double secondsFromTimeString(String time) {
    String[] hms = time.split(SEPARATOR_TIME);
    double multiplier = 1.0;
    double milliseconds = 0.0;
    for (int i = hms.length - 1; i >= 0; i--) {
      milliseconds += (Double.parseDouble(hms[i]) * multiplier);
      multiplier *= 60.0;
    }
    return milliseconds;
  }
}
