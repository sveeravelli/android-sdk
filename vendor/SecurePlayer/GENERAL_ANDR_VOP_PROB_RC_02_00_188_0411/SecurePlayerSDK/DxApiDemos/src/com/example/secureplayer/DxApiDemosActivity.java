package com.example.secureplayer;

import java.io.File;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.DxLogConfig;
import com.discretix.drmdlc.api.DxLogConfig.LogLevel;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.example.secureplayer.DxContentItem.ECustomDataType;

public class DxApiDemosActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener, OnPreferenceClickListener{

	int selectedItemIndex;
	Boolean isUseCustomData = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		
		//populate active content list preference
		
		ListPreference activeContentPref = (ListPreference) findPreference(getString(R.string.key_active_content_pref));
		String[] contentsEntries = new String[DxConstants.getNumberOfContents()];
		String[] contentsEntryValues = new String[DxConstants.getNumberOfContents()];
		for (int i = 0; i < DxConstants.getNumberOfContents(); i++) {
			contentsEntries[i] = DxConstants.getConetnt(i).getName();
			contentsEntryValues[i] = Integer.toString(i);
		}
		activeContentPref.setEntries(contentsEntries);
		activeContentPref.setEntryValues(contentsEntryValues);
		
		
		//populate custom data type list preference
		ListPreference customDataTypesPref = (ListPreference) findPreference(getString(R.string.key_custom_data_type_pref));
		ECustomDataType[] enumValues = DxContentItem.ECustomDataType.values();
		String[] customDataTypesEntries = new String[enumValues.length];
		String[] customDataTypesEntriesValues = new String[enumValues.length];
		for (int i = 0; i < enumValues.length; i++) {
			customDataTypesEntries[i] = enumValues[i].toString();
			customDataTypesEntriesValues[i] = Integer.toString(i);
		}
		customDataTypesPref.setEntries(customDataTypesEntries);
		customDataTypesPref.setEntryValues(customDataTypesEntriesValues);
		
		
		
		// Calling getDxDrmDlc is not required here. It is done only in order to
		// activate the DRM native logging and to display the version.
		// It is possible to call getDxDrmDlc() only when required.
		try {
			IDxDrmDlc dlc = DxDrmDlc.getDxDrmDlc(this, new DxLogConfig(LogLevel.Info, 10));
			//fill About Version
			String ver = dlc.getDrmVersion();
			ver = ver.replace(';', '\n');
			findPreference(getString(R.string.key_about)).setSummary(ver);
			
		} catch (DrmClientInitFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DrmGeneralFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Create content directory.
		if (new File(DxConstants.CONTENT_DIR).mkdirs() == false){
			if (new File(DxConstants.CONTENT_DIR).exists() == false){
				Log.e(DxConstants.TAG, "Cannot create content directory on SD-CARD");
			}
		}
				
		if (!isUseCustomData) {
			selectedItemIndex = Integer.parseInt(activeContentPref.getValue());
			setContentToDisplay(selectedItemIndex);
		}
		
		String playerType = ((ListPreference) findPreference(getString(R.string.key_player_type))).getEntry().toString();
		findPreference(getString(R.string.key_player_type)).setSummary(playerType);
		
		findPreference(getString(R.string.key_content_url_pref)).setOnPreferenceClickListener(this);
		findPreference(getString(R.string.key_initiator_url_pref)).setOnPreferenceClickListener(this);
		findPreference(getString(R.string.key_custom_la_url_pref)).setOnPreferenceClickListener(this);
		findPreference(getString(R.string.key_custom_data_pref)).setOnPreferenceClickListener(this);
		findPreference(getString(R.string.key_custom_cookies_pref)).setOnPreferenceClickListener(this);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Preference pref = findPreference(key);
		
		//To avoid recursive calls.
		getPreferenceScreen().getSharedPreferences()
		.unregisterOnSharedPreferenceChangeListener(this);
		
		if (key.equals(getString(R.string.key_active_content_pref))) {
			int contentIndex = Integer.parseInt(((ListPreference) pref).getValue());
			setContentToDisplay(contentIndex);
		} else if (key.equals(getString(R.string.key_player_type))) {
			String playerType = ((ListPreference) pref).getEntry().toString();
			findPreference(getString(R.string.key_player_type)).setSummary(playerType);
			DxConstants.setPlayerType(playerType);
		} else if (key.equals(getString(R.string.key_custom_data_type_pref))) {
			ECustomDataType customDataType = ECustomDataType.values()[Integer.parseInt(((ListPreference) pref).getValue())];
			findPreference(getString(R.string.key_custom_data_type_pref)).setSummary(customDataType.toString());
			DxConstants.getActiveContent().setCustomDataType(customDataType);
		} else if (key.equals(getString(R.string.key_use_custom_content_checkBox))) { // "Use custom content" check box.
			isUseCustomData = ((CheckBoxPreference) pref).isChecked();
			if (isUseCustomData == true) {
				setContentToDisplay(DxConstants.CUSTOM_CONTENT_ID);
			} else {
				selectedItemIndex = Integer
						.parseInt(((ListPreference) findPreference(getString(R.string.key_active_content_pref)))
								.getValue());
				setContentToDisplay(selectedItemIndex);
			}
		} else if (key.equals(getString(R.string.key_content_url_pref))) {
			String textPref = ((EditTextPreference) pref).getText();
			DxConstants.getActiveContent().setContentUrl(textPref);
			pref.setSummary(textPref);
		} if (key.equals(getString(R.string.key_initiator_url_pref))) {
			String textPref = ((EditTextPreference) pref).getText();
			DxConstants.getActiveContent().setInitiatorUrl(textPref);
			pref.setSummary(textPref);
		} else if (key.equals(getString(R.string.key_use_local_playback_checkBox))) {	
			boolean isLocal = ((CheckBoxPreference) pref).isChecked();
			DxConstants.getActiveContent().setmIsStreaming(!isLocal);
		} else if (key.equals(getString(R.string.key_custom_data_pref))) {	
			String textPref = ((EditTextPreference) pref).getText();
			DxConstants.getActiveContent().setCustomData(textPref);
			pref.setSummary(textPref);
		} else if (key.equals(getString(R.string.key_custom_la_url_pref))) {	
			String textPref = ((EditTextPreference) pref).getText();
			DxConstants.getActiveContent().setCustomUrl(textPref);
			pref.setSummary(textPref);
		} else if (key.equals(getString(R.string.key_custom_cookies_pref))) {	
			String textPref = ((EditTextPreference) pref).getText();
			DxConstants.getActiveContent().setCookiesFromStr(textPref);
			pref.setSummary(textPref);
		}else{	
			//TODO REPORT UNKNOWN PREF
		}  

		getPreferenceScreen().getSharedPreferences()
		.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}


