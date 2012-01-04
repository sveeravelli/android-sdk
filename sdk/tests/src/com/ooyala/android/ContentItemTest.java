package com.ooyala.android;

import java.io.InputStream;
import java.util.Scanner;

import org.json.*;

import android.test.AndroidTestCase;

public class ContentItemTest extends AndroidTestCase
{
  public ContentItemTest()
  {
    super();
  }

  protected void setUp()
  {

  }

  protected void tearDown()
  {

  }

  public static JSONObject getTestJSON(String file)
  {
    InputStream inputStream = ContentItemTest.class.getResourceAsStream(file);
    String json = new Scanner(inputStream).useDelimiter("\\A").next();
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
}
