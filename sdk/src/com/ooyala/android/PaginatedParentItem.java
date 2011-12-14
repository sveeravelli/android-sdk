package com.ooyala.android;

import org.json.JSONObject;

import com.ooyala.android.Constants.ReturnState;

public interface PaginatedParentItem
{
  public String getEmbedCode();

  public ReturnState update(JSONObject data);

  public boolean hasMoreChildren();

  public boolean fetchMoreChildren(PaginatedItemListener paginatedItemListener);

  public int childrenCount();
}
