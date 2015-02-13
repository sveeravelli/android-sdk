package com.ooyala.android.visualon;

import android.content.Context;
import android.os.AsyncTask;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.DxLogConfig;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.drmdlc.api.exceptions.DrmNotSupportedException;
import com.discretix.drmdlc.api.exceptions.DrmUpdateRequiredException;
import com.ooyala.android.util.DebugMode;
import com.ooyala.android.Environment;
import com.ooyala.android.OoyalaPlayer;

class PersonalizationAsyncTask extends AsyncTask<Void, Void, Exception> {
  private static final String TAG = PersonalizationAsyncTask.class.getClass().toString();
  protected static final String PERSONALIZATION_URI = "/discretix/personalization.svc/personalize/%s";
  protected static final String SESSION_ID = "session";

  protected PersonalizationCallback _callback = null;
  protected Context _context;
  protected boolean _enableDebugDRMPlayback;
  protected String _pcode;

  /**
   * An executable task which will perform Discredix Personalization for this device
   * @param callback the object which should be used as a callback
   * @param context the context in which this should run
   */
  public PersonalizationAsyncTask(PersonalizationCallback callback, Context context, String pcode) {
    super();
    _context = context;
    _callback = callback;
    _pcode = pcode;
    _enableDebugDRMPlayback = OoyalaPlayer.enableDebugDRMPlayback;
  }

  @Override
  protected Exception doInBackground(Void... input) {
    Exception returnException = null;
    DxLogConfig config = null;
    IDxDrmDlc dlc;

    String personalizationUrl = Environment.AUTHORIZE_HOST + String.format(PERSONALIZATION_URI, _pcode);
    try {
      dlc = DxDrmDlc.getDxDrmDlc(_context, config);

      if (_enableDebugDRMPlayback) {
        dlc.getDebugInterface().setClientSideTestPersonalization(true);
      }
      //Check for verification.
      if (!dlc.personalizationVerify()) {
        dlc.performPersonalization(OoyalaPlayer.getVersion(), personalizationUrl, SESSION_ID);
      } else {
        DebugMode.logD(TAG, "Device is already personalized");
      }
    } catch (DrmGeneralFailureException e) {
      DebugMode.logE(TAG, "Caught!", e);
      returnException = e;
    } catch (DrmUpdateRequiredException e) {
      DebugMode.logE(TAG, "Caught!", e);
      returnException = e;
    } catch (DrmNotSupportedException e) {
      DebugMode.logE(TAG, "Caught!", e);
      returnException = e;
    } catch (DrmClientInitFailureException e) {
      DebugMode.logE(TAG, "Caught!", e);
      returnException = e;
    } catch (Exception e) {
      DebugMode.logE(TAG, "Unknown exception thrown in Personalization Async Task");
      DebugMode.logE(TAG, "Caught!", e);
      returnException = e;
    }
    return returnException;
  }

  @Override
  protected void onPostExecute(Exception result) {
    _callback.afterPersonalization(result);
  }

}