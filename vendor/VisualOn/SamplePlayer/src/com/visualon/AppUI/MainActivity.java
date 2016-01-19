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


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;

import com.visualon.AppBehavior.AppBehaviorManager;
import com.visualon.AppBehavior.AppBehaviorManagerImpl;
import com.visualon.AppPlayerCommonFeatures.CDownloader;
import com.visualon.AppPlayerCommonFeatures.CPlayer;
import com.visualon.AppPlayerCommonFeatures.CommonFunc;
import com.visualon.AppPlayerCommonFeatures.Definition;
import com.visualon.AppPlayerCommonFeatures.voLog;

import java.io.File;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private static final String  TAG                  = "@@@OSMP+MainActivity"; 
    private FragmentTabHost m_tabHost                 = null;
    private View            mButtonInput              = null;
    private View            mButtonBrowser            = null;
    private View            mButtonOptions            = null;
    private CPlayer         m_cPlayer                 = null;
    private CDownloader     m_cDownloader             = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	voLog.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
        CommonFunc.copyfile(this, "cap.xml", "cap.xml");
        CommonFunc.copyfile(this, "appcfg.xml", "appcfg.xml");
       
        CommonFunc.copyfileTo(this, "jquery-2.1.3.min.js", "/sdcard/SamplePlayer/jquery-2.1.3.min.js");
        CommonFunc.setApplicationSharedPreference(PreferenceManager.getDefaultSharedPreferences(this));
        
        AppBehaviorManager   abManager = new AppBehaviorManagerImpl(this);
        String filePath = CommonFunc.getUserPath(this) + "/" + "appcfg.xml";
        abManager.loadCfgFile(filePath);
        m_cPlayer  = new CPlayer(this);
        m_cPlayer.setBehavior(abManager);
        
        CommonFunc.setCPlayer(m_cPlayer);
        
        File localFile=new File(Definition.LOCALFILE_PATH);
        File downloadFile=new File(Definition.DOWNLOAD_PATH);
        if(!localFile.exists())
            localFile.mkdir();
        if(!downloadFile.exists())
            downloadFile.mkdir();
        m_cDownloader = new CDownloader(this);
        m_cDownloader.setBehavior(abManager);
        CommonFunc.setCDownloader(m_cDownloader);
        findView();
    }
    
    @Override
    protected void onResume(){
    	voLog.i(TAG, "onResume()");
        super.onResume();
    }
    
    @Override
    protected void onStart(){
    	voLog.i(TAG, "onStart()");
        super.onStart();
    }
    
    @Override
    protected void onDestroy(){
    	voLog.i(TAG, "onDestroy()");
        m_cPlayer.destroyPlayer();
        m_cDownloader.destroy();
       super.onDestroy(); 
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	voLog.i(TAG, "onNewIntent()");
        super.onNewIntent(intent);
        String url = intent.getStringExtra("URLNAME");
        MainActivity.this.getIntent().putExtra("URLNAME", url);
    }
    
    private void findView() {
        m_tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        m_tabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        m_tabHost.getTabWidget().setVisibility(View.GONE);  
        m_tabHost.addTab(m_tabHost.newTabSpec("input").setIndicator("Input"),
                InputFragment.class, null);
        m_tabHost.addTab(m_tabHost.newTabSpec("web").setIndicator("Web"),
                WebBrowserFragment.class, null);
       
        m_tabHost.setCurrentTabByTag("input");

        //((RadioButton) findViewById(R.id.input)).setChecked(true);
        //m_radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        // m_radioGroup.setOnCheckedChangeListener(this);

        mButtonInput = findViewById(R.id.input);
        if (mButtonInput != null)
            mButtonInput.setOnClickListener(this);
        mButtonBrowser = findViewById(R.id.webBrowser);
        if (mButtonBrowser != null)
            mButtonBrowser.setOnClickListener(this);
        mButtonOptions = findViewById(R.id.options);
        if (mButtonOptions != null)
            mButtonOptions.setOnClickListener(this);
    }

    private void highlightCurrentPage(int id) {
        switch(id) {
            case R.id.input:
                ((Button)mButtonInput).setTextColor(Color.BLUE);
                ((Button)mButtonBrowser).setTextColor(Color.BLACK);
                break;
            case R.id.webBrowser:
                ((Button)mButtonInput).setTextColor(Color.BLACK);
                ((Button)mButtonBrowser).setTextColor(Color.BLUE);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        voLog.i(TAG, "onCheckedChanged()");
        FragmentManager fm = getSupportFragmentManager();
        InputFragment inputFragment = (InputFragment) fm.findFragmentByTag("input");
        WebBrowserFragment webFragment = (WebBrowserFragment) fm
                .findFragmentByTag("web");
        FragmentTransaction ft = fm.beginTransaction();

        int checkedId = v.getId();

        if (inputFragment != null)
            ft.detach(inputFragment);
        if (webFragment != null)
            ft.detach(webFragment);

        switch (checkedId) {
            case R.id.input:
                if (inputFragment == null) {
                    ft.add(R.id.realtabcontent, new InputFragment(), "input");
                } else {
                    ft.attach(inputFragment);
                }
                m_tabHost.setCurrentTabByTag("input");
                break;
            case R.id.webBrowser:
                if (webFragment == null) {
                    ft.add(R.id.realtabcontent, new WebBrowserFragment(), "web");
                } else {
                    ft.attach(webFragment);
                }
                m_tabHost.setCurrentTabByTag("web");
                break;
            case R.id.options:

                Intent intent = new Intent();
                intent.setClass(MainActivity.this, AppBehaviorManagerImpl.class);
                intent.putExtra("textView", R.id.privew_text);
                intent.putExtra("previewLayout", R.layout.preview);
                intent.putExtra("singleChoiceLayout", R.layout.simple_list_item_single_choice);
                startActivity(intent);

                break;
        }

        highlightCurrentPage(checkedId);
    }
  
}