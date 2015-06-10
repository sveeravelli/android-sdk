package com.ooyala.android.castsdk;

import android.content.Context;
import android.os.Bundle;

import com.ooyala.android.util.DebugMode;

public class CastMediaRouteControllerDialogFragment extends android.support.v7.app.MediaRouteControllerDialogFragment {
  private static final String TAG = "CastMediaRouteControllerDialogFragment";
  private CastMediaRouteControllerDialog mCustomControllerDialog;
  public CastMediaRouteControllerDialogFragment() {
    super();
  }
  
  @Override
  public CastMediaRouteControllerDialog onCreateControllerDialog(Context context, Bundle savedInstanceState) {
    mCustomControllerDialog = new CastMediaRouteControllerDialog(context);
    mCustomControllerDialog.setVolumeControlEnabled(false);
    DebugMode.logD(TAG, "onCreateControllerDialog = " + mCustomControllerDialog);
    return mCustomControllerDialog;
  }
  
  public CastMediaRouteControllerDialog getControllerDialog() {
      return mCustomControllerDialog;
  }
}
