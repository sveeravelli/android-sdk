package com.example.secureplayer.apis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;

import com.example.secureplayer.DxAsyncTaskBase;
import com.example.secureplayer.DxConstants;
import com.example.secureplayer.DxContentItem;
import com.example.secureplayer.Utils;

public class HarmonicHlsDownloadActivity extends Activity {
	
	private class HlsDownloader extends DxAsyncTaskBase {
		
		
		public HlsDownloader(){
			super(HarmonicHlsDownloadActivity.this, "Downloading content");
		}

		@Override
		protected DxResult doInBackground(Void... arg0) {
			// Will be overwritten on any exception
			String userMessage;
			Boolean isPassed = false;
			
			//Reset internal file name state
			DxConstants.getActiveContent().setmFileName(null);
			
			try {
				if (!DxConstants.getActiveContent().getContentUrl().toLowerCase(Locale.getDefault()).endsWith(".m3u8")){
					return new DxResult("Not applicable for current content", false);
				}
				
				//calculate .TS file URL
				String tsUrl = getTsUrl(DxConstants.getActiveContent().getContentUrl());
				
				if (null == tsUrl){
					throw new IOException("Failed to download .TS file");
				}
				//generate local file name for a .TS URL
				String tsFileName = DxContentItem.generateFileName(tsUrl);
				
				//set internal file name to .TS file - so all following file based actions will work with it		
				DxConstants.getActiveContent().setmFileName(tsFileName);
				
				//download .TS file
				Utils.DownloadFile(tsUrl, tsFileName, 100 * 1024);
				
				userMessage = "download completed";
				isPassed = true;
			} catch (FileNotFoundException e) {
				userMessage = "Exception: FileNotFoundException";
			} catch (IOException e) {
				userMessage = "Exception: IOException, download failed";
			}

			return new DxResult(userMessage, isPassed);
		}
		
		private String getTsUrl(String url) throws IOException{
			
			URL playlistUrl = null;
			String baseUrl = url.substring(0, url.lastIndexOf('/') +1);
			BufferedReader bufferedReader = null;
			try {
				playlistUrl = new URL(url);
		        bufferedReader = new BufferedReader(new InputStreamReader(playlistUrl.openConnection().getInputStream()));
		        String inputLine;

		        while ((inputLine = bufferedReader.readLine()) != null){ 
		            String trimmedLine = inputLine.trim();
		            String trimmedLineWithoutQuery = trimmedLine;
		            if (trimmedLine.lastIndexOf('?')>0){//check if there is a query string
		            	trimmedLineWithoutQuery = trimmedLine.substring(0, trimmedLine.lastIndexOf('?'));//skip query string
		    		}
		            if (trimmedLine.equals("") || trimmedLine.startsWith("#")){
		            	continue;
		            } else if (trimmedLineWithoutQuery.toLowerCase(Locale.getDefault()).endsWith(".m3u8")){
		            	return getTsUrl(baseUrl + trimmedLine);
		            } else if (trimmedLineWithoutQuery.toLowerCase(Locale.getDefault()).endsWith(".ts")){
		            	return baseUrl + trimmedLine;
		            }
		        }
			} finally {
				if (null != bufferedReader){
					bufferedReader.close();
				}
			}
			return null;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new HlsDownloader().execute();
	}

}
