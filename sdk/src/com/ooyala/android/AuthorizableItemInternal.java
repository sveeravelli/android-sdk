package com.ooyala.android;

import java.util.List;

import org.json.JSONObject;

import com.ooyala.android.Constants.ReturnState;

interface AuthorizableItemInternal extends AuthorizableItem {
  /**
   * For internal use only. Update the AuthorizableItemInternal using the specified data (subclasses should
   * override and call this)
   * @param data the data to use to update this AuthorizableItemInternal
   * @return a ReturnState based on if the data matched or not (or parsing failed)
   */
  public ReturnState update(JSONObject data);

  /**
   * For internal use only. The embed codes to authorize for the AuthorizableItemInternal
   * @return the embed codes to authorize as a List
   */
  public List<String> embedCodesToAuthorize();

  public boolean isReauthRequired();
}
