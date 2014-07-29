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
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.adobe.adobepass.accessenabler.api.AccessEnabler;
import com.adobe.adobepass.accessenabler.models.Mvpd;
import com.adobe.adobepass.accessenabler.utils.Log;
import com.adobe.adobepass.apps.demo.AdobePassDemoApp;
import com.adobe.adobepass.apps.demo.R;

import java.util.ArrayList;

public class MvpdPickerActivity extends AbstractActivity {
    private static final String LOG_TAG = "MvpdPickerActivity";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // retrieve the name of the MVPD from the intent that started the activity
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("mvpd_bundled_data");

        // inflate the layout for this activity
        setContentView(R.layout.mvpd_picker);

        // get references to UI elements
        ListView mvpdListView = (ListView) findViewById(R.id.mvpd_list);
        mvpdListView.setOnItemClickListener(onItemClickListener);

        // bind the MVPD list to the array adapter
        ArrayList<MvpdListItem> mvpdList = new ArrayList<MvpdListItem>();
        MvpdListArrayAdapter mvpdListArrayAdapter = new MvpdListArrayAdapter(this, R.layout.mvpd_picker_list_item, mvpdList);
        mvpdListView.setAdapter(mvpdListArrayAdapter);

        // set the content of the MVPD list:
        // STEP 1: de-serialize the MVPD objects
        ArrayList<String> serializedData = bundle.getStringArrayList("mvpd_data");
        try {
            for (String item : serializedData) {
                mvpdList.add(new MvpdListItem(Mvpd.deserialze(item)));
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }

        // STEP 2: update the UI
        mvpdListArrayAdapter.notifyDataSetChanged();

        // update the title bar with the client version
        AccessEnabler accessEnabler = AdobePassDemoApp.getAccessEnablerInstance();
        setTitle(getResources().getString(R.string.app_name) + " (v" + accessEnabler.getVersion() + ")");
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Mvpd mvpd = ((MvpdListItem) adapterView.getItemAtPosition(position)).getMvpd();

            Intent result = new Intent(MvpdPickerActivity.this, MainActivity.class);
            result.putExtra("mvpd", mvpd);
            setResult(RESULT_OK, result);
            finish();
        }
    };
}
