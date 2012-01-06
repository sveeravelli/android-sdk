package com.ooyala.android;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.ooyala.android.Constants.ReturnState;

public class DynamicChannel extends Channel
{
  protected List<String> _embedCodes = null;

  public DynamicChannel()
  {
  }

  public DynamicChannel(JSONObject data, List<String> embedCodes, PlayerAPIClient api)
  {
    this(data, embedCodes, null, api);
  }

  public DynamicChannel(JSONObject data, List<String> embedCodes, ChannelSet parent, PlayerAPIClient api)
  {
    super(data, null, parent, api);
    _parent = parent;
    _embedCode = null;
    _embedCodes = embedCodes;
    _api = api;
    update(data);
  }

  public synchronized ReturnState update(JSONObject data)
  {
    switch (super.update(data))
    {
      case STATE_FAIL:
        return ReturnState.STATE_FAIL;
      default:
        break;
    }

    for (Video video : _videos.values())
    {
      video.update(data);
    }

    try
    {
      for (String videoEmbedCode : _embedCodes)
      {
        if (data.isNull(videoEmbedCode))
        {
          // Remove from _embedCodes
          _embedCodes.remove(videoEmbedCode);
        }
        else
        {
          JSONObject videoData = data.getJSONObject(videoEmbedCode);
          if (videoData.isNull(Constants.KEY_CONTENT_TYPE))
          {
            // do nothing, this is most likely an authorization response and if so, was handled in the previous loop
          }
          else if (videoData.getString(Constants.KEY_CONTENT_TYPE).equals(Constants.CONTENT_TYPE_VIDEO))
          {
            Video existingChild = _videos.get(videoEmbedCode);
            if (existingChild == null)
            {
              addVideo(new Video(videoData, videoEmbedCode, this, _api));
            }
            else
            {
              existingChild.update(videoData);
            }
          }
          else
          {
            System.out.println("ERROR: Invalid Video(DynamicChannel) content_type: " + videoData.getString(Constants.KEY_CONTENT_TYPE));
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

  public List<String> embedCodesToAuthorize()
  {
    return _embedCodes;
  }

}