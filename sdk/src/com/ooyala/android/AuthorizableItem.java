package com.ooyala.android;

import java.util.List;

import org.json.JSONObject;

import com.ooyala.android.Constants.ReturnState;

/**
 * Stores the info and metatdata for the specified movie.
 *
 */
public interface AuthorizableItem
{
  /**
   * Update the ContentItem using the specified data (subclasses should override and call this)
   * @param data the data to use to update this ContentItem
   * @returns a ReturnState based on if the data matched or not (or parsing failed)
   */
  public ReturnState update(JSONObject data);

  /**
   * The embed code for the AuthorizableItem
   * @returns the embed codes to authorize as a List
   */
  public List<String> embedCodesToAuthorize();
}
