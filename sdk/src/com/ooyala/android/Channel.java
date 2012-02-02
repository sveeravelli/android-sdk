package com.ooyala.android;

import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ooyala.android.Constants.ReturnState;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;

public class Channel extends ContentItem implements PaginatedParentItem {
  protected OrderedMap<String, Video> _videos = new OrderedMap<String, Video>();
  protected ChannelSet _parent = null;
  protected String _nextChildren = null;
  protected boolean _isFetchingMoreChildren = false;

  Channel() {}

  Channel(JSONObject data, String embedCode, PlayerAPIClient api) {
    this(data, embedCode, null, api);
  }

  Channel(JSONObject data, String embedCode, ChannelSet parent, PlayerAPIClient api) {
    _embedCode = embedCode;
    _api = api;
    _parent = parent;
    update(data);
  }

  @Override
  public synchronized ReturnState update(JSONObject data) {
    switch (super.update(data)) {
      case STATE_FAIL:
        return ReturnState.STATE_FAIL;
      case STATE_UNMATCHED:
        for (Video video : _videos) {
          video.update(data);
        }
        return ReturnState.STATE_UNMATCHED;
      default:
        break;
    }

    try {
      JSONObject myData = data.getJSONObject(_embedCode);
      if (!myData.isNull(Constants.KEY_AUTHORIZED) && myData.getBoolean(Constants.KEY_AUTHORIZED)) {
        for (Video video : _videos) {
          video.update(data);
        }
        return ReturnState.STATE_MATCHED;
      }

      if (!myData.isNull(Constants.KEY_CONTENT_TYPE)
          && !myData.getString(Constants.KEY_CONTENT_TYPE).equals(Constants.CONTENT_TYPE_CHANNEL)) {
        System.out.println("ERROR: Attempted to initialize Channel with content_type: "
            + myData.getString(Constants.KEY_CONTENT_TYPE));
        return ReturnState.STATE_FAIL;
      }

      _nextChildren = myData.isNull(Constants.KEY_NEXT_CHILDREN) ? null : myData
          .getString(Constants.KEY_NEXT_CHILDREN);

      if (myData.isNull(Constants.KEY_CHILDREN)) {
        if (_nextChildren == null) {
          System.out
              .println("ERROR: Attempted to initialize Channel with children == nil and next_children == nil: "
                  + _embedCode);
          return ReturnState.STATE_FAIL;
        }
        return ReturnState.STATE_MATCHED;
      }

      JSONArray children = myData.getJSONArray(Constants.KEY_CHILDREN);
      if (children.length() > 0) {
        for (int i = 0; i < children.length(); i++) {
          JSONObject child = children.getJSONObject(i);
          if (!child.isNull(Constants.KEY_CONTENT_TYPE)
              && child.getString(Constants.KEY_CONTENT_TYPE).equals(Constants.CONTENT_TYPE_VIDEO)) {
            HashMap<String, JSONObject> childMap = new HashMap<String, JSONObject>();
            String childEmbedCode = child.getString(Constants.KEY_EMBED_CODE);
            childMap.put(childEmbedCode, child);
            JSONObject childData = new JSONObject(childMap);
            Video existingChild = _videos.get(childEmbedCode);
            if (existingChild == null) {
              addVideo(new Video(childData, childEmbedCode, this, _api));
            } else {
              existingChild.update(childData);
            }
          } else {
            System.out.println("ERROR: Invalid Video content_type: "
                + child.getString(Constants.KEY_CONTENT_TYPE));
          }
        }
      }
    } catch (JSONException exception) {
      System.out.println("JSONException: " + exception);
      return ReturnState.STATE_FAIL;
    }

    return ReturnState.STATE_MATCHED;
  }

  /**
   * Get the first Video for this Channel
   * 
   * @return the first Video this Channel represents
   */
  public Video firstVideo() {
    if (_videos == null || _videos.size() == 0) { return null; }
    return _videos.get(0);
  }

