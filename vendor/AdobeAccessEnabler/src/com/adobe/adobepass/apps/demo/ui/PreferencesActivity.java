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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.*;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import com.adobe.adobepass.apps.demo.R;
import static com.adobe.adobepass.apps.demo.AdobePassDemoApp.*;

public class PreferencesActivity extends AbstractActivity {

    private RadioGroup environmentRadioGroup;
    private CheckBox httpsCheckbox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.preferences);

        httpsCheckbox = (CheckBox) findViewById(R.id.checkbox_https);

        environmentRadioGroup = (RadioGroup) findViewById(R.id.radio_group_environment_url);
        Button btnOk = (Button) findViewById(R.id.btn_set_preferences);

        btnOk.setOnClickListener(btnOkOnClickListener);

        // read url from shared preferences (default value is staging)
        SharedPreferences settings = getSharedPreferences(ADOBE_PASS_PREFERENCES, 0);
        String url = settings.getString(ENVIRONMENT_URL, STAGING_URL);
        boolean useHttps = settings.getBoolean(USE_HTTPS, true);

        httpsCheckbox.setChecked(useHttps);

        if (url.equals(STAGING_URL)) {
            environmentRadioGroup.check(R.id.radio_staging);
        } else {
            environmentRadioGroup.check(R.id.radio_production);
        }
    }

    private final OnClickListener btnOkOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            int radioButtonId = environmentRadioGroup.getCheckedRadioButtonId();
            View radioButton = environmentRadioGroup.findViewById(radioButtonId);
            int id = environmentRadioGroup.indexOfChild(radioButton);

            String url;
            switch (id) {
                case 0: url = STAGING_URL; break;
                case 1: url = PRODUCTION_URL; break;
                default: url = STAGING_URL; break;
            }

            boolean useHttps = httpsCheckbox.isChecked();

            // save preferences
            SharedPreferences settings = getSharedPreferences(ADOBE_PASS_PREFERENCES, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(ENVIRONMENT_URL, url);
            editor.putBoolean(USE_HTTPS, useHttps);
            editor.commit();

            Intent result = new Intent(PreferencesActivity.this, MainActivity.class);
            result.putExtra("url", url);
            result.putExtra("use_https", useHttps);
            setResult(RESULT_OK, result);
            finish();
        }
    };
}
