package com.ooyala.android.visualon;

interface FileDownloadCallback {
  /**
   * This callback is used for the FileDownloadAsyncTask asynchronous calls
   * @param localFilename
   */
  public void afterFileDownload(String localFilename);
}
