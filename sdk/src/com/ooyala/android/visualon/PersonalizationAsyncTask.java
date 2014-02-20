package com.ooyala.android.visualon;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.DxLogConfig;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.drmdlc.api.exceptions.DrmNotSupportedException;
import com.discretix.drmdlc.api.exceptions.DrmUpdateRequiredException;
import com.ooyala.android.OoyalaPlayer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class PersonalizationAsyncTask extends AsyncTask<Void, Void, Boolean> {
  protected String TAG = this.getClass().toString();
  protected PersonalizationCallback _callback = null;
  protected Context _context;

  /**
   * An executable task which will perform Discredix Personalization for this device
   * @param callback the object which should be used as a callback
   * @param context the context in which this should run
   */
  public PersonalizationAsyncTask(PersonalizationCallback callback, Context context) {
    super();
    _context = context;
    _callback = callback;
  }

  @Override
  protected Boolean doInBackground(Void... input) {
    boolean success = true;
    DxLogConfig config = null;
    IDxDrmDlc dlc;

    String PERSONALIZATION_URL = "172.16.8.137:8000/Personalization";
    String SESSION_ID = "session";
    try {
      dlc = DxDrmDlc.getDxDrmDlc(_context, config);

      dlc.getDebugInterface().setClientSideTestPersonalization(true);
      //Check for verification.
      if (!dlc.personalizationVerify()) {
        dlc.performPersonalization(OoyalaPlayer.getVersion(), PERSONALIZATION_URL, SESSION_ID);
      } else {
        Log.d(TAG, "Device is already personalized");
      }
    } catch (DrmGeneralFailureException e) {
      e.printStackTrace();
      success = false;
    } catch (DrmUpdateRequiredException e) {
      e.printStackTrace();
      success = false;
    } catch (DrmNotSupportedException e) {
      e.printStackTrace();
      success = false;
    } catch (DrmClientInitFailureException e) {
      e.printStackTrace();
      success = false;
    }
    return success;
  }

  @Override
  protected void onPostExecute(Boolean result) {
    _callback.afterPersonalization(result);
  }

}