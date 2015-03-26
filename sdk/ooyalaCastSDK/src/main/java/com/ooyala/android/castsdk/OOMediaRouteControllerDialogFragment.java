package com.ooyala.android.castsdk;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.MediaRouteControllerDialogFragment;

public class OOMediaRouteControllerDialogFragment extends MediaRouteControllerDialogFragment {

  private OOMediaRouteControllerDialog mCustomControllerDialog;
  private OOCastManager castManager;

  
  public OOMediaRouteControllerDialogFragment(OOCastManager castManager) {
    super();
    this.castManager = castManager;
  }
  
  @Override
  public OOMediaRouteControllerDialog onCreateControllerDialog(Context context, Bundle savedInstanceState) {
      mCustomControllerDialog = new OOMediaRouteControllerDialog(context, this.castManager);
      mCustomControllerDialog.setVolumeControlEnabled(false);
      return mCustomControllerDialog;
  }
  
  public OOMediaRouteControllerDialog getControllerDialog() {
      return mCustomControllerDialog;
  }
}
