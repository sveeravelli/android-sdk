package com.ooyala.android;

/**
 * Stores the info and metatdata for the specified movie.
 * 
 */
public interface AuthorizableItem {
  /**
   * Authorize response codes
   */
  public interface AuthCode {
    /** The authorization code was invalid */
    public static int UNKNOWN = -2;
    /** The authorization has not been requested for this item */
    public static int NOT_REQUESTED = -1;
    /** The minimum value for auth codes from the server */
    public static int MIN_AUTH_CODE = 0;
    /** The item is authorized */
    public static int AUTHORIZED = 0;
    /** The item's parent is unauthorized */
    public static int UNAUTHORIZED_PARENT = 1;
    /** The item is not authorized for this domain */
    public static int UNAUTHORIZED_DOMAIN = 2;
    /** The item is not authorized for this location */
    public static int UNAUTHORIZED_LOCATION = 3;
    /** The item has been requested before its flight time */
    public static int BEFORE_FLIGHT_TIME = 4;
    /** The item has been requested after its flight time */
    public static int AFTER_FLIGHT_TIME = 5;
    /** The item has been requested outside of its recurring flight time */
    public static int OUTSIDE_RECURRING_FLIGHT_TIMES = 6;
    /** The item's embed code is invalid */
    public static int BAD_EMBED_CODE = 7;
    /** The signature of the request is invalid */
    public static int INVALID_SIGNATURE = 8;
    /** The request had missing params */
    public static int MISSING_PARAMS = 9;
    /** The server is missing its rule set */
    public static int MISSING_RULE_SET = 10;
    /** The item is unauthorized */
    public static int UNAUTHORIZED = 11;
    /** The request was missing the pcode */
    public static int MISSING_PCODE = 12;
    /** The item is not authorized for this device */
    public static int UNAUTHORIZED_DEVICE = 13;
    /** The request's token was invalid */
    public static int INVALID_TOKEN = 14;
    /** The request's token was expired */
    public static int TOKEN_EXPIRED = 15;
    /** The maximum value for auth codes from the server */
    public static int MAX_AUTH_CODE = 16;
  }

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
