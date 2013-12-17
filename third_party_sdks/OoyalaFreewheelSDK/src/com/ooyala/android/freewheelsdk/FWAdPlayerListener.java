package com.ooyala.android.freewheelsdk;

import tv.freewheel.ad.interfaces.ISlot;

public interface FWAdPlayerListener {

  public void adReady(ISlot ad);

  public void onError();

}
