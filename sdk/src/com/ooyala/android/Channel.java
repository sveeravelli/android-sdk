package com.ooyala.android;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ooyala.android.Constants.ReturnState;
import com.ooyala.android.OoyalaError.OoyalaErrorCode;

public class Channel extends ContentItem implements PaginatedParentItem
{
  protected LinkedHashMap<String,Video>_videos = new LinkedHashMap<String,Video>();
  protected ChannelSet _parent = null;
  protected String _nextChildren = null;
  protected boolean _isFetchingMoreChildren = false;

  public Channel()
  {
  }

  public Channel(JSONObject data, String embedCode, PlayerAPIClient api)
  {
    this(data, embedCode, null, api);
  }

  public Channel(JSONObject data, String embedCode, ChannelSet parent, PlayerAPIClient api)
  {
    _embedCode = embedCode;
    _api = api;
    _parent = parent;
    update(data);
  }

  public synchronized ReturnState update(JSONObject data)
  {
    switch (super.update(data))
    {
      case STATE_FAIL:
        return ReturnState.STATE_FAIL;
      case STATE_UNMATCHED:
        for (Video video : _videos.values())
        {
          video.update(data);
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
        for (Video video : _videos.values())
        {
          video.update(data);
        }
        return ReturnState.STATE_MATCHED;
      }

      if (!myData.isNull(Constants.KEY_CONTENT_TYPE) && !myData.getString(Constants.KEY_CONTENT_TYPE).equals(Constants.CONTENT_TYPE_CHANNEL))
      {
        System.out.println("ERROR: Attempted to initialize Channel with content_type: " + myData.getString(Constants.KEY_CONTENT_TYPE));
        return ReturnState.STATE_FAIL;
      }

      _nextChildren = myData.isNull(Constants.KEY_NEXT_CHILDREN) ? null : myData.getString(Constants.KEY_NEXT_CHILDREN);

      if (myData.isNull(Constants.KEY_CHILDREN))
      {
        if (_nextChildren == null)
        {
          System.out.println("ERROR: Attempted to initialize Channel with children == nil and next_children == nil: " + _embedCode);
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
          if (!child.isNull(Constants.KEY_CONTENT_TYPE) && !child.getString(Constants.KEY_CONTENT_TYPE).equals(Constants.CONTENT_TYPE_VIDEO))
          {
            HashMap<String, JSONObject> childMap = new HashMap<String, JSONObject>();
            String childEmbedCode = child.getString(Constants.KEY_EMBED_CODE);
            childMap.put(childEmbedCode, child);
            JSONObject childData = new JSONObject(childMap);
            Video existingChild = _videos.get(childEmbedCode);
            if (existingChild == null)
            {
              addVideo(new Video(childData, childEmbedCode, this, _api));
            }
            else
            {
              existingChild.update(childData);
            }
          }
          else
          {
            System.out.println("ERROR: Invalid Video content_type: " + child.getString(Constants.KEY_CONTENT_TYPE));
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
    if (_videos == null || _videos.size() == 0) { return null; }
    return _videos.values().iterator().next();
  }

  public Video lastVideo()
  {
    if (_videos == null || _videos.size() == 0) { return null; }
    Video video = null;
    Iterator<Video> iterator = _videos.values().iterator();
    while (iterator.hasNext())
    {
      video = iterator.next();
    }
    return video;
  }

  public Video nextVideo(Video currentItem)
  {
    if (_videos != null && _videos.size() > 0)
    {
      Iterator<Video> iterator = _videos.values().iterator();
      while (iterator.hasNext())
      {
        Video video = iterator.next();
        if (video.getEmbedCode().equals(currentItem.getEmbedCode()))
        {
          if (iterator.hasNext())
          {
            return iterator.next();
          }
          break;
        }
      }
    }
    return _parent == null ? null : _parent.nextVideo(this);
  }

  public Video previousVideo(Video currentItem)
  {
    if (_videos != null && _videos.size() > 0)
    {
      Video prevVideo = null;
      Video video = null;
      Iterator<Video> iterator = _videos.values().iterator();
      while (iterator.hasNext())
      {
        prevVideo = video;
        video = iterator.next();
        if (video.getEmbedCode().equals(currentItem.getEmbedCode()))
        {
          if (prevVideo != null)
          {
            return prevVideo;
          }
          break;
        }
      }
    }
    return _parent == null ? null : _parent.previousVideo(this);
  }

  protected void addVideo(Video video)
  {
    _videos.put(video.getEmbedCode(), video);
  }

  public int childrenCount()
  {
    return _videos.size();
  }

  public Collection<Video> getVideos()
  {
    return _videos.values();
  }

  public int getDuration()
  {
    int totalDuration = 0;
    for (Video video : _videos.values()) { totalDuration += video.getDuration(); }
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
      Object responseObject = _api.contentTreeNext(_nextChildren, this);
      if (responseObject instanceof OoyalaError)
      {
        _listener.onItemsFetched(-1, 0, (OoyalaError)responseObject);
        _isFetchingMoreChildren = false;
        return;
      }

      PaginatedItemResponse response = (PaginatedItemResponse) responseObject;
      if (response.firstIndex < 0)
      {
        _listener.onItemsFetched(response.firstIndex, response.count, new OoyalaError(OoyalaErrorCode.ERROR_CONTENT_TREE_NEXT_FAILED, "No additional children found"));
        _isFetchingMoreChildren = false;
        return;
      }

      Set<String> childEmbedCodesToAuthorize = ContentItem.getEmbedCodes(Utils.getSubset(_videos, response.firstIndex, response.count));
      boolean authorized = _api.authorizeEmbedCodes(childEmbedCodesToAuthorize, this);
      if (authorized)
      {
        _listener.onItemsFetched(response.firstIndex, response.count, null);
      }
      else
      {
        _listener.onItemsFetched(response.firstIndex, response.count, new OoyalaError(OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED, "Additional child authorization failed"));
      }
      _isFetchingMoreChildren = false;
      return;
    }
  }
}
