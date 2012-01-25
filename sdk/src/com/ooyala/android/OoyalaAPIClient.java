package com.ooyala.android;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class OoyalaAPIClient {
  private PlayerAPIClient _playerAPI = null;
  private OoyalaAPIHelper _apiHelper = null;

  /**
   * Instantiate an OoyalaAPIClient
   * @param apiKey the API Key to use for secured APIs
   * @param secret the Secret to use for secured APIs
   * @param pcode the Provider Code
   * @param domain the Embed Domain to use
   */
  public OoyalaAPIClient(String apiKey, String secret, String pcode, String domain) {
    _apiHelper = new OoyalaAPIHelper(apiKey, secret);
    _playerAPI = new PlayerAPIClient(_apiHelper, pcode, domain);
  }

  public OoyalaAPIClient(String apiKey, SignatureGenerator signatureGenerator, String pcode, String domain) {
    _apiHelper = new OoyalaAPIHelper(apiKey, signatureGenerator);
    _playerAPI = new PlayerAPIClient(_apiHelper, pcode, domain);
  }

  public OoyalaAPIClient(SecureURLGenerator secureURLGenerator, String pcode, String domain) {
    _apiHelper = new OoyalaAPIHelper(secureURLGenerator);
    _playerAPI = new PlayerAPIClient(_apiHelper, pcode, domain);
  }

  public OoyalaAPIClient(PlayerAPIClient apiClient) {
    _apiHelper = apiClient.getAPIHelper();
    _playerAPI = apiClient;
  }

  /**
   * Fetch the root ContentItem associated with the given embed codes. If multiple embed codes are given, the root item is
   * assumed to be a Dynamic Channel and the embed codes are assumed to all be videos.
   * @param embedCodes the embed codes to fetch
   * @return the root ContentItem representing embedCodes
   * @throws OoyalaException
   */
  public ContentItem contentTree(List<String> embedCodes) throws OoyalaException {
    return _playerAPI.contentTree(embedCodes);
  }

  /**
   * Fetch the root ContentItem associated with the given external ids. If multiple external ids are given, the root item is
   * assumed to be a Dynamic Channel and the external ids are assumed to all be videos.
   * @param externalIds the external ids to fetch
   * @return the root ContentItem representing externalIds
   * @throws OoyalaException
   */
  public ContentItem contentTreeByExternalIds(List<String> externalIds) throws OoyalaException {
    return _playerAPI.contentTreeByExternalIds(externalIds);
  }

  /**
   * Fetch a raw JSONObject from any backlot API (GET requests only)
   * @param uri the URI to be fetched from backlot *not* including "/v2". For example, to request https://api.ooyala.com/v2/assets, uri should be "/assets"
   * @param params Optional parameters to pass to the API
   * @return the raw JSONObject representing the response
   */
  public JSONObject objectFromBacklotAPI(String uri, Map<String,String> params) {
    return _apiHelper.objectForSecureAPI(Constants.BACKLOT_HOST, Constants.BACKLOT_URI_PREFIX+uri, params);
  }

  public SecureURLGenerator getSecureURLGenerator() {
    return _apiHelper.getSecureURLGenerator();
  }

  public String getPcode() {
    return _playerAPI.getPcode();
  }

  public String getDomain() {
    return _playerAPI.getDomain();
  }
}
