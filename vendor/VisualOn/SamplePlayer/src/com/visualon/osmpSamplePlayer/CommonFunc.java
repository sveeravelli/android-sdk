/************************************************************************
VisualOn Proprietary
Copyright (c) 2013, VisualOn Incorporated. All rights Reserved

VisualOn, Inc., 4675 Stevens Creek Blvd, Santa Clara, CA 95051, USA

All data and information contained in or disclosed by this document are
confidential and proprietary information of VisualOn, and all rights
therein are expressly reserved. By accepting this material, the
recipient agrees that this material and the information contained
therein are held in confidence and in trust. The material may only be
used and/or disclosed as authorized in a license agreement controlling
such use and disclosure.
************************************************************************/

package com.visualon.osmpSamplePlayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

public class CommonFunc {
   
    public static String getUserPath(Context context) {
        
        String path = context.getPackageName();
        String userPath = "/data/data/" + path;
      
        try {
            PackageInfo p = context.getPackageManager().getPackageInfo(path, 0);
            userPath = p.applicationInfo.dataDir;
        } catch (NameNotFoundException e) {
        }
        
        return userPath;
    }

	public static String getUserNativeLibPath(Context context) {
        
        String path = context.getPackageName();
        String userPath = "/data/data/" + path+ "/lib";
        if(Build.VERSION.SDK_INT<10){
            try {
                PackageInfo p = context.getPackageManager().getPackageInfo(path, 0);
                userPath = p.applicationInfo.dataDir+ "/lib";
            } catch (NameNotFoundException e) {
            }
        }
        else{
	        try {
	        	//for version below android 2.3.3, please remove codes below.
	            PackageInfo p = context.getPackageManager().getPackageInfo(path, 0);
	            userPath = p.applicationInfo.nativeLibraryDir;//dataDir;
	        } catch (NameNotFoundException e) {
	        }
        }
        
        return userPath;
    }
    
    /* Copy file from Assets directory to destination. Used for licenses and processor-specific configurations */
    public static void copyfile(Context context, String filename, String desName) {
        try {
            InputStream InputStreamis  = context.getAssets().open(filename);
            File desFile = new File(CommonFunc.getUserPath(context) + "/" + desName);
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
    
    /* Retrieve list of media sources */
    public static boolean ReadUrlInfoToList(List<String> listUrl, String configureFile) {
        String sUrl,line = "";
        sUrl = configureFile;
        File UrlFile = new File(sUrl);
        
        if (!UrlFile.exists())
            return false;
        
        FileReader fileread;

        try {
            fileread = new FileReader(UrlFile);
            BufferedReader bfr = new BufferedReader(fileread);
            try {
                while (line != null) {
                    line = bfr.readLine();
                    if (line !=null)
                        listUrl.add(line);
                }
              
                fileread.close();
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        return true;
    }
    
    public static String bitrateToString(int nBitr) {
       
        String s;
        nBitr /= 1024;
        if (nBitr < 1024) {
            s = Integer.toString(nBitr) + "k";
        } else {
            String str = Float.toString(nBitr / 1024.0f);
            int n = str.indexOf('.');
            if(n >= 0 && n <str.length() - 2)
                str = str.substring(0, n + 2);
            
            s = (str + "m");
        }
        return s;
    }
}
