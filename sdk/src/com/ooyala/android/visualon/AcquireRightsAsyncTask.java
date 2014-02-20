package com.ooyala.android.visualon;

import java.io.IOException;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.DxLogConfig;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.drmdlc.api.exceptions.DrmInvalidFormatException;
import com.discretix.drmdlc.api.exceptions.DrmNotProtectedException;
import com.discretix.drmdlc.api.exceptions.DrmServerSoapErrorException;

import android.content.Context;
import android.os.AsyncTask;
/**
 * Perform DRM rights acquisition on a provided local file
 * @author michael.len
 *
 */
public class AcquireRightsAsyncTask extends AsyncTask<Void, Void, Boolean> {
  protected String TAG = this.getClass().toString();
  protected AcquireRightsCallback _callback = null;
  protected Context _context;
  protected String _localFilename;

/**
 * An executable task which will call Discredix rights acquisition on a locally downloaded file
 * @param callback the object which should be used as a callback
 * @param context the context in which this should run
 * @param localFilename locally downloaded media file
 */
  public AcquireRightsAsyncTask(AcquireRightsCallback callback, Context context, String localFilename) {
    super();
    _context = context;
    _callback = callback;
    _localFilename = localFilename;
  }

  @Override
  protected Boolean doInBackground(Void... input) {
    boolean success = true;
    DxLogConfig config = null;
    IDxDrmDlc dlc;
    try {
      dlc = DxDrmDlc.getDxDrmDlc(_context, config);
      String customData = "Unlimited";
      String customUrl = null;
      if(!dlc.verifyRights(_localFilename)){
        dlc.acquireRights(_localFilename, customData, customUrl);
        dlc.setCookies(null);
      }
    } catch (DrmClientInitFailureException e) {
      e.printStackTrace();
      success = false;
    } catch (IOException e) {
      e.printStackTrace();
      success = false;
    } catch (DrmGeneralFailureException e) {
      e.printStackTrace();
      success = false;
    } catch (DrmNotProtectedException e) {
      e.printStackTrace();
      success = false;
    } catch (DrmInvalidFormatException e) {
      e.printStackTrace();
      success = false;
    } catch (DrmServerSoapErrorException e) {
      e.printStackTrace();
      success = false;
    }

    return success;
  }

  @Override
  protected void onPostExecute(Boolean result) {
    _callback.afterAcquireRights(result);
  }

}