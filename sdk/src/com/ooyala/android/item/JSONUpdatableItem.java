package com.ooyala.android.item;

/**
 * Provides a consistent return state for all objects that are updated from Player APIs
 * @author michael.len
 *
 */
interface JSONUpdatableItem {

  enum ReturnState {
    STATE_MATCHED, STATE_UNMATCHED, STATE_FAIL
  };

}
