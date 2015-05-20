package com.example.secureplayer.apis;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.example.secureplayer.DxConstants;
import com.example.secureplayer.R;

public class PlaybackOptionsActivity extends PreferenceActivity implements
OnSharedPreferenceChangeListener{
	
	ListPreference     subtitlesList = null;
	EditTextPreference subtitlesCustom = null;
	CheckBoxPreference checkBoxSubtitles = null;
	ListPreference     audioLanguageList = null;
	CheckBoxPreference checkBoxPlaybackInfo = null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.playback_options);
		
		//initialize UI handlers
		subtitlesList          = (ListPreference)    findPreference(getString(R.string.key_list_subtitles));
		subtitlesCustom        = (EditTextPreference)findPreference(getString(R.string.key_custom_subtitles));
		checkBoxSubtitles      = (CheckBoxPreference)findPreference(getString(R.string.key_checkbox_subtitles));
		audioLanguageList      = (ListPreference)    findPreference(getString(R.string.key_list_audio_languages));		
		checkBoxPlaybackInfo   = (CheckBoxPreference)findPreference(getString(R.string.key_display_playback_information));
		
		
		int iIndex;
		int iLength;
			
		subtitlesCustom.setText(DxConstants.getSubtitleCustom());
		checkBoxSubtitles.setChecked(DxConstants.getSubtitleUse());
		
		checkBoxPlaybackInfo.setChecked(DxConstants.getDisplayPlaybackInformation());
		
		iLength = DxConstants.getSubtitleArrayLength();
	
		
		if (iLength > 0)
		{
			CharSequence[] entries = null;
			CharSequence[] entryValues = null;
			
			entries = new  CharSequence[iLength];
			entryValues = new CharSequence[iLength];
			
			for (iIndex=0; iIndex<iLength; ++iIndex) {
				entries[iIndex] = DxConstants.getSubtitleArrayEntry(iIndex);
				entryValues[iIndex] = Integer.toString(iIndex);
			}
			
			subtitlesList.setEntries(entries);
			subtitlesList.setEntryValues(entryValues);
			subtitlesList.setEnabled(true);
			
			if (DxConstants.getSubtitleSelected() == -1) {
				subtitlesList.setValue(null);
			} else {
				subtitlesList.setValue(Integer.toString(DxConstants.getSubtitleSelected()));
			}
			
		}
		else 
		{
			subtitlesList.setEnabled(false);
		}
		
		//--- Obtain Audio Channel options from player
		
		iLength = DxConstants.getAudioArrayLength();
		
		// TO-DO
		if (iLength > 0)
		{
			CharSequence[] entries = null;
			CharSequence[] entryValues = null;
			
			entries = new  CharSequence[iLength];
			entryValues = new CharSequence[iLength];
			
			for (iIndex=0; iIndex<iLength; ++iIndex) {
				entries[iIndex] = DxConstants.getAudioArrayEntry(iIndex);
				entryValues[iIndex] = Integer.toString(iIndex);
			}
			
			audioLanguageList.setEntries(entries);
			audioLanguageList.setEntryValues(entryValues);
			audioLanguageList.setEnabled(true);
			
			if (DxConstants.getAudioSelected() == -1) {
				audioLanguageList.setValue(null);
			} else {
				audioLanguageList.setValue(Integer.toString(DxConstants.getAudioSelected()));
			}
			
			
		}
		else 
		{
			audioLanguageList.setEnabled(false);
			
		}				
				
		if (DxConstants.getSubtitleSelected() == -1){
			checkBoxSubtitles.setSummary(DxConstants.getSubtitleCustom());
		} else {
			checkBoxSubtitles.setSummary((subtitlesList.getEntry() != null) ? subtitlesList.getEntry().toString() : null);
		}
		
		
		audioLanguageList.setSummary((audioLanguageList.getEntry() != null) ? audioLanguageList.getEntry().toString() : null);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String key) {
		
		
		getPreferenceScreen().getSharedPreferences()
		.unregisterOnSharedPreferenceChangeListener(this);
		
		if (key.equals(getString(R.string.key_display_playback_information))) {
			boolean bIsChecked = checkBoxPlaybackInfo.isChecked();
			DxConstants.setDisplayPlaybackInformation(bIsChecked);
		}
		else if (key.equals(getString(R.string.key_checkbox_subtitles))) {
			boolean bIsChecked = checkBoxSubtitles.isChecked();
			
			// Enable Subtitles (in general)
			DxConstants.setSubtitleUse(bIsChecked);
			if (!bIsChecked)
			{
				checkBoxSubtitles.setSummary(null);
			}
			
			// Enable custom subtitles
			subtitlesCustom.setEnabled(bIsChecked);
			if (!bIsChecked)
			{
				DxConstants.setSubtitleCustom(null);
			}
			
			// Enable embedded subtitles 
			if (bIsChecked)
			{
				if (DxConstants.getSubtitleArrayLength() > 0) {
					subtitlesList.setEnabled(true);
				}
				else 
				{
					subtitlesList.setEnabled(false);
					subtitlesList.setValue(null);
					DxConstants.setSubtitleSelected(-1);
				}
			}
			else 
			{
				subtitlesList.setEnabled(false);
				subtitlesList.setValue(null);
				DxConstants.setSubtitleSelected(-1);
			}
		}
		
		else if (key.equals(getString(R.string.key_list_subtitles))) {
			if (subtitlesList.getValue() != null && subtitlesList.getValue().length() > 0) {
				subtitlesCustom.setText("");
				checkBoxSubtitles.setSummary((subtitlesList.getEntry() != null) ? subtitlesList.getEntry().toString() : null);
				DxConstants.setSubtitleSelected(Integer.parseInt(subtitlesList.getValue()));
				subtitlesList.setEnabled(true);
				DxConstants.setSubtitleUse(true);
			}
		}
		
		else if (key.equals(getString(R.string.key_custom_subtitles))) {
			if (subtitlesCustom.getText().length() > 0) {
				subtitlesList.setValue("");
				DxConstants.setSubtitleCustom(subtitlesCustom.getText());
				checkBoxSubtitles.setSummary(subtitlesCustom.getText());
				DxConstants.setSubtitleSelected(-1);
				DxConstants.setSubtitleUse(true);
			}
			else {
				DxConstants.setSubtitleCustom(null);
				DxConstants.setSubtitleSelected(-1);
				DxConstants.setSubtitleUse(true);
				checkBoxSubtitles.setSummary("");
			}
		}
		
		else if (key.equals(getString(R.string.key_list_audio_languages))) {
			
			audioLanguageList.setSummary((audioLanguageList.getEntry() != null) ? audioLanguageList.getEntry().toString() : null);
			
			DxConstants.setAudioSelected(Integer.parseInt(audioLanguageList.getValue()));
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

}
