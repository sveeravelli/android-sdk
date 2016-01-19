/************************************************************************
VisualOn Proprietary
Copyright (c) 2014, VisualOn Incorporated. All Rights Reserved

VisualOn, Inc., 4675 Stevens Creek Blvd, Santa Clara, CA 95051, USA

All data and information contained in or disclosed by this document are
confidential and proprietary information of VisualOn, and all rights
therein are expressly reserved. By accepting this material, the
recipient agrees that this material and the information contained
therein are held in confidence and in trust. The material may only be
used and/or disclosed as authorized in a license agreement controlling
such use and disclosure.
************************************************************************/

package com.visualon.AppUI;

import java.util.ArrayList;
import java.util.HashMap;

import com.visualon.AppPlayerCommonFeatures.CommonFunc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class HistoryActivity extends Activity {
    
    private ListView    m_lvHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        
        setContentView(R.layout.history_page);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.apptitle_customview);
        TextView titleText = (TextView) findViewById(R.id.tvTitle);
        titleText.setText("History");
        
        init();
    }
    
    private void prepareListViewHistory() {
        
        m_lvHistory = (ListView) findViewById(R.id.lvHistory);

        fillHistoryTabContent();

        m_lvHistory.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {

                TextView tvUrl = (TextView) arg1.findViewById(R.id.tvUrl);
                String url = tvUrl.getText().toString();
                Intent intent = new Intent();
                intent.setClass(HistoryActivity.this, MainActivity.class);
                intent.putExtra("URLNAME", url);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);

                finish();
            }
        });

    }
    
    private void fillHistoryTabContent() {

        ArrayList<String> lstHistory = new ArrayList<String>();
        CommonFunc.readHistoryList(lstHistory, getPackageName());

        ArrayList<HashMap<String, String>> urlList;

        urlList = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < lstHistory.size(); i++) {
            HashMap<String, String> urlHash = new HashMap<String, String>();
            urlHash.put("url", lstHistory.get(i));
            urlList.add(urlHash);
        }

        m_lvHistory.setAdapter(new SimpleAdapter(this, urlList,
                R.layout.simplelistitem1, new String[] { "url" },
                new int[] { R.id.tvUrl }));
    }
    
    private void init() {

        prepareListViewHistory();

    }
}
