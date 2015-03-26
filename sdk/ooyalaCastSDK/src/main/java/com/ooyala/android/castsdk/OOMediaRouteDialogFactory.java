package com.ooyala.android.castsdk;

import android.support.v7.app.MediaRouteDialogFactory;

public class OOMediaRouteDialogFactory extends MediaRouteDialogFactory {
  
  private OOCastManager castManager;

  
  public OOMediaRouteDialogFactory(OOCastManager castManager) {
    super();
    this.castManager = castManager;
  }
  
  @Override
  public OOMediaRouteControllerDialogFragment onCreateControllerDialogFragment() {
      return new OOMediaRouteControllerDialogFragment(this.castManager);
  }

}
