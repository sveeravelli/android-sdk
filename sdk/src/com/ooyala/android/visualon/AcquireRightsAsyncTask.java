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
public class AcquireRightsAsyncTask extends AsyncTask<Void, Void, Exception> {
  protected String TAG = this.getClass().toString();
  protected AcquireRightsCallback _callback = null;
  protected Context _context;
  protected String _localFilename;
  protected String _authToken;

/**
 * An executable task which will call Discredix rights acquisition on a locally downloaded file
 * @param callback the object which should be used as a callback
 * @param context the context in which this should run
 * @param localFilename locally downloaded media file
 */
  public AcquireRightsAsyncTask(AcquireRightsCallback callback, Context context, String localFilename, String authToken) {
    super();
    _context = context;
    _callback = callback;
    _localFilename = localFilename;
    _authToken = authToken;
  }

  @Override
  protected Exception doInBackground(Void... input) {
    Exception resultException = null;
    DxLogConfig config = null;
    IDxDrmDlc dlc;
    try {
      dlc = DxDrmDlc.getDxDrmDlc(_context, config);
      String customData = "";

      if (!"".equals(_authToken)) {
        customData = "auth_token=" + _authToken;
      }
      String customUrl = null;
      if(!dlc.verifyRights(_localFilename)){
        dlc.acquireRights(_localFilename, customData, customUrl);
        dlc.setCookies(null);
      }
    } catch (DrmClientInitFailureException e) {
      e.printStackTrace();
      resultException = e;
    } catch (IOException e) {
      e.printStackTrace();
      resultException = e;
    } catch (DrmGeneralFailureException e) {
      e.printStackTrace();
      resultException = e;
    } catch (DrmNotProtectedException e) {
      e.printStackTrace();
      resultException = e;
    } catch (DrmInvalidFormatException e) {
      e.printStackTrace();
      resultException = e;
    } catch (DrmServerSoapErrorException e) {
      resultException = e;
    }

    return resultException;
  }

  @Override
  protected void onPostExecute(Exception result) {
    _callback.afterAcquireRights(result);
  }

}