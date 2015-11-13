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

package com.example.csp.apis;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

import com.example.csp.CspConstants;
import com.example.csp.R;

/**
 * Represents different Preferences the user can configure for the Player. It can
 * activate/deactivate subtitle, select audio track, show media info. All preferences are defined
 * into res/xml/playback_options.
 */
public class PlaybackOptionsActivity extends PreferenceActivity
        implements OnSharedPreferenceChangeListener {

    ListPreference mSubtitlesList = null;
    EditTextPreference mSubtitlesCustom = null;
    CheckBoxPreference mCheckBoxSubtitles = null;
    ListPreference mAudioLanguageList = null;
    CheckBoxPreference mCheckBoxPlaybackInfo = null;

    /**
     * @deprecated Uses addPreferencesFromResource() and findPreference() method from
     *             PreferenceActivity (which is deprecated since API level 11) to be able to use API
     *             level 10 and less.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.playback_options);

        // initialize UI handlers
        mSubtitlesList = (ListPreference) findPreference(getString(R.string.key_list_subtitles));
        mSubtitlesCustom = (EditTextPreference) findPreference(
                getString(R.string.key_custom_subtitles));
        mCheckBoxSubtitles = (CheckBoxPreference) findPreference(
                getString(R.string.key_checkbox_subtitles));
        mAudioLanguageList = (ListPreference) findPreference(
                getString(R.string.key_list_audio_languages));
        mCheckBoxPlaybackInfo = (CheckBoxPreference) findPreference(
                getString(R.string.key_display_playback_information));

        mSubtitlesCustom.setText(CspConstants.getSubtitleCustom());
        mCheckBoxSubtitles.setChecked(CspConstants.getSubtitleUse());
        mCheckBoxPlaybackInfo.setChecked(CspConstants.getDisplayPlaybackInformation());

        // Configure subtitle list
        int iIndex;
        int iLength;

        iLength = CspConstants.getSubtitleArrayLength();

        if (iLength > 0) {
            CharSequence[] entries = null;
            CharSequence[] entryValues = null;

            entries = new CharSequence[iLength];
            entryValues = new CharSequence[iLength];

            for (iIndex = 0; iIndex < iLength; ++iIndex) {
                entries[iIndex] = CspConstants.getSubtitleArrayEntry(iIndex);
                entryValues[iIndex] = Integer.toString(iIndex);
            }

            mSubtitlesList.setEntries(entries);
            mSubtitlesList.setEntryValues(entryValues);
            mSubtitlesList.setEnabled(true);

            if (CspConstants.getSubtitleSelected() == -1) {
                mSubtitlesList.setValue(null);
            } else {
                mSubtitlesList.setValue(Integer.toString(CspConstants.getSubtitleSelected()));
            }

        } else {
            mSubtitlesList.setEnabled(false);
        }

        // Obtain Audio Channel options from player
        iLength = CspConstants.getAudioArrayLength();

        if (iLength > 0) {
            CharSequence[] entries = null;
            CharSequence[] entryValues = null;

            entries = new CharSequence[iLength];
            entryValues = new CharSequence[iLength];

            for (iIndex = 0; iIndex < iLength; ++iIndex) {
                entries[iIndex] = CspConstants.getAudioArrayEntry(iIndex);
                entryValues[iIndex] = Integer.toString(iIndex);
            }

            mAudioLanguageList.setEntries(entries);
            mAudioLanguageList.setEntryValues(entryValues);
            mAudioLanguageList.setEnabled(true);

            if (CspConstants.getAudioSelected() == -1) {
                mAudioLanguageList.setValue(null);
            } else {
                mAudioLanguageList.setValue(Integer.toString(CspConstants.getAudioSelected()));
            }

        } else {
            mAudioLanguageList.setEnabled(false);

        }

        if (CspConstants.getSubtitleSelected() == -1) {
            mCheckBoxSubtitles.setSummary(CspConstants.getSubtitleCustom());
        } else {
            mCheckBoxSubtitles
                    .setSummary((mSubtitlesList.getEntry() != null)
                            ? mSubtitlesList.getEntry().toString() : null);
        }

        mAudioLanguageList
                .setSummary((mAudioLanguageList.getEntry() != null)
                        ? mAudioLanguageList.getEntry().toString() : null);
    }

    /**
     * @deprecated Uses getPreferenceScreen() method from PreferenceActivity (which is deprecated
     *             since API level 11) to be able to use API level 10 and less.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences arg0, String key) {

        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

        if (key.equals(getString(R.string.key_display_playback_information))) {

            // Activate playback information
            boolean bIsChecked = mCheckBoxPlaybackInfo.isChecked();
            CspConstants.setDisplayPlaybackInformation(bIsChecked);

        } else if (key.equals(getString(R.string.key_checkbox_subtitles))) {

            boolean bIsChecked = mCheckBoxSubtitles.isChecked();

            // Enable Subtitles (in general)
            CspConstants.setSubtitleUse(bIsChecked);
            if (!bIsChecked) {
                mCheckBoxSubtitles.setSummary(null);
            }

            // Enable custom subtitles
            mSubtitlesCustom.setEnabled(bIsChecked);
            if (!bIsChecked) {
                CspConstants.setSubtitleCustom(null);
            }

            // Enable embedded subtitles
            if (bIsChecked) {

                if (CspConstants.getSubtitleArrayLength() > 0) {
                    mSubtitlesList.setEnabled(true);
                } else {
                    mSubtitlesList.setEnabled(false);
                    mSubtitlesList.setValue(null);
                    CspConstants.setSubtitleSelected(-1);
                }
            } else {
                mSubtitlesList.setEnabled(false);
                mSubtitlesList.setValue(null);
                CspConstants.setSubtitleSelected(-1);
            }

        } else if (key.equals(getString(R.string.key_list_subtitles))) {

            if (mSubtitlesList.getValue() != null && mSubtitlesList.getValue().length() > 0) {
                mSubtitlesCustom.setText("");
                mCheckBoxSubtitles
                        .setSummary((mSubtitlesList.getEntry() != null)
                                ? mSubtitlesList.getEntry().toString() : null);
                CspConstants.setSubtitleSelected(Integer.parseInt(mSubtitlesList.getValue()));
                mSubtitlesList.setEnabled(true);
                CspConstants.setSubtitleUse(true);
            }
        } else if (key.equals(getString(R.string.key_custom_subtitles))) {

            if (mSubtitlesCustom.getText().length() > 0) {
                mSubtitlesList.setValue("");
                CspConstants.setSubtitleCustom(mSubtitlesCustom.getText());
                mCheckBoxSubtitles.setSummary(mSubtitlesCustom.getText());
                CspConstants.setSubtitleSelected(-1);
                CspConstants.setSubtitleUse(true);
            } else {
                CspConstants.setSubtitleCustom(null);
                CspConstants.setSubtitleSelected(-1);
                CspConstants.setSubtitleUse(true);
                mCheckBoxSubtitles.setSummary("");
            }
        } else if (key.equals(getString(R.string.key_list_audio_languages))) {

            mAudioLanguageList.setSummary(
                    (mAudioLanguageList.getEntry() != null)
                            ? mAudioLanguageList.getEntry().toString()
                            : null);
            CspConstants.setAudioSelected(Integer.parseInt(mAudioLanguageList.getValue()));
        }

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * @deprecated Uses getPreferenceScreen() method from PreferenceActivity (which is deprecated
     *             since API level 11) to be able to use API level 10 and less.
     */
    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * @deprecated Uses getPreferenceScreen() method from PreferenceActivity (which is deprecated
     *             since API level 11) to be able to use API level 10 and less
     */
    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

}
