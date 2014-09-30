/*************************************************************************
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2013 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the
 * terms of the Adobe license agreement accompanying it.  If you have received this file from a
 * source other than Adobe, then your use, modification, or distribution of it requires the prior
 * written permission of Adobe.
 *
 * For the avoidance of doubt, this file is Documentation under the Agreement.
 ************************************************************************/

package com.adobe.adobepass.apps.demo.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import com.adobe.adobepass.accessenabler.utils.Log;
import com.adobe.adobepass.apps.demo.R;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;

public class TempPassResetActivity extends AbstractActivity {

    private static final String LOG_TAG = "TempPassResetActivity";

    private static String PREQUAL_URL = "prequal.api.auth-staging.adobe.com";
    private static String RELEASE_URL = "api.auth-staging.adobe.com";
    private static String RESET_SERVICE = "/reset-tempass/v2/reset";

    private RadioGroup radioGroup;
    private EditText editCustomUrl;
    private String requestorId;
    private EditText mvpdIdEdit;

    private ProgressDialog progressDialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.temp_pass_reset);
        radioGroup = (RadioGroup) findViewById(R.id.tpr_radio_group_environment_url);
        editCustomUrl = (EditText) findViewById(R.id.tpr_edit_custom_url);
        mvpdIdEdit = (EditText) findViewById(R.id.tpr_edit_tempassid);

        Button btnReset = (Button) findViewById(R.id.tpr_btn_reset);
        Button btnCancel = (Button) findViewById(R.id.tpr_btn_back_to_main);

        radioGroup.setOnCheckedChangeListener(radioGroupOnCheckedChangeListener);
        btnReset.setOnClickListener(btnResetListener);
        btnCancel.setOnClickListener(btnCancelListener);

        requestorId = getIntent().getStringExtra("requestorId");

        editCustomUrl.setEnabled(false);
    }

    private final RadioGroup.OnCheckedChangeListener radioGroupOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            int radioButtonId = radioGroup.getCheckedRadioButtonId();
            View radioButton = radioGroup.findViewById(radioButtonId);
            int id = radioGroup.indexOfChild(radioButton);

            editCustomUrl.setEnabled(id == 2);

            if (!editCustomUrl.isEnabled())
                editCustomUrl.setText("");
        }
    };

    private final View.OnClickListener btnResetListener = new View.OnClickListener() {
        public void onClick(View view) {
            String targetUrl = getEnvironment();
            String deviceId = getDeviceId();
            String mvpdId = mvpdIdEdit.getText().toString();

            String apiKey = ((EditText) findViewById(R.id.tpr_edit_apikey)).getText().toString();
            if (apiKey == null || apiKey.trim().equals("")) {
                alertDialog("Temp Pass Reset", "Please provide your Temp Pass Reset API Key");
                return;
            }

            Log.d(LOG_TAG, "Resetting Temp Pass for device ID: " + deviceId);

            progressDialog = ProgressDialog.show(TempPassResetActivity.this, "", "Talking to backend server...", true);

            new TempPassResetRunner().execute(targetUrl, apiKey, deviceId, mvpdId);
        }

    };

    public String getEnvironment() {
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        View radioButton = radioGroup.findViewById(radioButtonId);
        int id = radioGroup.indexOfChild(radioButton);

        String url;
        switch (id) {
            case 0: url = PREQUAL_URL; break;
            case 1: url = RELEASE_URL; break;
            default: url = editCustomUrl.getText().toString(); break;
        }

        return url;
    }

    private void handleResult(int number) {
        switch (firstDigit(number)) {
            case (2): {
                alertDialog("Temp Pass Reset", "SUCCESS");
            } break;
            default: {
                alertDialog("Temp Pass Reset", "FAILED");
            }
        }
        progressDialog.dismiss();
    }

    private int firstDigit(int httpStatusCode) {
        return String.valueOf(httpStatusCode).charAt(0) - '0';
    }

    private final View.OnClickListener btnCancelListener = new View.OnClickListener() {
        public void onClick(View view) {
            Intent intent = new Intent(TempPassResetActivity.this, MainActivity.class);
            startActivity(intent);
        }
    };

    private class TempPassResetRunner extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            String environmentUrl = params[0];
            String apiKey = params[1];
            String deviceId = params[2];
            String mvpdID = params[3];

            String targetUrl = new StringBuilder().append("https://")
                                                  .append(environmentUrl)
                                                  .append(RESET_SERVICE)
                                                  .append("?device_id=")
                                                  .append(deviceId)
                                                  .append("&requestor_id=")
                                                  .append(requestorId)
                                                  .append("&mvpd_id=")
                                                  .append(mvpdID)
                                                  .toString();
            Log.d(LOG_TAG, "Target URL: " + targetUrl);

            return performHttpDelete(apiKey, targetUrl);
        }

        private Integer performHttpDelete(String apiKey, String targetUrl) {
            try {
                HttpDelete httpDelete = new HttpDelete(targetUrl);
                httpDelete.setHeader("apiKey", apiKey);
                HttpResponse httpResponse = new DefaultHttpClient().execute(httpDelete, new BasicHttpContext());
                return httpResponse.getStatusLine().getStatusCode();
            } catch (Exception e) {
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            handleResult(result);
        }
    }

    private String getDeviceId() {
        return Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}