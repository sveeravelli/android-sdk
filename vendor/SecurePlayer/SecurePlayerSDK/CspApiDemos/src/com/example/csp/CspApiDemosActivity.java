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

package com.example.csp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.DxLogConfig;
import com.discretix.drmdlc.api.DxLogConfig.LogLevel;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.exceptions.DrmAndroidPermissionMissingException;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.vodx.VODXPlayerImpl;
import com.example.csp.CspContentItem.ECustomDataType;
import com.visualon.OSMPPlayer.VOOSMPInitParam;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_MODULE_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_PLAYER_ENGINE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;

import java.io.File;
import java.util.ArrayList;

/**
 * Represents the main application activity. It inherits from PreferenceActivity and configures
 * different categories defined into res/xml/preference.xml. The action performed by some
 * preferences is also configured into that xml file.
 */
public class CspApiDemosActivity extends PreferenceActivity
        implements OnSharedPreferenceChangeListener, OnPreferenceClickListener,
        OnItemLongClickListener {

    /**
     * It is the index of current content into {@link CspConstants#sContentArry}
     */
    int mSelectedItemIndex;

    /**
     * Definition
     */
    Boolean mIsUseCustomData = false;

    /**
     * @deprecated Uses addPreferencesFromResource() and findPreference() method from
     *             PreferenceActivity (which is deprecated since API level 11) to be able to use API
     *             level 10 and less
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read and add preferences categoriesf from xml definition
        addPreferencesFromResource(R.xml.preference);

        // Populate active content list preference
        ListPreference activeContentPref = (ListPreference) findPreference(
                getString(R.string.key_active_content_pref));
        String[] contentsEntries = new String[CspConstants.getNumberOfContents()];
        String[] contentsEntryValues = new String[CspConstants.getNumberOfContents()];
        for (int i = 0; i < CspConstants.getNumberOfContents(); i++) {
            contentsEntries[i] = CspConstants.getContent(i).getName();
            contentsEntryValues[i] = Integer.toString(i);
        }
        activeContentPref.setEntries(contentsEntries);
        activeContentPref.setEntryValues(contentsEntryValues);

        // Populate custom data type list preference
        ListPreference customDataTypesPref = (ListPreference) findPreference(
                getString(R.string.key_custom_data_type_pref));
        ECustomDataType[] enumValues = CspContentItem.ECustomDataType.values();
        String[] customDataTypesEntries = new String[enumValues.length];
        String[] customDataTypesEntriesValues = new String[enumValues.length];
        for (int i = 0; i < enumValues.length; i++) {
            customDataTypesEntries[i] = enumValues[i].toString();
            customDataTypesEntriesValues[i] = Integer.toString(i);
        }
        customDataTypesPref.setEntries(customDataTypesEntries);
        customDataTypesPref.setEntryValues(customDataTypesEntriesValues);

        //check dangerous permissions needed for application and CSP
        ArrayList<String> list = new ArrayList<String>();
        if (0 != PermissionChecker.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
        	list.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(0 != PermissionChecker.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_PHONE_STATE))
        {
        	list.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if (list.size() > 0)
        {
        	String[] permissionMissing = new String[list.size()];
        	permissionMissing = list.toArray(permissionMissing);
        	ActivityCompat.requestPermissions(this, permissionMissing, 0);
        }
        
        
        // Calling getDxDrmDlc is not required here. It is done only in order to
        // activate the DRM native logging and to display the version.
        // It is possible to call getDxDrmDlc() only when required.
        try {
            // Creating and configuring the log level in API
            DxLogConfig config = new DxLogConfig(LogLevel.Info, 10);

            // Calliing CSP object factory function with a specific config,
            // it returns a singleton object
            IDxDrmDlc cspApiSingleton = DxDrmDlc.getDxDrmDlc(this, config);
            String ver = cspApiSingleton.getDrmVersion();
            ver = ver.replace(';', '\n');
            findPreference(getString(R.string.key_about)).setSummary(ver);

            // Initialize the Player object
            VODXPlayerImpl player = new VODXPlayerImpl();
            VOOSMPInitParam initParam = new VOOSMPInitParam();
            String apkPath = getFilesDir().getParent() + "/lib/";
            initParam.setLibraryPath(apkPath);
            initParam.setContext(this);
            VO_OSMP_RETURN_CODE nRet = player.init(VO_OSMP_PLAYER_ENGINE.VO_OSMP_VOME2_PLAYER,
                    initParam);
            if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                Log.e(CspConstants.TAG, "Player init return error code " + nRet);
                
            }

            String VoVer = player.getVersion(VO_OSMP_MODULE_TYPE.VO_OSMP_MODULE_TYPE_SDK);
            findPreference(getString(R.string.key_about_player)).setSummary(VoVer);

            String deviceId = cspApiSingleton.getDeviceID();
            if (deviceId == null)
            	deviceId = "ROOTED";
            findPreference(getString(R.string.key_about_device)).setSummary(deviceId);
            
        } catch (DrmClientInitFailureException e) {
            e.printStackTrace();
            Log.e(CspConstants.TAG, "Cannot get DxDrmDlc element, check libraries link");
            
        } catch (DrmGeneralFailureException e) {
            e.printStackTrace();
            Log.e(CspConstants.TAG, "Cannot get DxDrmDlc Version");
            
        } catch (DrmAndroidPermissionMissingException e) {
        	e.printStackTrace();
            Log.e(CspConstants.TAG, "Cannot get DxDrmDlc element, check application system permission");
		}

        // Create content directory.
        if (new File(CspConstants.CONTENT_DIR).mkdirs() == false) {
            if (new File(CspConstants.CONTENT_DIR).exists() == false) {
                Log.e(CspConstants.TAG, "Cannot create content directory on SD-CARD");
            }
        }

        if (!mIsUseCustomData) {
            // Set default content to display
            mSelectedItemIndex = Integer.parseInt(activeContentPref.getValue());
            setContentToDisplay(mSelectedItemIndex);
        }

        // Set as listener for long clicks
        ListView listView = getListView();
        listView.setOnItemLongClickListener(this);

        // Set this activity as listener of some preferences field
        findPreference(getString(R.string.key_content_url_pref)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.key_initiator_url_pref))
                .setOnPreferenceClickListener(this);
        findPreference(getString(R.string.key_custom_la_url_pref))
                .setOnPreferenceClickListener(this);
        findPreference(getString(R.string.key_custom_data_pref)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.key_custom_cookies_pref))
                .setOnPreferenceClickListener(this);

        Log.i(CspConstants.TAG, "CspApiDemosActivity created");
    }
    
    @SuppressLint("Override")
	public void onRequestPermissionsResult(int arg0, String[] permissions, int[] grantResults) {
    	for (String result : permissions)
    	{
    		if ((result.equalsIgnoreCase(android.Manifest.permission.READ_PHONE_STATE)) &&
    		(PermissionChecker.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_PHONE_STATE) == PermissionChecker.PERMISSION_GRANTED)) 
    		{
    			//reload main activity to update UI for CSP versions
    			this.recreate();
    		}
    	}
		
	}
   
    /**
     * @deprecated Uses getPreferenceScreen() methods from PreferenceActivity (which are deprecated
     *             since API level 11) to be able to use API level 10 and less.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Register as listener of shared preference
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * @deprecated Uses getPreferenceScreen() methods from PreferenceActivity (which are deprecated
     *             since API level 11) to be able to use API level 10 and less.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Unregister as listener of shared preference
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * @deprecated Uses getPreferenceScreen() and findPreference() methods from PreferenceActivity
     *             (which are deprecated since API level 11) to be able to use API level 10 and
     *             less.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Preference pref = findPreference(key);

        // To avoid recursive calls.
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

        // Check which preference has changed and perform corresponding action
        if (key.equals(getString(R.string.key_active_content_pref))) {

            // Update the content
            int contentIndex = Integer.parseInt(((ListPreference) pref).getValue());
            setContentToDisplay(contentIndex);

        } else if (key.equals(getString(R.string.key_hardware_accelerator))) {

            // Update hardware acceleration property
            boolean isHWAccelerated = ((CheckBoxPreference) pref).isChecked();
            CspConstants.setHardwareAccelerated(isHWAccelerated);

        } else if (key.equals(getString(R.string.key_custom_data_type_pref))) {

            // Update custom data type
            ECustomDataType customDataType = ECustomDataType.values()[Integer
                    .parseInt(((ListPreference) pref).getValue())];
            findPreference(getString(R.string.key_custom_data_type_pref))
                    .setSummary(customDataType.toString());
            CspConstants.getActiveContent().setCustomDataType(customDataType);

        } else if (key.equals(getString(R.string.key_use_custom_content_checkBox))) {

            // Update "Use custom content" check box that makes all custom properties editable if it
            // is checked
            mIsUseCustomData = ((CheckBoxPreference) pref).isChecked();
            if (mIsUseCustomData) {
                setContentToDisplay(CspConstants.CUSTOM_CONTENT_ID);
            } else {
                mSelectedItemIndex = Integer.parseInt(
                        ((ListPreference) findPreference(
                                getString(R.string.key_active_content_pref))).getValue());
                setContentToDisplay(mSelectedItemIndex);
            }

        } else if (key.equals(getString(R.string.key_content_url_pref))) {

            // Update content url defined by user
            String textPref = ((EditTextPreference) pref).getText();
            CspConstants.getActiveContent().setContentUrl(textPref);
            pref.setSummary(textPref);

        } else if (key.equals(getString(R.string.key_initiator_url_pref))) {

            // Update initiator url defined by user
            String textPref = ((EditTextPreference) pref).getText();
            CspConstants.getActiveContent().setInitiatorUrl(textPref);
            pref.setSummary(textPref);

        } else if (key.equals(getString(R.string.key_use_local_playback_checkBox))) {

            // Update "use local playback" preference checkbox
            boolean isLocal = ((CheckBoxPreference) pref).isChecked();
            CspConstants.getActiveContent().setmIsStreaming(!isLocal);

        } else if (key.equals(getString(R.string.key_custom_data_pref))) {

            // Update custom data preference
            String textPref = ((EditTextPreference) pref).getText();
            CspConstants.getActiveContent().setCustomData(textPref);
            pref.setSummary(textPref);

        } else if (key.equals(getString(R.string.key_custom_la_url_pref))) {

            // Update custom url defined by user
            String textPref = ((EditTextPreference) pref).getText();
            CspConstants.getActiveContent().setCustomUrl(textPref);
            pref.setSummary(textPref);

        } else if (key.equals(getString(R.string.key_custom_cookies_pref))) {

            // Update custom cookies preference
            String textPref = ((EditTextPreference) pref).getText();
            CspConstants.getActiveContent().setCookiesFromStr(textPref);
            pref.setSummary(textPref);
        } else {
            Log.e(CspConstants.TAG, "Cannot recognise the selected preference");
        }

        // Register as listener again
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * @deprecated Uses getPreferenceScreen() and findPreference() methods from PreferenceActivity
     *             (which are deprecated since API level 11) to be able to use API level 10 and
     *             less.
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {

        Log.i(CspConstants.TAG, "Click preference " + preference.getKey());

        CharSequence summery = preference.getSummary();
        if (summery != null)
            ((EditTextPreference) preference).setText(preference.getSummary().toString());
        else
            ((EditTextPreference) preference).setText("");

        Log.i(CspConstants.TAG, "Preference clicked " + summery);
        return true;
    }

    /**
     * @deprecated Uses findPreference() method and ClipboardManager class from android.text (which
     *             are deprecated since API level 11) to be able to use API level 10 and less.
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long arg3) {

        ListView listView = (ListView) parent;
        Object obj = listView.getItemAtPosition(position);

        // If the preference pressed is Content Url, then copy the url to the clipboard
        if (obj != null
                && isCopyPreference(obj)) {

            EditTextPreference editText = (EditTextPreference) obj;
            ClipboardManager clipboard = (ClipboardManager) getSystemService(
                    Context.CLIPBOARD_SERVICE);
            clipboard.setText(editText.getSummary());
            Toast.makeText(getBaseContext(), "Url copy to clipboard", Toast.LENGTH_SHORT)
                    .show();
            Log.i(CspConstants.TAG, "Text copied " + editText.getSummary());
            return true;
        }

        return false;
    }

    /**
     * Checks if an object is a instance of a Preference that can be copied to the clipboard.
     * 
     * @param obj Object to check
     * @return True if the parameter is able to be copied, false otherwise
     * @override Uses findPreference() method and ClipboardManager class from android.text (which
     *           are deprecated since API level 11) to be able to use API level 10 and less.
     */
    private boolean isCopyPreference(Object obj) {
        return obj.equals(findPreference(getString(R.string.key_content_url_pref))) ||
                obj.equals(findPreference(getString(R.string.key_initiator_url_pref))) ||
                obj.equals(findPreference(getString(R.string.key_custom_la_url_pref))) ||
                obj.equals(findPreference(getString(R.string.key_custom_data_pref))) ||
                obj.equals(findPreference(getString(R.string.key_custom_cookies_pref)));
    }

    /**
     * Sets preferences values for selected content.
     * 
     * @param contentId int value to get the content item.
     * @deprecated Uses findPreference() method (which are deprecated since API level 11) to be able
     *             to use API level 10 and less.
     */
    private void setContentToDisplay(int contentId) {
        CspContentItem item2Display = CspConstants.getContent(contentId);
        CspConstants.setActiveContent(item2Display);

        // Enable or disable depending on value of mIsUseCustomData
        findPreference(getString(R.string.key_content_url_pref)).setEnabled(mIsUseCustomData);
        findPreference(getString(R.string.key_initiator_url_pref)).setEnabled(mIsUseCustomData);
        findPreference(getString(R.string.key_use_local_playback_checkBox))
                .setEnabled(mIsUseCustomData);
        findPreference(getString(R.string.key_custom_data_pref)).setEnabled(mIsUseCustomData);
        findPreference(getString(R.string.key_custom_data_type_pref)).setEnabled(mIsUseCustomData);
        findPreference(getString(R.string.key_custom_la_url_pref)).setEnabled(mIsUseCustomData);
        findPreference(getString(R.string.key_custom_cookies_pref)).setEnabled(mIsUseCustomData);

        // Set summary for the selected item content
        findPreference(getString(R.string.key_content_url_pref))
                .setSummary(item2Display.getContentUrl());
        findPreference(getString(R.string.key_initiator_url_pref))
                .setSummary(item2Display.getInitiatorUrl());
        findPreference(getString(R.string.key_custom_data_pref))
                .setSummary(item2Display.getCustomData());
        findPreference(getString(R.string.key_custom_data_type_pref))
                .setSummary(item2Display.getCustomDataType().toString());
        findPreference(getString(R.string.key_custom_la_url_pref))
                .setSummary(item2Display.getCustomUrl());
        findPreference(getString(R.string.key_custom_cookies_pref))
                .setSummary(item2Display.getCookiesStr());

        findPreference(getString(R.string.key_active_content_display))
                .setTitle(item2Display.getName());
        ((CheckBoxPreference) findPreference(getString(R.string.key_use_local_playback_checkBox)))
                .setChecked(!item2Display.IsStreaming());

        ((CheckBoxPreference) findPreference(getString(R.string.key_use_custom_content_checkBox)))
                .setChecked(mIsUseCustomData);
    }

	
}
