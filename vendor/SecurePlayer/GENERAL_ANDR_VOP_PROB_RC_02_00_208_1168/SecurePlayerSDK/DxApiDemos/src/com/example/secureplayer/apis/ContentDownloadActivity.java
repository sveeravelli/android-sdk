package com.example.secureplayer.apis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.example.secureplayer.DxAsyncTaskBase;
import com.example.secureplayer.DxConstants;
import com.example.secureplayer.Utils;

public class ContentDownloadActivity extends Activity {

	private String mContentUrl = DxConstants.getActiveContent().getContentUrl();
	private String mLocalContentPath = DxConstants.getActiveContent().getTemplocalFile();
	private Boolean mIsContentStreaming = DxConstants.getActiveContent().IsStreaming();


	
	private class ContentDownloader extends DxAsyncTaskBase {
		
		public ContentDownloader(){
			super(ContentDownloadActivity.this, "Downloading content");
		}

		@Override
		protected DxResult doInBackground(Void... arg0) {
			// Will be overwritten on any exception
			String userMessage;
			Boolean isPassed = false;
			
			//Reset internal file name state (refer at HarmonicHlsDownloadActivity::doInBackground).
			DxConstants.getActiveContent().setmFileName(null);
			
			try {
				//If the file is for streaming the download will only download the first 100k of the file.
				if (mIsContentStreaming && shouldDownloadPartially(mContentUrl)) {
					Log.d("ContentDownloadActity", "Downloading only first 1MB form " + mContentUrl);
					Utils.DownloadFile(mContentUrl, mLocalContentPath, 1024 * 1024);
				} else {
					
					Utils.DownloadFile(mContentUrl, mLocalContentPath);					
				}
				
				userMessage = "download completed";
				isPassed = true;
			} catch (FileNotFoundException e) {
				userMessage = "Exception: FileNotFoundException";
			} catch (IOException e) {
				userMessage = "Exception: IOException, download failed";
			}

			return new DxResult(userMessage, isPassed);
		}

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContentUrl = DxConstants.getActiveContent().getContentUrl();
		mLocalContentPath = DxConstants.getActiveContent().getTemplocalFile();
		new ContentDownloader().execute();
	}
	
	private final String[] remoteFileExtensions = {"eny", "ismv"};
	
	/**
	 * test whether file denoted by the url argument should be fully download or not, according to {@link #fullDownloadFileExtensions}  
	 * @param url - URL of the current file
	 * @return true in case current file should be downloaded as fully
	 */
	private boolean shouldDownloadPartially(String url) {
		for (String fileEtension : remoteFileExtensions) {
			if (url.toLowerCase(Locale.getDefault()).endsWith(fileEtension)) {
				return true;
			}
		}
		
		return false;
	}
}
