/**
 * *********************************************************************
 * VisualOn Proprietary
 * Copyright (c) 2014, VisualOn Incorporated. All Rights Reserved
 * <p/>
 * VisualOn, Inc., 4675 Stevens Creek Blvd, Santa Clara, CA 95051, USA
 * <p/>
 * All data and information contained in or disclosed by this document are
 * confidential and proprietary information of VisualOn, and all rights
 * therein are expressly reserved. By accepting this material, the
 * recipient agrees that this material and the information contained
 * therein are held in confidence and in trust. The material may only be
 * used and/or disclosed as authorized in a license agreement controlling
 * such use and disclosure.
 * **********************************************************************
 */

package com.visualon.AppUI;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup.LayoutParams;
import android.widget.Toast;

import com.visualon.AppBehavior.AppBehaviorManagerImpl.OPTION_ID;
import com.visualon.AppPlayerCommonFeatures.CDownloader;
import com.visualon.AppPlayerCommonFeatures.CPlayer;
import com.visualon.AppPlayerCommonFeatures.CommonFunc;
import com.visualon.AppPlayerCommonFeatures.Definition;

import java.util.ArrayList;


public class InputBaseFragment extends Fragment {

    private EditText m_edtInputURL = null;
    private View m_btnSelectURL = null;
    private View m_btnStart = null;
    private ListView m_lvURL = null;
    private AlertDialog m_adlgURL = null;
    private int m_nFastChannleIndex = 0;
    private String m_uriVideoPath = null;
    private ArrayList<String> m_lstSelectURL = null;           // Media source list from url.txt
    private boolean m_isSelect = false;
    protected CDownloader m_cDownloader = null;
    protected CPlayer m_cPlayer = null;

    private Context mContext = null;
    private InputBaseFragment mThisContextEnv = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            onPassingDataEventListener = (OnPassingDataEventListener) activity;
        } catch (ClassCastException e) {
            //throw new ClassCastException(activity.toString() + " must implement onPassingDataEventListener");
        }
    }

    public interface OnPassingDataEventListener {
        public void startVideo();
    }

    OnPassingDataEventListener onPassingDataEventListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.url, null);
        m_edtInputURL = (EditText) v.findViewById(R.id.edtInputURL);

        m_btnSelectURL = (View) v.findViewById(R.id.bSelectURL);
        m_btnStart = (View) v.findViewById(R.id.bStart);

        mContext = getActivity();
        mThisContextEnv = this;

        initLayout();
        Uri uri = getActivity().getIntent().getData();
        m_uriVideoPath = CommonFunc.uriToVideoPath(getActivity(), uri);

        m_cPlayer = CommonFunc.getCPlayer();
        m_cPlayer.setPlayerURL(m_uriVideoPath);

        m_cDownloader = CommonFunc.getCDownloader();

        m_edtInputURL.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                m_edtInputURL.requestFocusFromTouch();
                return false;
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        String url = m_edtInputURL.getText().toString();
        if (url != null && url.length() > 0)
            m_cPlayer.setPlayerURL(url);
        if (m_uriVideoPath != null) {
            createPlayer();
            m_uriVideoPath = null;
            Intent intentNew = new Intent();
            intentNew.setClass(getActivity().getApplicationContext(), BasePlayer.class);
            startActivity(intentNew);
        }
    }

    protected void setDRM() {
    }

    private TextWatcher watcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable arg0) {
            String url = m_edtInputURL.getText().toString();
            m_cPlayer.setPlayerURL(url);
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
        }
    };

    private int m_CurrentSelectedinputID = -1;

    private void showVideoSourceSelector(int targetInputID) {

        m_CurrentSelectedinputID = targetInputID;

        readURL();
        if (!m_isSelect) {
            m_adlgURL = new AlertDialog.Builder(getActivity())
                    .setView(m_lvURL)
                    .setPositiveButton("OK", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).create();
            m_isSelect = true;
        }
        m_adlgURL.show();
    }

    public void initLayout() {
        m_edtInputURL.addTextChangedListener(watcher);
        m_lvURL = new ListView(getActivity().getApplicationContext());
        m_lvURL.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        m_lvURL.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                m_nFastChannleIndex = arg2;
                String url = arg0.getItemAtPosition(m_nFastChannleIndex).toString();

                if (m_CurrentSelectedinputID == R.id.edtInputURL) {
                    m_edtInputURL.setText(url);
                }

                if (m_adlgURL != null) {
                    m_adlgURL.dismiss();
                }
            }
        });

        m_btnSelectURL.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showVideoSourceSelector(R.id.edtInputURL);
            }
        });

        m_btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (m_cPlayer.getPlayerURL() == null) {
                    Toast.makeText(getActivity(), "The input url is empty", Toast.LENGTH_LONG).show();
                    return;
                }
				
                createPlayer();
                if (isDownloadEnable()) {
                    m_cDownloader.createDownloader();
                }
                setDRM();
                FeatureManager featureMgr = new FeatureManager(mContext);
                featureMgr.setupDRM(mThisContextEnv);

                if(getActivity() instanceof BasePlayer) {
                    onPassingDataEventListener.startVideo();

                    getActivity().getSupportFragmentManager().beginTransaction().remove(mThisContextEnv).commit();
                    return;
                }

                Intent intent = new Intent();
                intent.putExtra("fastChannleIndex", m_nFastChannleIndex);

                String urlFor2ndPlayer = m_cPlayer.get2ndPlayerUrl();
                if(urlFor2ndPlayer != null)
                intent.putExtra("KEY_2ND_PLAY_URL", urlFor2ndPlayer);

                intent.setClass(getActivity().getApplicationContext(), BasePlayer.class);
                startActivity(intent);
            }
        });
    }

    public void createPlayer() {
        m_cPlayer.createPlayer();
    }
    
    private void readURL() {
        m_lstSelectURL = null;
        m_lstSelectURL = new ArrayList<String>();

        CommonFunc.ReadUrlInfo(getActivity().getApplicationContext(), m_lstSelectURL);
        CommonFunc.getLocalFiles(m_lstSelectURL, Definition.LOCALFILE_PATH);
        CommonFunc.getDownloadFiles(m_lstSelectURL, Definition.DOWNLOAD_PATH);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1, m_lstSelectURL);
        m_lvURL.setAdapter(adapter);
    }

    protected boolean isDownloadEnable() {
        boolean b = (m_cPlayer.getBehavior().getOptionItemByID(OPTION_ID.OPTION_DOWNLOAD_ID.getValue()).getSelect() == 1);
        return b;
    }
}