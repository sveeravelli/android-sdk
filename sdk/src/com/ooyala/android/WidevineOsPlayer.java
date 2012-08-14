package com.ooyala.android;

import com.ooyala.android.OoyalaPlayer.State;

import android.annotation.TargetApi;
import android.drm.DrmErrorEvent;
import android.drm.DrmEvent;
import android.drm.DrmInfoRequest;
import android.drm.DrmManagerClient;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.provider.Settings.Secure;

//the widevine player using the built in libraries, for honeycomb+
@TargetApi(11)
class WidevineOsPlayer extends MoviePlayer implements DrmManagerClient.OnErrorListener, DrmManagerClient.OnEventListener {

  private static DrmManagerClient _drmClient;

  @Override
  public void init(OoyalaPlayer parent, Object o) {
    WidevineParams params = (WidevineParams)o;

    if (_drmClient == null) {
      _drmClient = new DrmManagerClient(OoyalaAPIHelper.context);
      _drmClient.setOnErrorListener(this);
      _drmClient.setOnEventListener(this);
    }

    DrmInfoRequest request = new DrmInfoRequest(DrmInfoRequest.TYPE_RIGHTS_ACQUISITION_INFO, "video/wvm");
    //this should point to SAS once we get the proxy up
    request.put("WVDRMServerKey", "http://chrisl.mtv:4567/drm/widevine/v1");
    request.put("WVAssetURIKey", params.url);
    request.put("WVPortalKey", "ooyala");
    request.put("WVDeviceIDKey", Secure.getString(OoyalaAPIHelper.context.getContentResolver(), Secure.ANDROID_ID));
    request.put("WVLicenseTypeKey", "3");

    _drmClient.acquireRights(request);
    super.init(parent, params.url);
  }

  @Override
  public void onError(DrmManagerClient client, DrmErrorEvent event) {
    switch (event.getType()) {
      case DrmErrorEvent.TYPE_ACQUIRE_DRM_INFO_FAILED:
        _error = "Could not acquire DRM";
        break;
      case DrmErrorEvent.TYPE_NO_INTERNET_CONNECTION:
        _error = "Could not connect to Internet";
        break;
      case DrmErrorEvent.TYPE_NOT_SUPPORTED:
        _error = "Content type not supported";
        break;
      case DrmErrorEvent.TYPE_OUT_OF_MEMORY:
        _error = "Out of memory";
        break;
      case DrmErrorEvent.TYPE_PROCESS_DRM_INFO_FAILED:
        _error = "Could not connect to Internet";
        break;
      case DrmErrorEvent.TYPE_REMOVE_ALL_RIGHTS_FAILED:
        _error = "Could not remove rights";
        break;
      case DrmErrorEvent.TYPE_RIGHTS_NOT_INSTALLED:
        _error = "Could not install rights";
        break;
      case DrmErrorEvent.TYPE_RIGHTS_RENEWAL_NOT_ALLOWED:
        _error = "Could not renew rights";
        break;
    }
    Log.d("Widevine", event.getMessage());
    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        setState(State.ERROR);
      }
    });
  }

  @Override
  public void onEvent(DrmManagerClient client, DrmEvent event) {
    switch (event.getType()) {
      case DrmEvent.TYPE_ALL_RIGHTS_REMOVED:
        break;
      case DrmEvent.TYPE_DRM_INFO_PROCESSED:
        break;
    }
  }
}