package com.ooyala.android;

import org.json.JSONObject;

import com.ooyala.android.Constants.ReturnState;

public interface PaginatedParentItem {
  public String getEmbedCode();

  /**
   * For internal use only. Update the PaginatedParentItem using the specified data (subclasses should
   * override and call this)
   * @param data the data to use to update this PaginatedParentItem
   * @return a ReturnState based on if the data matched or not (or parsing failed)
   */
  public ReturnState update(JSONObject data);

  /**
   * Find out it this PaginatedParentItem has more children
   * 
   * @return true if it does, false if it doesn't
   */
  public boolean hasMoreChildren();

  /**
   * Fetch the additional children if they exist. This will happen in the background and callback will be
   * called when the fetch is complete.
   * 
   * @param paginatedItemListener the PaginatedItemListener to execute when the children are fetched
   * @return true if more children exist, false if they don't or they are already in the process of being
   *         fetched
   */
  public boolean fetchMoreChildren(PaginatedItemListener paginatedItemListener);

  /**
   * The number of children this PaginatedParentItem has.
   * 
   * @return an int with the number of children
   */
  public int childrenCount();

  /**
   * For Internal Use Only.
   * @return the next children token for this PaginatedParentItem
   */
  public String getNextChildren();
}
