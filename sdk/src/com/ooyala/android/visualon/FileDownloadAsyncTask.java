package com.ooyala.android.visualon;

import java.io.File;
import java.io.IOException;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class FileDownloadAsyncTask extends AsyncTask<Void, Void, String> {
  protected String TAG = this.getClass().toString();
  protected FileDownloadCallback _callback = null;
  protected String _contentDir;
  protected String _localFilePath;
  protected String _streamUrl;

  /**
   * An executable task which will download a url to a local filename
   * @param callback the object which should be used as a callback
   * @param filename the destination filename for the file
   * @param streamUrl the source url for the file
   */
  public FileDownloadAsyncTask(FileDownloadCallback callback, String filename, String streamUrl) {
    super();
    _contentDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Ooyala_SecurePlayer";
    _localFilePath = String.format("%s/%s", _contentDir, filename);
    _callback = callback;
    _streamUrl = streamUrl;
  }

  @Override
  protected String doInBackground(Void...input) {
    if(_streamUrl == null || _localFilePath == null) return null;
    try {
      //Create content directory.
      if (new File(_contentDir).mkdirs() == false){
        if (new File(_contentDir).exists() == false){
          Log.e(TAG, "Cannot create content directory on SD-CARD");
        }
      }
      VisualOnUtils.DownloadFile(_streamUrl, _localFilePath);
      return _localFilePath;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  protected void onPostExecute(String result) {
    _callback.afterFileDownload(result);
  }

}