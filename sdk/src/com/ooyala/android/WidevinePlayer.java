package com.ooyala.android;
import java.util.HashMap;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ooyala.android.OoyalaPlayer.State;
import com.widevine.drmapi.android.WVEvent;
import com.widevine.drmapi.android.WVEventListener;
import com.widevine.drmapi.android.WVPlayback;
import com.widevine.drmapi.android.WVStatus;

public class WidevinePlayer extends MoviePlayer implements WVEventListener {
  private WVPlayback wvplayback = new WVPlayback();
  private OoyalaPlayer parent;
  private String wvUrl = "";
  @Override
  public void init(OoyalaPlayer parent, Object stream) {
    HashMap<String, Object> options = new HashMap<String, Object>();
    //this should point to SAS once we get the proxy up
    options.put("WVDRMServer", "http://wstfcps005.shibboleth.tv/widevine/cypherpc/cgi-bin/GetEMMs.cgi");
    options.put("WVPortalKey", "ooyala");
    
    this.wvUrl = (String)stream;
    this.parent = parent;

    wvplayback.initialize(OoyalaAPIHelper.context, options, this);
  }
  
  //helper method to call super.init inside runnable
  private void init2(String stream) {
    super.init(parent, stream);
    
  }

  @Override
  public WVStatus onEvent(WVEvent event, HashMap<String, Object> arg1) {
    if (event == WVEvent.Initialized) {
      new Handler(Looper.getMainLooper()).post(new Runnable() {
        @Override
        public void run() {
          init2(wvplayback.play((String)wvUrl));
        }
      });
    } else if (event == WVEvent.InitializeFailed) {
      this._error = "Widevine Initialization Failed";
      setState(State.ERROR);
    } else if (event == WVEvent.LicenseRequestFailed) {
      this._error = "Widevine License Request Failed";
      setState(State.ERROR);
    } else if (event == WVEvent.PlayFailed) {
      this._error = "Widevine Playback Failed";
      setState(State.ERROR);
    }

    Log.d(this.getClass().getName(), event.toString());
    return WVStatus.OK;
  }
}
