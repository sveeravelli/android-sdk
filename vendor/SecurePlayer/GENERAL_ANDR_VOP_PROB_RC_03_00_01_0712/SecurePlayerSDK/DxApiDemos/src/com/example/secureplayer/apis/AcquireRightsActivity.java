package com.example.secureplayer.apis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.DxLogConfig;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmCommunicationFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.drmdlc.api.exceptions.DrmInvalidFormatException;
import com.discretix.drmdlc.api.exceptions.DrmNotProtectedException;
import com.discretix.drmdlc.api.exceptions.DrmServerSoapErrorException;
import com.example.secureplayer.DxAsyncTaskBase;
import com.example.secureplayer.DxConstants;
import com.example.secureplayer.DxContentItem.ECustomDataType;
import com.example.secureplayer.Utils;

public class AcquireRightsActivity extends Activity {

	private class DrmExecuter extends DxAsyncTaskBase {
		
		public DrmExecuter(){
			super(AcquireRightsActivity.this, "Acquiring Rights");
		}
		
		@Override
		protected DxResult doInBackground(Void... arg0) {
			String userMessage;
			String fileName = DxConstants.getActiveContent().getTemplocalFile();
			Boolean isPassed = false;
			try {
				// First we need check if there is a local Content Path.
				if (!Utils.checkFileExists(fileName)) {
					userMessage = "You need to download the file befor you acquire Rights.";
					return new DxResult(userMessage, false);
				}

				// Setting the config object enables logging from DRM core
				// library.
				DxLogConfig config = null; // new
											// DxLogConfig(DxLogConfig.LogLevel.Info,
											// 0x0a);
				IDxDrmDlc dlc = DxDrmDlc.getDxDrmDlc(AcquireRightsActivity.this, config);

				String customData = getCustomData();
				String customUrl  = getCustomUrl();
				dlc.setCookies(getCookiesArry());
				dlc.acquireRights(fileName, customData, customUrl);
				dlc.setCookies(null);

				userMessage = "License acquisition successful";
				isPassed = true;
			} catch (DrmClientInitFailureException e) {
				userMessage = "Exception: DrmClientInitFailureException";
			} catch (DrmGeneralFailureException e) {
				userMessage = "Exception: DrmGeneralFailureException";
			} catch (FileNotFoundException e) {
				userMessage = "Exception: FileNotFoundException";
			} catch (DrmNotProtectedException e) {
				userMessage = "Exception: DrmNotProtectedException";
			} catch (DrmInvalidFormatException e) {
				userMessage = "Exception: DrmInvalidFormatException";
			} catch (DrmCommunicationFailureException e) {
				userMessage = "Exception: DrmCommunicationFailureException";
			} catch (DrmServerSoapErrorException e) {
				String customData = e.getCustomData();
				String redirectUrl = e.getRedirectUrl();
				userMessage = "Exception: DrmServerSoapErrorException: CustomData = "
						+ customData + " RedirectUrl = " + redirectUrl;
			} catch (IOException e) {
				userMessage = "Exception: IOException, download failed";
			}

			return new DxResult(userMessage, isPassed);
		}

		private String readFile(String path) throws IOException {
			
			Reader reader = null;
		    try {
		        reader = new BufferedReader(new FileReader(path));
		        StringBuilder builder = new StringBuilder();
		        char[] buffer = new char[8192];
		        int read;
		        while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
		            builder.append(buffer, 0, read);
		        }
		        return builder.toString();
		    } finally {
		        if (null != reader){
		        	reader.close();
		        }
		    }   
	    }
		
		private String readUrl(String url) throws IOException{
			StringBuilder customData = new StringBuilder();
			BufferedReader bufferedReader = null;
			try {
				URL theUrl = new URL(url);
		        bufferedReader = new BufferedReader(new InputStreamReader(theUrl.openConnection().getInputStream()));
		        String inputLine;

		        while ((inputLine = bufferedReader.readLine()) != null){
		        	customData.append(inputLine);
		        }
			} finally {
				if (null != bufferedReader){
					bufferedReader.close();
				}
			}
			return customData.toString();

		}
		
		private String getCustomData() throws IOException{
			String customData = null;
			ECustomDataType customDataType = DxConstants.getActiveContent().getCustomDataType();
			
			switch (customDataType) {
			case CUSTOM_DATA_IS_FILE:
				customData = readFile(DxConstants.getActiveContent().getCustomData());
				break;
			case CUSTOM_DATA_IS_TEXT:
				customData = DxConstants.getActiveContent().getCustomData();
				break;
			case CUSTOM_DATA_IS_URL:
				customData = readUrl(DxConstants.getActiveContent().getCustomData());
				break;
			}
			
			if (null != customData && !customData.equals("")){
				Log.w(DxConstants.TAG, "loaded custom data: " + customData);	
				return customData;
			}
			return null;
		}
		private String getCustomUrl(){
			String customUrl = DxConstants.getActiveContent().getCustomUrl();
			
			if (null != customUrl && !customUrl.equals("")){
				Log.i(DxConstants.TAG, "loaded custom Url: " + customUrl);
				return customUrl;
			}
			
			return null;
		}

		private String[] getCookiesArry() {
			return DxConstants.getActiveContent().getCookiesArry();			
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new DrmExecuter().execute();
	}

}
