package com.ooyala.android.visualon;

interface FileDownloadCallback {
  /**
   * This callback is used for the FileDownloadAsyncTask asynchronous calls
   * @param result true if the FileDownloadAsyncTask call succeeded, false otherwise
   */
  public void afterFileDownload(String localFilename);
}
