package com.ooyala.android.castsdk;

import android.content.Context;
import android.os.Bundle;

import java.lang.ref.WeakReference;

public class CastMediaRouteControllerDialogFragment extends android.support.v7.app.MediaRouteControllerDialogFragment {

  private CastMediaRouteControllerDialog mCustomControllerDialog;
  private WeakReference<CastManager> castManager;

  public CastMediaRouteControllerDialogFragment() {
    super();
  }
  
  @Override
  public CastMediaRouteControllerDialog onCreateControllerDialog(Context context, Bundle savedInstanceState) {
      mCustomControllerDialog = new CastMediaRouteControllerDialog(context, this.castManager.get());
      mCustomControllerDialog.setVolumeControlEnabled(false);
      return mCustomControllerDialog;
  }
  
  public CastMediaRouteControllerDialog getControllerDialog() {
      return mCustomControllerDialog;
  }

  public void setCastManager(CastManager castManager) {
    this.castManager = new WeakReference<CastManager>(castManager);
  }
}
