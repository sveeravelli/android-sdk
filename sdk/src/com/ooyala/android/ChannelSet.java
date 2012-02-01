package com.ooyala.android;

import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ooyala.android.Constants.ReturnState;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;

public class ChannelSet extends ContentItem implements PaginatedParentItem {
  protected OrderedMap<String, Channel> _channels = new OrderedMap<String, Channel>();
  protected String _nextChildren = null;
  protected boolean _isFetchingMoreChildren = false;

  ChannelSet() {}

  ChannelSet(JSONObject data, String embedCode, PlayerAPIClient api) {
    this(data, embedCode, null, api);
  }

  ChannelSet(JSONObject data, String embedCode, ChannelSet parent, PlayerAPIClient api) {
    _embedCode = embedCode;
    _api = api;
    update(data);
  }

  @Override
  /** For internal use only.
   * Update the AuthorizableItem using the specified data (subclasses should override and call this)
   * @param data the data to use to update this AuthorizableItem
   * @return a ReturnState based on if the data matched or not (or parsing failed)
   */
  public synchronized ReturnState update(JSONObject data) {
    switch (super.update(data)) {
      case STATE_FAIL:
        return ReturnState.STATE_FAIL;
      case STATE_UNMATCHED:
        for (Channel channel : _channels) {
          channel.update(data);
        }
        return ReturnState.STATE_UNMATCHED;
      default:
        break;
    }

    try {
      JSONObject myData = data.getJSONObject(_embedCode);
      if (!myData.isNull(Constants.KEY_AUTHORIZED) && myData.getBoolean(Constants.KEY_AUTHORIZED)) {
        for (Channel channel : _channels) {
          channel.update(data);
        }
        return ReturnState.STATE_MATCHED;
      }

      if (!myData.isNull(Constants.KEY_CONTENT_TYPE)
          && !myData.getString(Constants.KEY_CONTENT_TYPE).equals(Constants.CONTENT_TYPE_CHANNEL_SET)) {
        System.out.println("ERROR: Attempted to initialize ChannelSet with content_type: "
            + myData.getString(Constants.KEY_CONTENT_TYPE));
        return ReturnState.STATE_FAIL;
      }

      _nextChildren = myData.isNull(Constants.KEY_NEXT_CHILDREN) ? null : myData
          .getString(Constants.KEY_NEXT_CHILDREN);

      if (myData.isNull(Constants.KEY_CHILDREN)) {
        if (_nextChildren == null) {
          System.out
              .println("ERROR: Attempted to initialize ChannelSet with children == nil and next_children == nil: "
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
              && child.getString(Constants.KEY_CONTENT_TYPE).equals(Constants.CONTENT_TYPE_CHANNEL)) {
            HashMap<String, JSONObject> childMap = new HashMap<String, JSONObject>();
            String childEmbedCode = child.getString(Constants.KEY_EMBED_CODE);
            childMap.put(childEmbedCode, child);
            JSONObject childData = new JSONObject(childMap);
            Channel existingChild = _channels.get(childEmbedCode);
            if (existingChild == null) {
              addChannel(new Channel(childData, childEmbedCode, this, _api));
            } else {
              existingChild.update(childData);
            }
          } else {
            System.out.println("ERROR: Invalid Channel content_type: "
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
   * Get the first Video for this ChannelSet
   * @return the first Video this ChannelSet represents
   */
  public Video firstVideo() {
    if (_channels == null || _channels.size() == 0) { return null; }
    return _channels.get(0).firstVideo();
  }

  /**
   * Get the next Video for this ChannelSet (this method should only be called at the end of a channel)
   * @param currentItem the current Channel
   * @return the next Video from ChannelSet
   */
  public Video nextVideo(Channel currentItem) {
    int idx = _channels.indexForValue(currentItem);
    if (idx < 0 || ++idx >= _channels.size()) { return null; }
    return _channels.get(idx).firstVideo();
  }

  /**
   * Get the previous Video for this ChannelSet (this method should only be called at the end of a channel)
   * @param currentItem the current Channel
   * @return the previous Video from ChannelSet
   */
  public Video previousVideo(Channel currentItem) {
    int idx = _channels.indexForValue(currentItem);
    if (idx < 0 || --idx < 0) { return null; }
    return _channels.get(idx).lastVideo();
  }

  protected void addChannel(Channel channel) {
    _channels.put(channel.getEmbedCode(), channel);
  }

  /**
   * The number of channels this ChannelSet has. Same as getChannels().size().
   * @return an int with the number of channels
   */
  public int childrenCount() {
    return _channels.size();
  }

  public OrderedMap<String, Channel> getChannels() {
    return _channels;
  }

  /**
   * The total duration (not including Ads) of this ChannelSet
   * @return an int with the total duration in seconds
   */
  public int getDuration() {
    int totalDuration = 0;
    for (Channel channel : _channels) {
      totalDuration += channel.getDuration();
    }
    return totalDuration;
  }

  /**
   * Find out it this ChannelSet has more children
   * @return true if it does, false if it doesn't
   */
  public boolean hasMoreChildren() {
    return _nextChildren != null;
  }

  /**
   * Fetch the additional children if they exist
   * @param listener the listener to execute when the children are fetched
   * @return true if more children exist, false if they don't or they are already in the process of being
   *         fetched
   */
  public boolean fetchMoreChildren(PaginatedItemListener listener) {
    // The two lines below aren't within a synchronized block because we assume single thread
    // of execution except for the threads we explicitly spawn below, but those set
    // _isFetchingMoreChildren = false at the very end of their execution.
    if (!hasMoreChildren() || _isFetchingMoreChildren) { return false; }
    _isFetchingMoreChildren = true;

    Thread thread = new Thread(new NextChildrenRunner(_nextChildren, listener));
    thread.start();
    return true;
  }

  private class NextChildrenRunner implements Runnable {
    private String _nextChildren = null;
    private PaginatedItemListener _listener = null;

    public NextChildrenRunner(String nextChildren, PaginatedItemListener listener) {
      _nextChildren = nextChildren;
      _listener = listener;
    }

    public void run() {
      PaginatedItemResponse response = _api.contentTreeNext(_nextChildren, ChannelSet.this);
      if (response == null) {
        _listener.onItemsFetched(-1, 0, new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED,
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

      List<String> childEmbedCodesToAuthorize = ContentItem.getEmbedCodes(_channels.subList(
          response.firstIndex, response.firstIndex + response.count));
      try {
        if (_api.authorizeEmbedCodes(childEmbedCodesToAuthorize, ChannelSet.this)) {
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
  /**
   * Get the Video in this ChannelSet with the specified embed code
   * @param embedCode the embed code to look up
   * @param currentItem the current Video
   * @return the video in this ChannelSet with the specified embed code
   */
  public Video videoFromEmbedCode(String embedCode, Video currentItem) {
    // search through channelset starting with currentItem's channel
    // get first channels index
    int start = (currentItem == null) ? 0 : _channels.indexForValue(currentItem.getParent());
    int i = start;
    do {
      Video v = _channels.get(i).videoFromEmbedCode(embedCode, currentItem);
      if (v != null) { return v; }
      i = i >= _channels.size() ? 0 : i + 1;
    } while (i != start);
    return null;
  }
}
