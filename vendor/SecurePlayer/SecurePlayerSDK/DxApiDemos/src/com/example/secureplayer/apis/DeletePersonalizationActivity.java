package com.example.secureplayer.apis;

import android.app.Activity;
import android.os.Bundle;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.example.secureplayer.DxAsyncTaskBase;

public class DeletePersonalizationActivity extends Activity {
	private IDxDrmDlc mDrmProvider = null;


	private class PersonalizationExecuter extends DxAsyncTaskBase {
		
		
		public PersonalizationExecuter(){
			super(DeletePersonalizationActivity.this, "Deleting Personalization");
		}
		
		@Override
		protected DxResult doInBackground(Void... params) {
			// Will be overwritten on any error
			String message = "Personalization was deleted";
			Boolean isPassed = false;
			
			try {
				mDrmProvider = DxDrmDlc
						.getDxDrmDlc(DeletePersonalizationActivity.this);
				if (!mDrmProvider.personalizationVerify()) {
					message = "the device is not personalized";
				} else { 
					mDrmProvider.getDebugInterface().deletePersonalization();
					mDrmProvider.getDebugInterface().deletePlayReadyStore();
				}
				isPassed = true;
			} catch (DrmClientInitFailureException e1) {
				message = "Exception: DrmClientInitFailureException";
			} catch (DrmGeneralFailureException e) {
				message = "Exception: DrmGeneralFailureException";
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
