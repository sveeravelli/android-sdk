package com.ooyala.android;

//this interface should only be implemented by OoyalaPlayer and be used by PluginManager to handle control switch
interface ActiveSwitchInterface {
  // the caller enter active mode and put OoyalaPlayer to halt the flow.
  void enterActive();

  // the caller exit active mode and OoyalaPlayer can resume the flow.
  void exitActive();
}
