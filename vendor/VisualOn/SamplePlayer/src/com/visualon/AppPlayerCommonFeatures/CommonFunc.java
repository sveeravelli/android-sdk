/************************************************************************
VisualOn Proprietary
Copyright (c) 2014, VisualOn Incorporated. All rights Reserved

VisualOn, Inc., 4675 Stevens Creek Blvd, Santa Clara, CA 95051, USA

All data and information contained in or disclosed by this document are
confidential and proprietary information of VisualOn, and all rights
therein are expressly reserved. By accepting this material, the
recipient agrees that this material and the information contained
therein are held in confidence and in trust. The material may only be
used and/or disclosed as authorized in a license agreement controlling
such use and disclosure.
************************************************************************/

package com.visualon.AppPlayerCommonFeatures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.visualon.OSMPPlayer.VOOSMPVerificationInfo;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class CommonFunc {
    private static final String  TAG                  = "@@@OSMP+CommonFunc"; 
    private static CPlayer m_cplayer                  = null;
    private static CDownloader m_cDownloader          = null;
    public static SharedPreferences m_spMain          = null;
    public static Editor m_editor                     = null;
    public static void setCPlayer(CPlayer player) {
        m_cplayer = player;
    }
    public static CPlayer getCPlayer() {
        return m_cplayer;
    }
    
    public static VOOSMPVerificationInfo creatVerificationInfo(String str)
    {
        VOOSMPVerificationInfo info = new VOOSMPVerificationInfo();
        info.setDataFlag(0);
        info.setVerificationString(str); 
        return info;
    }
    
    public static void setCDownloader(CDownloader downloader) {
        m_cDownloader = downloader;
    }
    public static CDownloader getCDownloader() {
        return m_cDownloader;
    }
    
    public static void setApplicationSharedPreference(SharedPreferences preference) {
	    m_spMain = preference;
	    m_editor = m_spMain.edit();
    }
    public static SharedPreferences getApplicationSharedPreference() {
        return m_spMain;
    }
    public static void saveStringPreferenceValue(String key,String value) {
    	if(m_editor != null) {
    	    m_editor.putString(key, value);
    	    m_editor.commit();
    	}
    }
    public static String getStringPreferenceValue(String key) {
    	if (m_spMain != null)
    	    return m_spMain.getString(key, "");
    	else 
    		return "";
    }
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
    public static String getApkPath(Context context) {  //Hard code for apk path to adapt multi-user feature (at least API level 17)
        
        String path = context.getPackageName();
        String userPath = "/data/data/" + path + "/lib/";  
        return userPath;
    }
    @SuppressLint("NewApi")
    public static String getUserNativeLibPath(Context context) {
        
        String path = context.getPackageName();
        String userPath = "/data/data/" + path+ "/lib/";
		try {
			PackageInfo p = context.getPackageManager().getPackageInfo(path, 0);
			userPath = p.applicationInfo.dataDir + "/lib/";
		} catch (NameNotFoundException e) {
		}
		File libFile = new File(userPath);
		if (!libFile.exists() || null == libFile.listFiles()) {
	        try {
	            PackageInfo p = context.getPackageManager().getPackageInfo(path, 0);
	            userPath = p.applicationInfo.nativeLibraryDir + "/";
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
    
    
    /* Copy file from Assets directory to destination. Used to copy  files to local directory now. */
    public static void copyfileTo(Context context, String filename, String desName) {
        try {
            InputStream InputStreamis  = context.getAssets().open(filename);
            File desFile = new File(desName);
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
    
    public static void ReadUrlInfo(Context context, ArrayList<String> listUrl){
        voLog.i(TAG, "Current external storage directory is %s", Environment.getExternalStorageDirectory().getAbsolutePath());
        String str = Environment.getExternalStorageDirectory().getAbsolutePath() + "/url.txt";
        if (ReadUrlInfoToList(listUrl, str) == false)
            Toast.makeText(context, "Could not find " + str, Toast.LENGTH_LONG).show();
    }
    
    public static void getLocalFiles(ArrayList<String> list,String url){
        File files = new File(url); 
        File[] file = files.listFiles();
        if(file == null){
            return;
        }
        try {
            for (File f : file) { 
            if (!f.isDirectory()) { 
               list.add(f.getPath());
                 }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void getDownloadFiles(ArrayList<String> list,String url){
        File files = new File(url); 
        File[] file = files.listFiles();
        if(file == null){
            return;
        }
        try {
            for (File f : file) { 
            if (f.isDirectory()) { 
               File[] videoFile = f.listFiles();
               int i = 0;
               for(File vf : videoFile){
            	   if(vf.getPath().contains(".manifest")){
                       list.add(vf.getPath());
                   }
                   if(vf.getPath().contains("Master.m3u8")){
                       list.add(vf.getPath());
                       i = 1;
                   }
               }
               if(i == 0){
                   for(File vf : videoFile){
                       if(vf.getPath().contains("Video.m3u8"))
                           list.add(vf.getPath());
                   }
               }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter(); 
        if (listAdapter == null) {
       
        return;
         }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {  
        View listItem = listAdapter.getView(i, null, listView);
        listItem.measure(0, 0);  
        totalHeight += listItem.getMeasuredHeight(); 
         }    

       ViewGroup.LayoutParams params = listView.getLayoutParams();
       params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
   
       listView.setLayoutParams(params);
    }
    
    public static String bitrateToString(int nBitr)
    {
        String s;
        nBitr /= 1000;
        if (nBitr<1000) {
            s = Integer.toString(nBitr) + "kbps";
        }   
        else {
            String str = Float.toString(nBitr/1000.0f);
            int n = str.indexOf('.');
            if(n>=0 && n<str.length()-2)
                str = str.substring(0, n+2);
            
            s = (str + "Mbps");
        }
        return s;
    }
    public static String formatTime(long time){
        long hour = time/60/60;
        long minute = time/60%60;
        long second = time%60;
        final String format = String.format(Locale.US, "%02d:%02d:%02d", hour, minute, second);
        return format;
    }
    
    public static String uriToVideoPath(Activity activity,Uri uri){
        String videoPath = null;
        if (uri != null) {
            // If media source was passed as URI data, use it
                  if(uri.toString().startsWith("content"))//change image uri to file path
                {
                    String[] proj = { MediaStore.Video.Media.DATA };   
                    Cursor actualimagecursor =  activity.managedQuery(uri,proj,null,null,null);  
                    int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);   
                    actualimagecursor.moveToFirst();   

                   String img_path = actualimagecursor.getString(actual_image_column_index);  
                   File file = new File(img_path);
                   uri = Uri.fromFile(file);
                }
                  videoPath = uri.getPath();

                int i = videoPath.indexOf("/mnt/");
                if (i != -1) {
                    //local file
                    videoPath = videoPath.subSequence(i, videoPath.length()).toString();
                } else {
                    //stream
                    videoPath = uri.toString();
                }

              
            }
        return videoPath;
    }
   
    public static boolean checkFileExt(String str)
    {   
          Pattern audioPattern = Pattern.compile(".+\\.(mp3|amr|aac|wma|m4a|wav|ec3|ac3|mp2|ogg|ra|isma|flac|evrc|qcelp|pcm|adpcm|au|awb)$",Pattern.CASE_INSENSITIVE);
          Pattern videoPattern = Pattern.compile(".+\\.(avi|asf|rm|rmvb|mp4|m4v|3gp|3g2|wmv|3g2|mpg|mpeg|qt|mkv|flv|mov|asx|m3u8|m3u|manifest|mpd|ts|webm|ismv|ismc|k3g|sdp|265|h265|264|h264|m2ts|dts|dtshd)$",Pattern.CASE_INSENSITIVE);

          Matcher matcher = audioPattern.matcher(str);
          if(matcher.find())
          {
              return true;
          }
          
          matcher = videoPattern.matcher(str);
          if(matcher.matches())
          {
              return true;   
          }
          
          String strTest = str.toLowerCase();
          
          if (strTest.startsWith(Definition.PREFIX_MEDIAFILE_MTV) == true)
              return true;
          
          if (strTest.endsWith("/manifest") == true)
              return true;
          
          if (strTest.contains("/manifest?") == true)
              return true;
          
          if (strTest.contains("m3u8") == true)
              return true;
          
          if (strTest.contains("m3u?") == true)
              return true;
          
          int index = strTest.lastIndexOf("_");
          if (index != -1) {
              strTest = strTest.substring(index);
              if (strTest.length() == 5) {
              
                  if (isNumeric(strTest.substring(1))) {
                      return true; 
                  }
              }
          }
          
          return false;
    }
    
    public static boolean isNumeric(String str)
    {
          Pattern pattern = Pattern.compile("[0-9]*");
          Matcher isNum = pattern.matcher(str);
          return isNum.matches();
    }
    
    public static boolean isLink(String url){
        boolean bLink = false;
        
        int index = url.lastIndexOf(".");
        if (index == -1)
            return false;
        
        String strUrlSuffix = url.substring(index);
        
        if (strUrlSuffix.equalsIgnoreCase(Definition.SUFFIX_HTM)
                || strUrlSuffix.equalsIgnoreCase(Definition.SUFFIX_HTML)
                || strUrlSuffix.equalsIgnoreCase(Definition.SUFFIX_PHP)
                || strUrlSuffix.equalsIgnoreCase(Definition.SUFFIX_ASP)
                || strUrlSuffix.equalsIgnoreCase(Definition.SUFFIX_ASPX)) {
            bLink = true;
        }
        
        return bLink;
    }
    
    public static void readHistoryList(ArrayList<String> lstHistory, String strPackage) {
        
        String strFilePathName;
        if (CommonFunc.checkSDCard()) {
            strFilePathName = Definition.DOWNLOAD_PATH
                + "/" + Definition.DEFAULT_HISTORYLIST_FILENAME;
        } else {
            strFilePathName = Definition.DEFAULT_DATAFILE_PATH
                + strPackage + "/" + Definition.DEFAULT_HISTORYLIST_FILENAME;
        }
        
        readFileToList(lstHistory, strFilePathName);
    }
    
    public static boolean checkSDCard() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }
    
    private static void readFileToList(List<String> lstUrl, String filePathName)
    {
       String sUrl, line = "";
       sUrl = filePathName;
       File UrlFile = new File(sUrl);
     
       if (!UrlFile.exists()) {
           return;
       }

       try {
           FileReader fileread = new FileReader(UrlFile);
           BufferedReader bfr = new BufferedReader(fileread);
           try {
               while (line != null) {
                   line = bfr.readLine();
                   if (line != null && line.length() != 0) {
                       lstUrl.add(line);
                   }
               }
               
               bfr.close();
               fileread.close();
               
           } catch (IOException e) {
               e.printStackTrace();
           }
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       } 
       
   }
    
    public static void addStringToFile(String filePathName, String str) {

        String line = "";
        boolean bNewCreate = false;

        File urlFile = new File(filePathName);
        if (!urlFile.exists()) {
            try {
                
                int index = filePathName.lastIndexOf("/");
                if (index == -1)
                    return;
                
                String strFolder = filePathName.substring(0, index);
                File fFolder = new File(strFolder);
                if (fFolder.exists() == false) {
                   boolean bRet = fFolder.mkdirs();
                   if (bRet == false)
                      return;
                }
                
                urlFile.createNewFile();
                bNewCreate = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileReader fileread = new FileReader(urlFile);
            BufferedReader bfr = new BufferedReader(fileread);
            try {
                while (line != null) {

                    line = bfr.readLine();
                    if (line != null) {
                        if (line.equalsIgnoreCase(str)) {
                            bfr.close();
                            fileread.close();
                            return;
                        }
                    }

                }

                bfr.close();
                fileread.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            RandomAccessFile raf = new RandomAccessFile(filePathName, "rw");
            try {

                raf.seek(raf.length());
                if (bNewCreate == false) {
                    raf.writeBytes("\r\n");
                }
                raf.writeBytes(str);
                raf.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
   
}
