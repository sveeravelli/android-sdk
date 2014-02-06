package com.example.secureplayer.apis;

import android.app.Activity;
import android.os.Bundle;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.drmdlc.api.exceptions.DrmNotSupportedException;
import com.discretix.drmdlc.api.exceptions.DrmUpdateRequiredException;
import com.example.secureplayer.DxAsyncTaskBase;
import com.example.secureplayer.DxConstants;

public class PersonalizationActivity extends Activity {
	// private boolean stopFlag = false;
	private IDxDrmDlc mDrmProvider = null;

	private static final boolean LOCAL_PEROSNALIZATION = true;
	private static final String SERVER_URL = DxConstants.PERSONALIZATION_URL;
	private static final String SESSION_ID = DxConstants.SESSION_ID;

	private class PersonalizationExecuter extends DxAsyncTaskBase {
		
		public PersonalizationExecuter(){
			super(PersonalizationActivity.this, "Performing personalization");
		}
		
		@Override
		protected DxResult doInBackground(Void... params) {
			// Will be overwritten on any error
			String message = "Personalization successful";
			Boolean isPassed = false;
			
			try {
				mDrmProvider = DxDrmDlc
						.getDxDrmDlc(PersonalizationActivity.this);

				if (LOCAL_PEROSNALIZATION) {
					mDrmProvider.getDebugInterface()
							.setClientSideTestPersonalization(true);
				}

				if (!mDrmProvider.personalizationVerify()) {
					mDrmProvider.performPersonalization("Example", SERVER_URL,
							SESSION_ID);
				} else { // personalization was already done
					message = "Device is already personalized";
				}
				isPassed = true;
			} catch (DrmClientInitFailureException e1) {
				message = "Exception: DrmClientInitFailureException";
			} catch (DrmGeneralFailureException e) {
				message = "Exception: DrmGeneralFailureException";
			} catch (DrmUpdateRequiredException e) {
				message = "Exception: DrmUpdateRequiredException";
			} catch (DrmNotSupportedException e) {
				message = "Exception: DrmNotSupportedException";
			}

			return new DxResult(message, isPassed);
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new PersonalizationExecuter().execute();
	}
}
