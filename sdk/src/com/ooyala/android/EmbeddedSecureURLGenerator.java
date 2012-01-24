package com.ooyala.android;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class EmbeddedSecureURLGenerator implements SecureURLGenerator {
  private String _apiKey = null;
  private SignatureGenerator _signatureGenerator = null;

  public EmbeddedSecureURLGenerator(String apiKey, String secretKey) {
    _apiKey = apiKey;
    _signatureGenerator = new EmbeddedSignatureGenerator(secretKey);
  }

  public URL secureURL(String host, String uri, Map<String,String> params)
  {
    Map<String,String> allParams = null;
    if (params == null) {
      allParams = new HashMap<String,String>();
      allParams.put(Constants.KEY_API_KEY, _apiKey);
      long secondsSince1970 = (new Date()).getTime() / 1000;
      allParams.put(Constants.KEY_EXPIRES, Long.toString(secondsSince1970 + Constants.RESPONSE_LIFE_SECONDS));
      allParams.put(Constants.KEY_SIGNATURE, _signatureGenerator.sign(genStringToSign(uri, allParams, Constants.METHOD_GET)));
    } else {
      allParams = new HashMap<String,String>(params);
      if (!params.containsKey(Constants.KEY_SIGNATURE)) {
        if (!params.containsKey(Constants.KEY_API_KEY)) {
          allParams.put(Constants.KEY_API_KEY, _apiKey);
        }
        if (!params.containsKey(Constants.KEY_EXPIRES)) {
          long secondsSince1970 = (new Date()).getTime() / 1000;
          allParams.put(Constants.KEY_EXPIRES, Long.toString(secondsSince1970 + Constants.RESPONSE_LIFE_SECONDS));
        }
        allParams.put(Constants.KEY_SIGNATURE, _signatureGenerator.sign(genStringToSign(uri, allParams, Constants.METHOD_GET)));
      }
    }
    return Utils.makeURL(host, uri, allParams);
  }

  private String genStringToSign(String uri, Map<String,String> params, String method) {
    String paramsString = Utils.getParamsString(params, Constants.SEPARATOR_EMPTY, false);
    return method + uri + paramsString;
  }

}
