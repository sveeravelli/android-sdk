package com.ooyala.android.castsdk;

import com.ooyala.android.util.DebugMode;

public class CastMediaRouteDialogFactory extends android.support.v7.app.MediaRouteDialogFactory {
  private static final String TAG = "CastMediaRouteDialogFactory";
  private CastManager castManager;
  
  public CastMediaRouteDialogFactory() {
    super();
  }
  
  @Override
  public CastMediaRouteControllerDialogFragment onCreateControllerDialogFragment() {
    DebugMode.logD(TAG, "onCreateControllerDialogFragment");
    CastMediaRouteControllerDialogFragment mediarouteControllerDialogFragment =  new CastMediaRouteControllerDialogFragment();
    return mediarouteControllerDialogFragment;
  }

}
