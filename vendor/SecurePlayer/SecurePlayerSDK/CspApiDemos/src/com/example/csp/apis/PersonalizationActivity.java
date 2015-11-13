/*******************************************************************************
 * Copyright
 *  This code is strictly confidential and the receiver is obliged to use it
 *  exclusively for his or her own purposes. No part of Viaccess Orca code may be
 *  reproduced or transmitted in any form or by any means, electronic or
 *  mechanical, including photocopying, recording, or by any information storage
 *  and retrieval system, without permission in writing from Viaccess Orca.
 *  The information in this code is subject to change without notice. Viaccess Orca
 *  does not warrant that this code is error free. If you find any problems
 *  with this code or wish to make comments, please report them to Viaccess Orca.
 *  
 *  Trademarks
 *  Viaccess Orca is a registered trademark of Viaccess S.A in France and/or other
 *  countries. All other product and company names mentioned herein are the
 *  trademarks of their respective owners.
 *  Viaccess S.A may hold patents, patent applications, trademarks, copyrights
 *  or other intellectual property rights over the code hereafter. Unless
 *  expressly specified otherwise in a Viaccess Orca written license agreement, the
 *  delivery of this code does not imply the concession of any license over
 *  these patents, trademarks, copyrights or other intellectual property.
 *******************************************************************************/

package com.example.csp.apis;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.exceptions.DrmAndroidPermissionMissingException;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmCommunicationFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.drmdlc.api.exceptions.DrmNotSupportedException;
import com.discretix.drmdlc.api.exceptions.DrmUpdateRequiredException;
import com.example.csp.CspAsyncTaskBase;
import com.example.csp.CspConstants;
import com.example.csp.R;

/**
 * Represents the activity to perform Personalization. The goal of the personalization process is to
 * provide Client Application with credentials (certificates and keys) in a secure manner.
 */
public class PersonalizationActivity extends Activity {

    // Views elements
    private Button m_btnPersonalize;
    private CheckBox m_cbLocalPerso;
    private EditText m_etRemoteURL;
    private EditText m_etSessionID;
    private EditText m_etAppVersion;

    /**
     * Represents the asynchronous task to do Personalization.
     */
    private class PersonalizationExecuter extends CspAsyncTaskBase {

        public PersonalizationExecuter() {
            super(PersonalizationActivity.this, "Performing personalization");
        }

        @Override
        protected CspResult doInBackground(Void... params) {
            // Will be overwritten on any error
            String message = "Personalization successful";
            Boolean isPassed = false;

            try {
                
                // Get the CSP singleton instance
                IDxDrmDlc cspApiSingleton = DxDrmDlc.getDxDrmDlc(PersonalizationActivity.this);

                cspApiSingleton.getDebugInterface()
                        .setClientSideTestPersonalization(m_cbLocalPerso.isChecked());

                if (!cspApiSingleton.personalizationVerify()) {
                    cspApiSingleton.performPersonalization(m_etAppVersion.getText().toString(),
                            m_etRemoteURL.getText().toString(), m_etSessionID.getText().toString());
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
            } catch (DrmAndroidPermissionMissingException e) {
            	message = "Exception: DrmAndroidPermissionMissingException "+e.getMessage();
			}

            return new CspResult(message, isPassed);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.personalization);

        SharedPreferences preferences = this.getSharedPreferences("CspApiDemos",
                Context.MODE_PRIVATE);

        m_cbLocalPerso = (CheckBox) findViewById(R.id.cbLocalPerso);

        Boolean checked = preferences.getBoolean("PERSONALIZATION_LOCAL",
                CspConstants.PERSONALIZATION_LOCAL);
        m_cbLocalPerso.setChecked(checked);
        m_cbLocalPerso.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {

                m_etRemoteURL.setEnabled(!m_cbLocalPerso.isChecked());
                m_etRemoteURL.setFocusable(!m_cbLocalPerso.isChecked());
                m_etRemoteURL.setFocusableInTouchMode(!m_cbLocalPerso.isChecked());
            }
        });

        m_etRemoteURL = (EditText) findViewById(R.id.etRemoteURL);
        String remoteURL = preferences.getString("PERSONALIZATION_SERVER_URL",
                CspConstants.PERSONALIZATION_URL);
        if (remoteURL.isEmpty())
            remoteURL = CspConstants.PERSONALIZATION_URL;
        m_etRemoteURL.setText(remoteURL);

        m_etRemoteURL.setEnabled(!m_cbLocalPerso.isChecked());
        m_etRemoteURL.setFocusable(!m_cbLocalPerso.isChecked());
        m_etRemoteURL.setFocusableInTouchMode(!m_cbLocalPerso.isChecked());

        m_etSessionID = (EditText) findViewById(R.id.etSessionID);
        String sessionID = preferences.getString("PERSONALIZATION_SESSION_ID",
                CspConstants.PERSONALIZATION_SESSION_ID);
        if (sessionID.isEmpty())
            sessionID = CspConstants.PERSONALIZATION_SESSION_ID;
        m_etSessionID.setText(sessionID);

        m_etAppVersion = (EditText) findViewById(R.id.etAppVersion);
        String appVersion = preferences.getString("PERSONALIZATION_APP_VERSION",
                CspConstants.PERSONALIZATION_APPLICATION_VERSION);
        if (appVersion.isEmpty())
            appVersion = CspConstants.PERSONALIZATION_APPLICATION_VERSION;
        m_etAppVersion.setText(appVersion);

        m_btnPersonalize = (Button) findViewById(R.id.btnPersonalize);
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

        SharedPreferences preferences = this.getSharedPreferences("CspApiDemos",
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("PERSONALIZATION_LOCAL", m_cbLocalPerso.isChecked());
        editor.putString("PERSONALIZATION_SERVER_URL", m_etRemoteURL.getText().toString());
        editor.putString("PERSONALIZATION_SESSION_ID", m_etSessionID.getText().toString());
        editor.putString("PERSONALIZATION_APP_VERSION", m_etAppVersion.getText().toString());

        editor.commit();

        super.onDestroy();
    }
}
