package com.ooyala.android.visualon;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import android.os.AsyncTask;

public class PlayreadyLicenseAcquisitionAsyncTask extends
    AsyncTask<String, Void, Boolean> {

  private class FileDownloadCallable implements Callable<String> {

    @Override
    public String call() throws Exception {
      return null;
    }

  }

  @Override
  protected Boolean doInBackground(String... params) {
    FutureTask<String> downloadTask = new FutureTask<String>(new FileDownloadCallable());
    Thread downloadThread = new Thread(downloadTask);
    downloadThread.run();
    try {
      downloadThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  protected void onPostExecute(Boolean result) {
  }
}
