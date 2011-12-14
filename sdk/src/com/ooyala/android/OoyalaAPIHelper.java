package com.ooyala.android;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import org.json.*;

public class OoyalaAPIHelper
{
  private SecureURLGenerator _secureURLGenerator = null;

  public OoyalaAPIHelper(String apiKey, String secretKey)
  {
    _secureURLGenerator = new EmbeddedSecureURLGenerator(apiKey, secretKey);
  }

  public String jsonForSecureAPI(String host, String uri, Map<String,String> params)
  {
    URL url = _secureURLGenerator.secureURL(host, uri, params);
    return getRequest(url);
  }

  public JSONObject objectForSecureAPI(String host, String uri, Map<String,String> params)
  {
    String json = jsonForSecureAPI(host, uri, params);
    return objectForJson(json);
  }

  public static String jsonForAPI(String host, String uri, Map<String,String> params)
  {
    URL url = Utils.makeURL(host, uri, params);
    if (url == null) { return null; }
    return getRequest(url);
  }

  public static JSONObject objectForAPI(String host, String uri, Map<String,String> params)
  {
    String json = jsonForAPI(host, uri, params);
    return objectForJson(json);
  }

  private static JSONObject objectForJson(String json)
  {
    try
    {
      return (JSONObject) new JSONTokener(json).nextValue();
    }
    catch (JSONException exception)
    {
      System.out.println("JSONException: " + exception);
      return null;
    }
  }

  private static String getRequest(URL url)
  {
    StringBuffer sb = new StringBuffer();
    try
    {
      URLConnection conn = url.openConnection();

      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      while ((line = rd.readLine()) != null)
      {
        sb.append(line);
      }
      rd.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return sb.toString();
  }
}
