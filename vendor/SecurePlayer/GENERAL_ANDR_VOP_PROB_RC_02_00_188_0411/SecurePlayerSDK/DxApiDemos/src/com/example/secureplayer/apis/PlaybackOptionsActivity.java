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
	ListPreference     closedCaptionList = null;
	EditTextPreference subtitlesCustom = null;
	CheckBoxPreference checkBoxSubtitles = null;
	ListPreference     audioLanguageList = null;
	CheckBoxPreference checkBoxClosedCaptions = null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.playback_options);
		
		//initialize UI handlers
		subtitlesList          = (ListPreference)    findPreference(getString(R.string.key_list_subtitles));
		closedCaptionList      = (ListPreference)    findPreference(getString(R.string.key_list_close_captions));
		subtitlesCustom        = (EditTextPreference)findPreference(getString(R.string.key_custom_subtitles));
		checkBoxSubtitles      = (CheckBoxPreference)findPreference(getString(R.string.key_checkbox_subtitles));
		audioLanguageList      = (ListPreference)    findPreference(getString(R.string.key_list_audio_languages));
		checkBoxClosedCaptions = (CheckBoxPreference)findPreference(getString(R.string.key_checkbox_close_captions));
		
		Log.d("Alex","useCustomContent " + DxConstants.getSubtitleUse());
		Log.d("Alex","customSubtitle "   + DxConstants.getSubtitleCustom());
		Log.d("Alex","subtitleIndex "    + DxConstants.getSubtitleSelected());
		
		
		int iIndex;
		int iLength;
		
		//--- Obtain Closed Caption options from player
		CharSequence[] entries0 = { "ClosedCaption1", "ClosedCaption2", "ClosedCaption3" };
		CharSequence[] entryValues0 = { "ClosedCaption1", "ClosedCaption2", "ClosedCaption3" };
		// TO-DO
		
		closedCaptionList.setEntries(entries0);
		closedCaptionList.setEntryValues(entryValues0);
			
		subtitlesCustom.setText(DxConstants.getSubtitleCustom());
		checkBoxSubtitles.setChecked(DxConstants.getSubtitleUse());
		
		
		
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
		
		
		
		
		closedCaptionList.setEnabled(checkBoxClosedCaptions.isChecked());
		
		subtitlesList.setEnabled(checkBoxSubtitles.isChecked());
		subtitlesCustom.setEnabled(checkBoxSubtitles.isChecked());
		
		checkBoxClosedCaptions.setSummary(closedCaptionList.getValue());
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
		
		
		
		
		//Preference pref = findPreference(key);

		if (key.equals(getString(R.string.key_checkbox_close_captions))) {
			closedCaptionList.setEnabled(checkBoxClosedCaptions.isChecked());
		}
		
		else if (key.equals(getString(R.string.key_checkbox_subtitles))) {
			if (DxConstants.getSubtitleArrayLength() > 0) {
				subtitlesList.setEnabled(checkBoxSubtitles.isChecked());
			}
			subtitlesCustom.setEnabled(checkBoxSubtitles.isChecked());
			
			if (!checkBoxSubtitles.isChecked()) {
				subtitlesList.setValue(null);
				
				checkBoxSubtitles.setSummary(null);
				
				DxConstants.setSubtitleSelected(-1);
				DxConstants.setSubtitleCustom(null);
			}
			
			DxConstants.setSubtitleUse(checkBoxSubtitles.isChecked());
		}
		
		else if (key.equals(getString(R.string.key_list_close_captions))) {
			if (closedCaptionList.getValue() != null && (closedCaptionList.getValue().length() > 0)) {
				checkBoxClosedCaptions.setSummary(closedCaptionList.getValue());
			}
		}
		
		else if (key.equals(getString(R.string.key_list_subtitles))) {
			if (subtitlesList.getValue() != null && subtitlesList.getValue().length() > 0) {
				
				subtitlesCustom.setText("");
				
				checkBoxSubtitles.setSummary((subtitlesList.getEntry() != null) ? subtitlesList.getEntry().toString() : null);
				
				DxConstants.setSubtitleSelected(Integer.parseInt(subtitlesList.getValue()));
			}
		}
		
		else if (key.equals(getString(R.string.key_custom_subtitles))) {
			if (subtitlesCustom.getText().length() > 0) {
				
				subtitlesList.setValue("");
				
				DxConstants.setSubtitleCustom(subtitlesCustom.getText());
				DxConstants.setSubtitleSelected(-1);
			}
			else {
				DxConstants.setSubtitleCustom(null);
			}
			
			checkBoxSubtitles.setSummary(subtitlesCustom.getText());
			
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