	private void setContentToDisplay(int contentId) {
		boolean isCustomCotent = DxConstants.CUSTOM_CONTENT_ID == contentId;
		DxContentItem item2Display = DxConstants.getConetnt(contentId);
		DxConstants.setActiveContent(item2Display);
		
		findPreference(getString(R.string.key_content_url_pref)).setEnabled(isCustomCotent);
		findPreference(getString(R.string.key_initiator_url_pref)).setEnabled(isCustomCotent);
		findPreference(getString(R.string.key_use_local_playback_checkBox)).setEnabled(isCustomCotent);
		findPreference(getString(R.string.key_custom_data_pref)).setEnabled(isCustomCotent);
		findPreference(getString(R.string.key_custom_data_type_pref)).setEnabled(isCustomCotent);
		findPreference(getString(R.string.key_custom_la_url_pref)).setEnabled(isCustomCotent);
		findPreference(getString(R.string.key_custom_cookies_pref)).setEnabled(isCustomCotent);		
		
		findPreference(getString(R.string.key_content_url_pref)).setSummary(item2Display.getContentUrl());
		findPreference(getString(R.string.key_initiator_url_pref)).setSummary(item2Display.getInitiatorUrl());
		findPreference(getString(R.string.key_custom_data_pref)).setSummary(item2Display.getCustomData());
		findPreference(getString(R.string.key_custom_data_type_pref)).setSummary(item2Display.getCustomDataType().toString());
		findPreference(getString(R.string.key_custom_la_url_pref)).setSummary(item2Display.getCustomUrl());
		findPreference(getString(R.string.key_custom_cookies_pref)).setSummary(item2Display.getCookiesStr());		
		

		findPreference(getString(R.string.key_active_content_display)).setTitle(item2Display.getName());
		((CheckBoxPreference) findPreference(getString(R.string.key_use_local_playback_checkBox)))
				.setChecked(!item2Display.IsStreaming());

		((CheckBoxPreference) findPreference(getString(R.string.key_use_custom_content_checkBox)))
				.setChecked(isCustomCotent);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {	
		CharSequence summery = preference.getSummary();
		if (summery != null)
			((EditTextPreference)preference).setText(preference.getSummary().toString());
		else
			((EditTextPreference)preference).setText("");
		return true;
	}
}