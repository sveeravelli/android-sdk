package com.ooyala.android.castsdk;

import java.lang.ref.WeakReference;

public class CastMediaRouteDialogFactory extends android.support.v7.app.MediaRouteDialogFactory {

  private WeakReference<CastManager> castManager;
  
  public CastMediaRouteDialogFactory(CastManager castManager) {
    super();
    this.castManager = new WeakReference<CastManager>(castManager);
  }
  
  @Override
  public CastMediaRouteControllerDialogFragment onCreateControllerDialogFragment() {
    CastMediaRouteControllerDialogFragment mediarouteControllerDialogFragment =  new CastMediaRouteControllerDialogFragment();
    mediarouteControllerDialogFragment.setCastManager(this.castManager.get());
    return mediarouteControllerDialogFragment;
  }

}
