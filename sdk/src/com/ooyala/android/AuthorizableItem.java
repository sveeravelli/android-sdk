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
   * Authorize response codes
   */
  public interface AuthCode {
    public static int UNKNOWN = -2;                       /**< The authorization code was invalid */
    public static int NOT_REQUESTED = -1;                 /**< The authorization has not been requested for this item */
    public static int MIN_AUTH_CODE = 0;                  /**< The minimum value for auth codes from the server */
    public static int AUTHORIZED = 0;                     /**< The item is authorized */
    public static int UNAUTHORIZED_PARENT = 1;            /**< The item's parent is unauthorized */
    public static int UNAUTHORIZED_DOMAIN = 2;            /**< The item is not authorized for this domain */
    public static int UNAUTHORIZED_LOCATION = 3;          /**< The item is not authorized for this location */
    public static int BEFORE_FLIGHT_TIME = 4;             /**< The item has been requested before its flight time */
    public static int AFTER_FLIGHT_TIME = 5;              /**< The item has been requested after its flight time */
    public static int OUTSIDE_RECURRING_FLIGHT_TIMES = 6; /**< The item has been requested outside of its recurring flight time */
    public static int BAD_EMBED_CODE = 7;                 /**< The item's embed code is invalid */
    public static int INVALID_SIGNATURE = 8;              /**< The signature of the request is invalid */
    public static int MISSING_PARAMS = 9;                 /**< The request had missing params */
    public static int MISSING_RULE_SET = 10;              /**< The server is missing its rule set */
    public static int UNAUTHORIZED = 11;                  /**< The item is unauthorized */
    public static int MISSING_PCODE = 12;                 /**< The request was missing the pcode */
    public static int UNAUTHORIZED_DEVICE = 13;           /**< The item is not authorized for this device */
    public static int INVALID_TOKEN = 14;                 /**< The request's token was invalid */
    public static int TOKEN_EXPIRED = 15;                 /**< The request's token was expired */
    public static int MAX_AUTH_CODE = 16;                 /**< The maximum value for auth codes from the server */
  }

  /**
   * Update the ContentItem using the specified data (subclasses should override and call this)
   * @param data the data to use to update this ContentItem
   * @return a ReturnState based on if the data matched or not (or parsing failed)
   */
  public ReturnState update(JSONObject data);

  /**
   * The embed code for the AuthorizableItem
   * @return the embed codes to authorize as a List
   */
  public List<String> embedCodesToAuthorize();

  /**
   * Whether or not this AuthorizableItem is authorized
   * @return true if authorized, false if not
   */
  public boolean isAuthorized();

  /**
   * The Auth Code from the authorization request
   * @return an int with the status of the authorization request
   */
  public int getAuthCode();
}