  /**
   * Get the last Video for this Channel
   * 
   * @return the last Video this Channel represents
   */
  public Video lastVideo() {
    if (_videos == null || _videos.size() == 0) { return null; }
    return _videos.get(_videos.size() - 1);
  }

  /**
   * Get the next Video for this Channel
   * 
   * @param currentItem the current Video
   * @return the next Video from Channel
   */
  public Video nextVideo(Video currentItem) {
    int index = _videos.indexForValue(currentItem);
    if (index < 0 || ++index >= _videos.size()) { return _parent == null ? null : _parent.nextVideo(this); }
    return _videos.get(index);
  }

  /**
   * Get the previous Video for this Channel
   * 
   * @param currentItem the current Video
   * @return the previous Video from Channel
   */
  public Video previousVideo(Video currentItem) {
    int index = _videos.indexForValue(currentItem);
    if (index < 0 || --index < 0) { return _parent == null ? null : _parent.previousVideo(this); }
    return _videos.get(index);
  }

  protected void addVideo(Video video) {
    _videos.put(video.getEmbedCode(), video);
  }

  @Override
  public int childrenCount() {
    return _videos.size();
  }

  public OrderedMap<String, Video> getVideos() {
    return _videos;
  }

  /**
   * The total duration (not including Ads) of this Channel
   * 
   * @return an int with the total duration in seconds
   */
  public int getDuration() {
    int totalDuration = 0;
    for (Video video : _videos) {
      totalDuration += video.getDuration();
    }
    return totalDuration;
  }

  @Override
  public boolean hasMoreChildren() {
    return _nextChildren != null;
  }

  @Override
  public String getNextChildren() {
    return _nextChildren;
  }

  @Override
  public boolean fetchMoreChildren(PaginatedItemListener listener) {
    // The two lines below aren't within a synchronized block because we assume
    // single thread
    // of execution except for the threads we explicitly spawn below, but those
    // set
    // _isFetchingMoreChildren = false at the very end of their execution.
    if (!hasMoreChildren() || _isFetchingMoreChildren) { return false; }
    _isFetchingMoreChildren = true;

    Thread thread = new Thread(new NextChildrenRunner(listener));
    thread.start();
    return true;
  }

  private class NextChildrenRunner implements Runnable {
    private PaginatedItemListener _listener = null;

    public NextChildrenRunner(PaginatedItemListener listener) {
      _listener = listener;
    }

    public void run() {
      PaginatedItemResponse response = _api.contentTreeNext(Channel.this);
      if (response == null) {
        _listener.onItemsFetched(-1, 0, new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_NEXT_FAILED,
            "Null response"));
        _isFetchingMoreChildren = false;
        return;
      }

      if (response.firstIndex < 0) {
        _listener.onItemsFetched(response.firstIndex, response.count, new OoyalaException(
            OoyalaErrorCode.ERROR_CONTENT_TREE_NEXT_FAILED, "No additional children found"));
        _isFetchingMoreChildren = false;
        return;
      }

      List<String> childEmbedCodesToAuthorize = ContentItem.getEmbedCodes(_videos.subList(
          response.firstIndex, response.firstIndex + response.count));
      try {
        if (_api.authorizeEmbedCodes(childEmbedCodesToAuthorize, Channel.this)) {
          _listener.onItemsFetched(response.firstIndex, response.count, null);
        } else {
          _listener.onItemsFetched(response.firstIndex, response.count, new OoyalaException(
              OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED, "Additional child authorization failed"));
        }
      } catch (OoyalaException e) {
        _listener.onItemsFetched(response.firstIndex, response.count, e);
      }
      _isFetchingMoreChildren = false;
      return;
    }
  }

  @Override
  public Video videoFromEmbedCode(String embedCode, Video currentItem) {
    return _videos.get(embedCode);
  }

  public ChannelSet getParent() {
    return _parent;
  }
}
