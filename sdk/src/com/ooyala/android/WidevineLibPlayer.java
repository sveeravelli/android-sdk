package com.ooyala.android;
import java.util.HashMap;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ooyala.android.OoyalaPlayer.State;
import com.widevine.drmapi.android.WVEvent;
import com.widevine.drmapi.android.WVEventListener;
import com.widevine.drmapi.android.WVPlayback;
import com.widevine.drmapi.android.WVStatus;

//Use the 2.x API for pre-honeycomb devices.
public class WidevineLibPlayer extends MoviePlayer implements WVEventListener, Handler.Callback {
  //messages
  private static final int INIT = 0;
  private static final int ERROR = -1;

  private static WVPlayback _wvplayback = new WVPlayback();
  private Handler _handler = new Handler(this);
  private OoyalaPlayer parent;
  private String stream = "";

  @Override
  public void init(OoyalaPlayer parent, Object o) {
    WidevineParams params = (WidevineParams)o;

    this.stream = params.url;
    this.parent = parent;

    HashMap<String, Object> options = new HashMap<String, Object>();
    //this should point to SAS once we get the proxy up

    String path = "http://172.16.100.176:4567/sas/drm2/" + params.pcode + "/" + params.embedCode + "/widevine/ooyala/";
    options.put("WVPortalKey", ""); //add this value in SAS
    options.put("WVDRMServer", path);
    options.put("WVLicenseTypeKey", 3);

    _wvplayback.initialize(OoyalaAPIHelper.context, options, this);
  }

  @Override
  public WVStatus onEvent(WVEvent event, HashMap<String, Object> attributes) {
    Log.d("Widevine", event.toString() + ": " + attributes.toString());
    switch (event) {
      case InitializeFailed:
        this._error = "Widevine Initialization Failed";
      case LicenseRequestFailed:
        this._error = "Widevine License Request Failed";
      case PlayFailed:
        this._error = "Widevine Playback Failed";
        _handler.sendEmptyMessage(ERROR);
        if (attributes.containsKey("WVStatusKey"))
          return (WVStatus)attributes.get("WVStatusKey");
        else
          return WVStatus.OK;
      case Initialized:
        _handler.sendEmptyMessage(INIT);
      case NullEvent:
      case Playing:
      case Stopped:
      case EndOfList:
      case Terminated:
      case SecureStore:
      case LicenseReceived:
      case LicenseRemoved:
      case Registered:
      case Unregistered:
      case QueryStatus:
        return WVStatus.OK;
      default:
        return WVStatus.OK;
    }
  }

  @Override
  public boolean handleMessage(Message msg) {
    switch (msg.what) {
      case INIT:
        super.init(parent, _wvplayback.play(stream));
        break;
      case ERROR:
        setState(State.ERROR);
        break;
      default:
    }
    return true;
  }

  @Override
  public void destroy() {
    _wvplayback.terminate();
    super.destroy();
  }
}
