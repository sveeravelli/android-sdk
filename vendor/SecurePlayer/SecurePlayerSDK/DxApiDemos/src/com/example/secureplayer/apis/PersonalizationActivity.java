package com.example.secureplayer.apis;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmCommunicationFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.drmdlc.api.exceptions.DrmNotSupportedException;
import com.discretix.drmdlc.api.exceptions.DrmUpdateRequiredException;
import com.example.secureplayer.DxAsyncTaskBase;
import com.example.secureplayer.DxConstants;
import com.example.secureplayer.R;

public class PersonalizationActivity extends Activity {
	// private boolean stopFlag = false;
	private IDxDrmDlc mDrmProvider = null;

	private Button      m_btnPersonalize;
	private CheckBox    m_cbLocalPerso;
	private EditText    m_etRemoteURL;
	private EditText    m_etSessionID;
	private EditText    m_etAppVersion;
	
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

				mDrmProvider
						.getDebugInterface()
						.setClientSideTestPersonalization(m_cbLocalPerso.isChecked());

				if (!mDrmProvider.personalizationVerify()) {
					mDrmProvider.performPersonalization(m_etAppVersion.getText().toString(), m_etRemoteURL.getText().toString(),
							m_etSessionID.getText().toString());
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
			} catch (DrmCommunicationFailureException e) {
				message = "Exception: DrmCommunicationFailureException";
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
		
		setContentView(R.layout.personalization);
		
		SharedPreferences preferences = this.getSharedPreferences("DxApiDemos", Context.MODE_PRIVATE);  

		m_cbLocalPerso = (CheckBox)findViewById(R.id.cbLocalPerso);
		
	    Boolean checked = preferences.getBoolean("PERSONALIZATION_LOCAL", DxConstants.PERSONALIZATION_LOCAL);
	    m_cbLocalPerso.setChecked(checked);  
	    m_cbLocalPerso.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				m_etRemoteURL.setEnabled(! m_cbLocalPerso.isChecked());
				m_etRemoteURL.setFocusable(! m_cbLocalPerso.isChecked());
				m_etRemoteURL.setFocusableInTouchMode(! m_cbLocalPerso.isChecked());
			}
		});

		m_etRemoteURL = (EditText)findViewById(R.id.etRemoteURL);
		String remoteURL = preferences.getString("PERSONALIZATION_SERVER_URL", DxConstants.PERSONALIZATION_URL);
		if (remoteURL.isEmpty()) remoteURL = DxConstants.PERSONALIZATION_URL;
		m_etRemoteURL.setText(remoteURL);
		
		m_etRemoteURL.setEnabled(! m_cbLocalPerso.isChecked());
		m_etRemoteURL.setFocusable(! m_cbLocalPerso.isChecked());
		m_etRemoteURL.setFocusableInTouchMode(! m_cbLocalPerso.isChecked());
		
		m_etSessionID = (EditText)findViewById(R.id.etSessionID);
		String sessionID = preferences.getString("PERSONALIZATION_SESSION_ID", DxConstants.PERSONALIZATION_SESSION_ID);
		if (sessionID.isEmpty()) sessionID = DxConstants.PERSONALIZATION_SESSION_ID;
		m_etSessionID.setText(sessionID);
				
		m_etAppVersion = (EditText)findViewById(R.id.etAppVersion);
		String appVersion = preferences.getString("PERSONALIZATION_APP_VERSION", DxConstants.PERSONALIZATION_APPLICATION_VERSION);
		if (appVersion.isEmpty()) appVersion = DxConstants.PERSONALIZATION_APPLICATION_VERSION;
		m_etAppVersion.setText(appVersion);

		m_btnPersonalize = (Button)findViewById(R.id.btnPersonalize);
		m_btnPersonalize.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				m_btnPersonalize.setEnabled(false);

				new PersonalizationExecuter().execute();
			}
		});
	}
	
	/** Called when the activity is destroyed. */
	@Override
	public void onDestroy() {
		
		SharedPreferences preferences = this.getSharedPreferences("DxApiDemos", Context.MODE_PRIVATE);  
		
		SharedPreferences.Editor editor = preferences.edit();
		
		editor.putBoolean("PERSONALIZATION_LOCAL", m_cbLocalPerso.isChecked());
		editor.putString("PERSONALIZATION_SERVER_URL", m_etRemoteURL.getText().toString());
		editor.putString("PERSONALIZATION_SESSION_ID", m_etSessionID.getText().toString());
		editor.putString("PERSONALIZATION_APP_VERSION", m_etAppVersion.getText().toString());
		
		editor.commit();
		
		super.onDestroy();
	}
}
