package com.ooyala.android;

//this interface should only be implemented by OoyalaPlayer and be used by PluginManager to handle control switch
interface ActiveSwitchInterface {
  // the caller exit active mode and OoyalaPlayer can resume the flow.
  void exitActive();
}
