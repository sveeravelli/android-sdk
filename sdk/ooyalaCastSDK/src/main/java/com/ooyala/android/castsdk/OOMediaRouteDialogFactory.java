package com.ooyala.android.castsdk;

import android.support.v7.app.MediaRouteDialogFactory;

import java.lang.ref.WeakReference;

public class OOMediaRouteDialogFactory extends MediaRouteDialogFactory {

  private WeakReference<OOCastManager> castManager;
  
  public OOMediaRouteDialogFactory(OOCastManager castManager) {
    super();
    this.castManager = new WeakReference<OOCastManager>(castManager);
  }
  
  @Override
  public OOMediaRouteControllerDialogFragment onCreateControllerDialogFragment() {
    OOMediaRouteControllerDialogFragment mediarouteControllerDialogFragment =  new OOMediaRouteControllerDialogFragment();
    mediarouteControllerDialogFragment.setCastManager(this.castManager.get());
    return mediarouteControllerDialogFragment;
  }

}
