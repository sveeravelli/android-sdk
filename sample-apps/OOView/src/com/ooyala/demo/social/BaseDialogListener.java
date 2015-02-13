package com.ooyala.demo.social;


/**
 * Skeleton base class for RequestListeners, providing default error
 * handling. Applications should handle these error conditions.
 */
public abstract class BaseDialogListener implements DialogListener {

    public void onFacebookError(FacebookError e) {
        DebugMode.logE(TAG, "Caught!", e);
    }

    public void onError(DialogError e) {
        DebugMode.logE(TAG, "Caught!", e);
    }

    public void onCancel() {
    }

}
