package com.ooyala.android.visualon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;

import com.ooyala.android.util.DebugMode;

public class VisualOnUtils {
  // --- Network constants --------------------------------------------------
  private static final String TAG = VisualOnUtils.class.getName();
  public static final int CONNECTION_TIMEOUT = 60000;
  public static final int READ_DATA_TIMEOUT = 60000;
  public static final int BUFFER_SIZE = 4096;

  /**
   * Checks whether file exists on file system.
   *
   * @param fileName
   *            Full file name (Path + Filename)
   * @return true if file exists.
   */
  static public boolean checkFileExists(String fileName) {
    File f = new File(fileName);
    return ((f != null) && (f.exists()));
  }

  /**
   * Download file from a specified Url to local file system.
   *
   * @param url
   *            Location of the file on the Internet
   * @param destFileName
   *            Filename to be used on local file system.
   * @param bytesToRead
   *            The number of bytes to read (from the start).
   * @throws IOException
   */
  static public void DownloadFile(String url, String destFileName,
      int bytesToDownload) throws IOException {
    DebugMode.logI(TAG, "Downloading url: " + url + ", dest: " + destFileName);
    FileOutputStream fos = null;

    try{

      fos = new FileOutputStream(destFileName);

      byte[] buffer = new byte[BUFFER_SIZE];
      boolean shouldDownloadEntireFile = (bytesToDownload == -1);
      URL urlObj = new URL(url);
      URLConnection conn = urlObj.openConnection();
      conn.setConnectTimeout(CONNECTION_TIMEOUT);
      conn.setReadTimeout(READ_DATA_TIMEOUT);

      InputStream is = conn.getInputStream();

      int bytesRead = 0;
      int bytesReadSoFar = 0;

      while (shouldDownloadEntireFile || (bytesReadSoFar < bytesToDownload)) {
        int bytesToRead = 0;
        int bytesLeft = bytesToDownload - bytesReadSoFar;

        if (false == shouldDownloadEntireFile && bytesLeft < BUFFER_SIZE) {
          bytesToRead = bytesLeft;
        } else {
          bytesToRead = BUFFER_SIZE;
        }

        bytesRead = is.read(buffer, 0, bytesToRead);

        if (bytesRead == -1)
          break;

        bytesReadSoFar += bytesRead;
        fos.write(buffer, 0, bytesRead);
      }
      DebugMode.logI("DownloadFile", "Downloading complete: " + bytesReadSoFar + " bytes downloaded");
    } finally {
      if (null != fos){
        fos.close();
      }
    }
  }

  /**
   * Download file from a specified Url to local file system.
   *
   * @param url
   *            Location of the file on the Internet
   * @param destFileName
   *            Filename to be used on local file system.
   * @throws IOException
   */
  static public void DownloadFile(String url, String destFileName)
      throws IOException {
    DownloadFile(url, destFileName, -1);
  }

  /**
   * Copy file from Assets directory to destination. Used for licenses and processor-specific configurations
   */
  static public void copyFile(Context context, String filename, String desName)
  {
    try {
      InputStream InputStreamis  = context.getAssets().open(filename);
      File desFile = new File(context.getFilesDir().getParentFile().getPath() + "/" + desName);
      desFile.createNewFile();
      FileOutputStream  fos = new FileOutputStream(desFile);
      int bytesRead;
      byte[] buf = new byte[4 * 1024]; //4K buffer
      while((bytesRead = InputStreamis.read(buf)) != -1) {
      fos.write(buf, 0, bytesRead);
      }
      fos.flush();
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  static public void cleanupLocalFiles(Context context) {
    File dir = new File(getLocalFileDir(context));
    if (dir.listFiles() != null) {
      for (File file : dir.listFiles()) {
        file.delete();
      }
    }
  }

  static public String getLocalFileDir(Context context) {
    return context.getFilesDir().getAbsolutePath() + "/Ooyala_VO";
  }
}
