package com.ooyala.android;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ooyala.android.Constants.ReturnState;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;

public class ChannelSet extends ContentItem implements PaginatedParentItem
{
  protected LinkedHashMap<String,Channel> _channels = new LinkedHashMap<String,Channel>();
  protected String _nextChildren = null;
  protected boolean _isFetchingMoreChildren = false;

  public ChannelSet()
  {
  }

  public ChannelSet(JSONObject data, String embedCode, PlayerAPIClient api)
  {
    this(data, embedCode, null, api);
  }

  public ChannelSet(JSONObject data, String embedCode, ChannelSet parent, PlayerAPIClient api)
  {
    _embedCode = embedCode;
    _api = api;
    update(data);
  }

  public synchronized ReturnState update(JSONObject data)
  {
    switch (super.update(data))
    {
      case STATE_FAIL:
        return ReturnState.STATE_FAIL;
      case STATE_UNMATCHED:
        for (Channel channel : _channels.values())
        {
          channel.update(data);
        }
        return ReturnState.STATE_UNMATCHED;
      default:
        break;
    }

    try
    {
      JSONObject myData = data.getJSONObject(_embedCode);
      if (!myData.isNull(Constants.KEY_AUTHORIZED) && myData.getBoolean(Constants.KEY_AUTHORIZED))
      {
        for (Channel channel : _channels.values())
        {
          channel.update(data);
        }
        return ReturnState.STATE_MATCHED;
      }

      if (!myData.isNull(Constants.KEY_CONTENT_TYPE) && !myData.getString(Constants.KEY_CONTENT_TYPE).equals(Constants.CONTENT_TYPE_CHANNEL_SET))
      {
        System.out.println("ERROR: Attempted to initialize ChannelSet with content_type: " + myData.getString(Constants.KEY_CONTENT_TYPE));
        return ReturnState.STATE_FAIL;
      }

      _nextChildren = myData.isNull(Constants.KEY_NEXT_CHILDREN) ? null : myData.getString(Constants.KEY_NEXT_CHILDREN);

      if (myData.isNull(Constants.KEY_CHILDREN))
      {
        if (_nextChildren == null)
        {
          System.out.println("ERROR: Attempted to initialize ChannelSet with children == nil and next_children == nil: " + _embedCode);
          return ReturnState.STATE_FAIL;
        }
        return ReturnState.STATE_MATCHED;
      }

      JSONArray children = myData.getJSONArray(Constants.KEY_CHILDREN);
      if (children.length() > 0)
      {
        for (int i = 0; i < children.length(); i++)
        {
          JSONObject child = children.getJSONObject(i);
          if (!child.isNull(Constants.KEY_CONTENT_TYPE) && child.getString(Constants.KEY_CONTENT_TYPE).equals(Constants.CONTENT_TYPE_CHANNEL))
          {
            HashMap<String, JSONObject> childMap = new HashMap<String, JSONObject>();
            String childEmbedCode = child.getString(Constants.KEY_EMBED_CODE);
            childMap.put(childEmbedCode, child);
            JSONObject childData = new JSONObject(childMap);
            Channel existingChild = _channels.get(childEmbedCode);
            if (existingChild == null)
            {
              addChannel(new Channel(childData, childEmbedCode, this, _api));
            }
            else
            {
              existingChild.update(childData);
            }
          }
          else
          {
            System.out.println("ERROR: Invalid Channel content_type: " + child.getString(Constants.KEY_CONTENT_TYPE));
          }
        }
      }
    }
    catch (JSONException exception)
    {
      System.out.println("JSONException: " + exception);
      return ReturnState.STATE_FAIL;
    }

    return ReturnState.STATE_MATCHED;
  }

  public Video firstVideo()
  {
    if (_channels == null || _channels.size() == 0) { return null; }
    return _channels.values().iterator().next().firstVideo();
  }

  public Video lastVideo()
  {
    if (_channels == null || _channels.size() == 0) { return null; }
    Channel channel = null;
    Iterator<Channel> iterator = _channels.values().iterator();
    while (iterator.hasNext())
    {
      channel = iterator.next();
    }
    return channel.firstVideo();
  }

  public Video nextVideo(Channel currentItem)
  {
    if (_channels == null || _channels.size() == 0) { return null; }
    Iterator<Channel> iterator = _channels.values().iterator();
    while (iterator.hasNext())
    {
      Channel channel = iterator.next();
      if (channel.getEmbedCode().equals(currentItem.getEmbedCode()))
      {
        if (iterator.hasNext())
        {
          return iterator.next().firstVideo();
        }
        break;
      }
    }
    return null;
  }

  public Video previousVideo(Channel currentItem)
  {
    if (_channels == null || _channels.size() == 0) { return null; }
    Channel prevChannel = null;
    Channel channel = null;
    Iterator<Channel> iterator = _channels.values().iterator();
    while (iterator.hasNext())
    {
      prevChannel = channel;
      channel = iterator.next();
      if (channel.getEmbedCode().equals(currentItem.getEmbedCode()))
      {
        if (prevChannel != null)
        {
          return prevChannel.lastVideo();
        }
        break;
      }
    }
    return null;
  }

  protected void addChannel(Channel channel)
  {
    _channels.put(channel.getEmbedCode(), channel);
  }

  public int childrenCount()
  {
    return _channels.size();
  }

  public LinkedHashMap<String,Channel> getChannels()
  {
    return _channels;
  }

  public int getDuration()
  {
    int totalDuration = 0;
    for (Channel channel : _channels.values()) { totalDuration += channel.getDuration(); }
    return totalDuration;
  }

  public boolean hasMoreChildren()
  {
    return _nextChildren != null;
  }

  public boolean fetchMoreChildren(PaginatedItemListener listener)
  {
    // The two lines below aren't within a synchronized block because we assume single thread
    // of execution except for the threads we explicitly spawn below, but those set
    // _isFetchingMoreChildren = false at the very end of their execution.
    if (!hasMoreChildren() || _isFetchingMoreChildren) { return false; }
    _isFetchingMoreChildren = true;

    Thread thread = new Thread(new NextChildrenRunner(_nextChildren, listener));
    thread.start();
    return true;
  }

  private class NextChildrenRunner implements Runnable
  {
    private String _nextChildren = null;
    private PaginatedItemListener _listener = null;

    public NextChildrenRunner(String nextChildren, PaginatedItemListener listener)
    {
      _nextChildren = nextChildren;
      _listener = listener;
    }

    public void run()
    {
      PaginatedItemResponse response = _api.contentTreeNext(_nextChildren, ChannelSet.this);
      if (response == null)
      {
        _listener.onItemsFetched(-1, 0, new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED, "Null response"));
        _isFetchingMoreChildren = false;
        return;
      }

      if (response.firstIndex < 0)
      {
        _listener.onItemsFetched(response.firstIndex, response.count,
                                 new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_NEXT_FAILED, "No additional children found"));
        _isFetchingMoreChildren = false;
        return;
      }

      List<String> childEmbedCodesToAuthorize = ContentItem.getEmbedCodes(Utils.getSubset(_channels, response.firstIndex, response.count));
      boolean authorized = _api.authorizeEmbedCodes(childEmbedCodesToAuthorize, ChannelSet.this);
      if (authorized)
      {
        _listener.onItemsFetched(response.firstIndex, response.count, null);
      }
      else
      {
        _listener.onItemsFetched(response.firstIndex, response.count,
                                 new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED, "Additional child authorization failed"));
      }
      _isFetchingMoreChildren = false;
      return;
    }
  }
}
