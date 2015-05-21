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

public class DeleteRightsActivity extends Activity {

	private class DrmExecuter extends DxAsyncTaskBase {
		
		public DrmExecuter(){
			super(DeleteRightsActivity.this, "Delete Rights");
		}
		
		@Override
		protected DxResult doInBackground(Void... arg0) {
			String userMessage;
			String fileName = DxConstants.getActiveContent().getTemplocalFile();
			Boolean isPassed = false;
			try {
				// First we need check if there is a local Content Path.
				if (!Utils.checkFileExists(fileName)) {
					userMessage = "You need to download the file before you delete Rights.";
					return new DxResult(userMessage, false);
				}

				// Setting the config object enables logging from DRM core
				// library.
				DxLogConfig config = null; // new
											// DxLogConfig(DxLogConfig.LogLevel.Info,
											// 0x0a);
				IDxDrmDlc dlc = DxDrmDlc.getDxDrmDlc(DeleteRightsActivity.this, config);

				dlc.deleteRights (fileName);

				userMessage = "Delete license successful";
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
		new DrmExecuter().execute();
	}

}
