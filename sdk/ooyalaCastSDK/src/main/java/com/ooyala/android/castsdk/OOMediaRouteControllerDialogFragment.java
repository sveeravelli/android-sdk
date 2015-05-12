package com.ooyala.android.castsdk;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.MediaRouteControllerDialogFragment;

import java.lang.ref.WeakReference;

public class OOMediaRouteControllerDialogFragment extends MediaRouteControllerDialogFragment {

  private OOMediaRouteControllerDialog mCustomControllerDialog;
  private WeakReference<OOCastManager> castManager;

  public OOMediaRouteControllerDialogFragment() {
    super();
  }
  
  @Override
  public OOMediaRouteControllerDialog onCreateControllerDialog(Context context, Bundle savedInstanceState) {
      mCustomControllerDialog = new OOMediaRouteControllerDialog(context, this.castManager.get());
      mCustomControllerDialog.setVolumeControlEnabled(false);
      return mCustomControllerDialog;
  }
  
  public OOMediaRouteControllerDialog getControllerDialog() {
      return mCustomControllerDialog;
  }

  public void setCastManager(OOCastManager castManager) {
    this.castManager = new WeakReference<OOCastManager>(castManager);
  }
}
