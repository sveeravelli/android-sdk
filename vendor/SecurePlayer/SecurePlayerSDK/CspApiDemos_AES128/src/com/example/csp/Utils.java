/*******************************************************************************
 * Copyright
 *  This code is strictly confidential and the receiver is obliged to use it
 *  exclusively for his or her own purposes. No part of Viaccess Orca code may be
 *  reproduced or transmitted in any form or by any means, electronic or
 *  mechanical, including photocopying, recording, or by any information storage
 *  and retrieval system, without permission in writing from Viaccess Orca.
 *  The information in this code is subject to change without notice. Viaccess Orca
 *  does not warrant that this code is error free. If you find any problems
 *  with this code or wish to make comments, please report them to Viaccess Orca.
 *  
 *  Trademarks
 *  Viaccess Orca is a registered trademark of Viaccess S.A in France and/or other
 *  countries. All other product and company names mentioned herein are the
 *  trademarks of their respective owners.
 *  Viaccess S.A may hold patents, patent applications, trademarks, copyrights
 *  or other intellectual property rights over the code hereafter. Unless
 *  expressly specified otherwise in a Viaccess Orca written license agreement, the
 *  delivery of this code does not imply the concession of any license over
 *  these patents, trademarks, copyrights or other intellectual property.
 *******************************************************************************/

package com.example.csp;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Implements different static helper methods used by application modules.
 */
public class Utils {

    // Network constants
    public static final int CONNECTION_TIMEOUT = 60000;
    public static final int READ_DATA_TIMEOUT = 60000;
    public static final int BUFFER_SIZE = 4096;

    /**
     * Checks whether file exists on file system.
     * 
     * @param fileName Full file name (Path + Filename)
     * @return true if file exists.
     */
    static public boolean checkFileExists(String fileName) {
        File f = new File(fileName);
        return ((f != null) && (f.exists()));
    }

    /**
     * Downloads file from a specified Url to local file system.
     * 
     * @param url Location of the file on the Internet.
     * @param destFileName Filename to be used on local file system.
     * @param bytesToRead The number of bytes to read (from the start).
     * @throws IOException If any problem during file read/write.
     */
    static public void downloadFile(String url, String destFileName, int bytesToDownload)
            throws IOException {
        Log.i("DownloadFile", "Downloading url: " + url + ", dest: " + destFileName);
        FileOutputStream fos = null;

        try {

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
            Log.i("DownloadFile", "Downloading complete: " + bytesReadSoFar + " bytes downloaded");
        } finally {
            if (null != fos) {
                fos.close();
            }
        }
    }

    /**
     * Downloads file from a specified Url to local file system.
     * 
     * @param url Location of the file on the Internet.
     * @param destFileName Filename to be used on local file system.
     * @throws IOException
     */
    static public void downloadFile(String url, String destFileName) throws IOException {
        downloadFile(url, destFileName, -1);
    }

    /**
     * Generates streaming downloader destination folder name.
     * 
     * @param url Location of the file on the Internet
     */
    static public String generateDownloadFolderName(String url) {
        String dirPath = String.format("%s/%08X", CspConstants.CONTENT_DIR, url.hashCode());
        return dirPath;
    }

}
