package com.ooyala.android;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Utils
{
  public static URL makeURL(String host, String uri, Map<String,String> params)
  {
    return makeURL(host, uri, getParamsString(params, Constants.SEPARATOR_AMPERSAND));
  }

  public static URL makeURL(String host, String uri, String params)
  {
    try
    {
      return new URL("http://" + host + uri + (params == null || params.length() < 1 ? "" : "?" + params));
    }
    catch (MalformedURLException e)
    {
      e.printStackTrace();
    }
    return null;
  }

  public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c)
  {
    List<T> list = new ArrayList<T>(c);
    java.util.Collections.sort(list);
    return list;
  }

  public static String getParamsString(Map<String,String> params, String separator)
  {
    if (params == null || params.isEmpty()) { return ""; }

    StringBuffer result = new StringBuffer();
    boolean first = true;
    for (String key : asSortedList(params.keySet()))
    {
      if (first)
      {
        first = false;
      }
      else
      {
        result.append(separator);
      }

      result.append(key);
      result.append("=");
      result.append(params.get(key));
    }
    return result.toString();
  }

  public static <T> Set<T> getSubset(Map<String,T> objects, int firstIndex, int count)
  {
    if (firstIndex < 0 || firstIndex + count > objects.size()) { return null; }

    Iterator<T> iterator = objects.values().iterator();
    for (int i = 0; i < firstIndex && iterator.hasNext(); i++)
    {
      iterator.next();
    }

    Set<T> result = new HashSet<T>();
    for (int i = 0; i < count && iterator.hasNext(); i++)
    {
      result.add(iterator.next());
    }
    return result;
  }
}


