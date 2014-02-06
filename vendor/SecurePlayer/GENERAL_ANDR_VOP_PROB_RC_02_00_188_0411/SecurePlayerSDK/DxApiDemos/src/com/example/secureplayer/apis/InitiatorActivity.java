package com.example.secureplayer.apis;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.DxLogConfig;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.drmdlc.api.exceptions.DrmServerSoapErrorException;
import com.example.secureplayer.DxAsyncTaskBase;
import com.example.secureplayer.DxConstants;

public class InitiatorActivity extends Activity {
	private String mInitiatorUrl = DxConstants.getActiveContent().getInitiatorUrl();


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		mInitiatorUrl = DxConstants.getActiveContent().getInitiatorUrl();
		new DrmExecuter().execute();

	}

	private class DrmExecuter extends DxAsyncTaskBase {
		
		public DrmExecuter(){
			super(InitiatorActivity.this, "Executing Initiator");
		}

		@Override
		protected DxResult doInBackground(Void... arg0) {
			String userMessage;
			Boolean isPassed = false;
			
			if (mInitiatorUrl == null || mInitiatorUrl.equals("")) {
				userMessage = "No Initiator URL found.";
				return new DxResult(userMessage, false);
			}
			
			try {
				// Setting the config object enables logging from DRM core library.
				DxLogConfig config = null; //new DxLogConfig(DxLogConfig.LogLevel.Info, 0x0a);
				
				IDxDrmDlc dlc = DxDrmDlc.getDxDrmDlc(InitiatorActivity.this ,config);

				Uri initiatorUri = Uri.parse(mInitiatorUrl);		
				
				// Import the rights in the initiator URL
				dlc.setCookies(getCookiesArry());
				dlc.executeInitiator(initiatorUri);
				dlc.setCookies(null);
				userMessage = "License acquisition successful";
				isPassed = true;
				
			} catch (DrmClientInitFailureException e) {
				userMessage = "Exception: DrmClientInitFailureException";
			} catch (DrmGeneralFailureException e) {
				userMessage = "Exception: DrmGeneralFailureException";
			} catch (FileNotFoundException e) {
				userMessage = "Exception: FileNotFoundException";
			} catch (IOException e) {
				userMessage = "Exception: IOException";
			} catch (DrmServerSoapErrorException e) {
				String customData = e.getCustomData();
				String redirectUrl = e.getRedirectUrl();
				String soapError = e.getSoapMessage();
				userMessage = "Exception: DrmServerSoapErrorException: CustomData = "
						+ customData + " RedirectUrl = " + redirectUrl+"\nSoap error:\n"+soapError;
			}

			return new DxResult(userMessage, isPassed);
		}

		private String[] getCookiesArry() {
			return DxConstants.getActiveContent().getCookiesArry();			
		}
	}
}
