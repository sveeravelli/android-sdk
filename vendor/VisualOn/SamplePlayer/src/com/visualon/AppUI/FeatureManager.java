package com.visualon.AppUI;

import android.content.Context;

import com.visualon.AppPlayerCommonFeatures.CDownloader;
import com.visualon.AppPlayerCommonFeatures.CPlayer;
import com.visualon.AppPlayerCommonFeatures.CommonFunc;
import com.visualon.AppPlayerCommonFeatures.Definition;

public class FeatureManager {

    private final static String TAG = "FeatureMgr";

    private Context mContext = null;

    public FeatureManager(Context context) {
        mContext = context;
    }

    public void setupDRM(InputBaseFragment mContextEnv) {

        setupDRM(mContextEnv.m_cPlayer, (mContextEnv.isDownloadEnable())?mContextEnv.m_cDownloader:null);

    }

    public void setupDRM(CPlayer cPlayer, CDownloader cDownloader) {

        CommonFunc.saveStringPreferenceValue(Definition.PREFERENCE_KEY_DRM_TYPE, Definition.DRM_TYPE_A);
        cPlayer.setDRMAdapter("libvoDRMCommonAES128.so", true);
    }

}
