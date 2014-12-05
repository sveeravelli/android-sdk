package com.example.secureplayer.apis;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.IDxRightsInfo;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmCommunicationFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.drmdlc.api.exceptions.DrmInvalidFormatException;
import com.discretix.drmdlc.api.exceptions.DrmNotProtectedException;
import com.example.secureplayer.DxAsyncTaskBase;
import com.example.secureplayer.DxConstants;
import com.example.secureplayer.RightsInfoLocalizer;

public class DisplayRightsInfoActivity extends Activity {
	
	private class DrmExecuter extends DxAsyncTaskBase {
		public DrmExecuter(){
			super(DisplayRightsInfoActivity.this, "Rights Info");
		}
		
		@Override
		protected DxResult doInBackground(Void... arg0) {
			String localContentFilePath = DxConstants.getActiveContent().getTemplocalFile();
		    String userMessage;
		    Boolean doHaveRights = false;
		    try {
				IDxDrmDlc dlc = DxDrmDlc.getDxDrmDlc(DisplayRightsInfoActivity.this);
				IDxRightsInfo[] rights = dlc.getRightsInfo(DisplayRightsInfoActivity.this, localContentFilePath);
				userMessage = "Rights info: " + RightsInfoLocalizer.toSringRightsInfo(DisplayRightsInfoActivity.this, rights);
				doHaveRights = dlc.verifyRights(localContentFilePath);
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
			} catch (IOException e) {
				userMessage = "Exception: IOException";
			} 
			
		    return new DxResult(userMessage, doHaveRights);
		    
		}
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    new DrmExecuter().execute();
	}
}
