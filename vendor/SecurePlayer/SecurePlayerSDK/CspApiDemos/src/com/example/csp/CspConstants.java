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

import android.os.Environment;

import com.example.csp.CspContentItem.ECustomDataType;

/**
 * Represents the constants values used by application modules. It also contains data shared by the
 * different activities of the application like current content item.
 */
public class CspConstants {

    /** String value for logs. */
    public static final String TAG = "CspApiDemos";
    /** License file for contents */
    public static final String LICENSE_FILE = "voVidDec.dat";
    /** Used when content is user defined. */
    public static final int CUSTOM_CONTENT_ID = -1;
    /** Directory to save content files. */
    public static final String CONTENT_DIR = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/CspApiDemos_Contents";
    /** Personalization Local/Remote. */
    public static final Boolean PERSONALIZATION_LOCAL = true;
    /** Personalization Url. */
    public static final String PERSONALIZATION_URL = "http://persopp.purpledrm.com/PersoServer/Personalization";
    /** App Version name to be transmitted via performPersonalization API. */
    public static final String PERSONALIZATION_APPLICATION_VERSION = "CspApiDemos";
    /** SessionID string to be transmitted via performPersonalization API. */
    public static final String PERSONALIZATION_SESSION_ID = "Android";

    /** Default user defined {@link CspContentItem}. */
    private static CspContentItem sUserInput = new CspContentItem.Builder("----Custom Content----")
            .setIsStreaming(true).build();
    /**
     * Array of {@link CspContentItem} that holds all possible contents showed into Active Content
     * {@link android.preference.ListPreference}.
     */
    private static CspContentItem[] sContentArry = new CspContentItem[] {

            new CspContentItem.Builder("Big Buck Bunny - Clear MSS")
                    .setContentUrl(
                            "http://195.6.224.140:1080/CSP/PROD/video/smooth/clear/bbb/bbb.ism/manifest")
                    .setIsStreaming(true)
                    .build(),

            new CspContentItem.Builder("Big Buck Bunny - Clear MP4")
                    .setContentUrl("http://195.6.224.140:1080/CSP/PROD/video/mp4/clear/bbb/bbb.mp4")
                    .setIsStreaming(false)
                    .build(),

            new CspContentItem.Builder("Big Buck Bunny - Clear HLS")
                    .setContentUrl(
                            "http://195.6.224.140:1080/CSP/PROD/video/hls/clear/bbb/index.m3u8")
                    .setIsStreaming(true)
                    .build(),

            new CspContentItem.Builder("Big Buck Bunny - Clear MPEG-DASH")
                    .setContentUrl(
                            "http://195.6.224.140:1080/CSP/PROD/video/dash/clear/bbb/index.mpd")
                    .setIsStreaming(true)
                    .build(),

            new CspContentItem.Builder("Sintel - Encrypted MP4 (Envelope)")
                    .setContentUrl(
                            "http://195.6.224.140:1080/CSP/PROD/video/eny/enc/sintel/index.eny")
                    .setInitiatorUrl(
                            "http://195.6.224.140:1080/CSP/PROD/video/eny/enc/sintel/index.cms")
                    .setIsStreaming(false)
                    .build(),

            new CspContentItem.Builder("Big Buck Bunny - Encrypted MSS")
                    .setContentUrl(
                            "http://195.6.224.140:1080/CSP/PROD/video/smooth/enc/bbb/bbb.ism/manifest")
                    .setIsStreaming(true)
                    .build(),

            new CspContentItem.Builder("Big Buck Bunny - Encrypted HLS Discretix v.2.x")
                    .setContentUrl(
                            "http://195.6.224.140:1080/CSP/PROD/video/hls/enc/bbb/index.m3u8")
                    .setIsStreaming(true)
                    .build(),

            new CspContentItem.Builder("Big Buck Bunny - Encrypted MPEG-DASH")
                    .setContentUrl(
                            "http://195.6.224.140:1080/CSP/PROD/video/dash/enc/bbb/index.mpd")
                    .setIsStreaming(true)
                    .build(),
    };

    /**
     * Determines the {@link CspContentItem} item linked with the actions.
     */
    private static CspContentItem sActiveContantItem;

    /**
     * Returns the current active content object. All actions will be performed over it.
     */
    public static CspContentItem getActiveContent() {
        return sActiveContantItem;
    }

    /**
     * Sets the current active content.
     * 
     * @param fromContentItem The new content object {@link CspContentItem}.
     */
    public static void setActiveContent(CspContentItem fromContentItem) {
        sActiveContantItem = fromContentItem;
    }

    /**
     * Returns the content corresponding to a specific id.
     * 
     * @param id Usually it is the index of the context into content array but if
     *            id=CUSTOM_CONTENT_ID it gets the user defined content object.
     */
    public static CspContentItem getContent(int id) {
        if (CUSTOM_CONTENT_ID == id) {
            return sUserInput;
        }
        return sContentArry[id];
    }

    /**
     * Returns the number of content elements that can be selected.
     */
    public static int getNumberOfContents() {
        return sContentArry.length;
    }

    /** Determines if video parameters has been configured. */
    private static boolean sVideoSpecificSet = false;
    /** Determines any subtitles are used. */
    private static boolean sSubtitleUse = false;
    /** Subtitles index. */
    private static int sSubtitleIndexSelected = -1;
    /** Subtitles array. */
    private static String[] sSubtitleArray = null;
    /** Custom subtitle file. */
    private static String sSubtitleCustom = null;
    /** Audio index. */
    private static int sAudioIndexSelected = -1;
    /** Array of audio tracks. */
    private static String[] sAudioArray = null;

    /**
     * Sets the value for videoSpecifics. If it is false (meaning video parameters are not
     * specified) the others video values (subtitles, audio) will be configured to default values.
     * 
     * @param val Boolean value for videoSpecificsSet field.
     */
    public static void setVideoSpecifics(boolean val) {
        sVideoSpecificSet = val;

        if (!val) {
            sSubtitleUse = false;
            sSubtitleArray = null;
            sSubtitleCustom = null;
            sSubtitleIndexSelected = -1;

            sAudioArray = null;
            sAudioIndexSelected = -1;
        }
    }

    /**
     * Returns true if video parameters has been configured, false otherwise.
     */
    public static boolean isVideoSpecificsSet() {
        return sVideoSpecificSet;
    }

    /**
     * Sets the number of subtitles. It will be the length of subtitle array.
     * 
     * @param count total number of subtitles.
     */
    public static void setSubtitleArray(int count) {
        sSubtitleArray = new String[count];
    }

    /**
     * Sets a subtitle in subtitle array index position.
     * 
     * @param index Index into subtitles array.
     * @param subtitle Subtitle.
     * @throws ArrayIndexOutOfBoundsException If index is greater than subtitle array length.
     */
    public static void setSubtitle(int index, String subtitle)
            throws ArrayIndexOutOfBoundsException {

        if (index > sSubtitleArray.length) {
            throw new ArrayIndexOutOfBoundsException("Subtitle index " + index + " does not exist");
        }
        sSubtitleArray[index] = subtitle;
    }

    /**
     * Sets the current selected subtitle to subtitle array index position.
     * 
     * @param index Index into the subtitles array for the selected subtitle.
     */
    public static void setSubtitleSelected(int index) {
        sSubtitleIndexSelected = index;
    }

    /**
     * Returns subtitle array length.
     */
    public static int getSubtitleArrayLength() {
        if (sSubtitleArray != null)
            return sSubtitleArray.length;
        else
            return 0;
    }

    /**
     * Sets subtitle use.
     * 
     * @param val Specifies if subtitle must be active or not.
     */
    public static void setSubtitleUse(boolean val) {
        sSubtitleUse = val;
    }

    /**
     * Returns true if subtitle is being or will be used, false otherwise.
     */
    public static boolean getSubtitleUse() {
        return sSubtitleUse;
    }

    /**
     * Returns the subtitle string into sSubtitleArray[index].
     * 
     * @param index of the subtitle into subtitle array.
     * @throws ArrayIndexOutOfBoundsException If index is greater than subtitle array length.
     */
    public static String getSubtitleArrayEntry(int index) {

        if (index > sSubtitleArray.length) {
            throw new ArrayIndexOutOfBoundsException("Subtitle index " + index + " does not exist");
        }
        return sSubtitleArray[index];
    }

    /**
     * Returns the selected subtitle index.
     */
    public static int getSubtitleSelected() {
        return sSubtitleIndexSelected;
    }

    /**
     * Sets the custom user defined subtitle.
     * 
     * @param subtitle string.
     */
    public static void setSubtitleCustom(String subtitle) {
        sSubtitleCustom = subtitle;
    }

    /**
     * Returns the user defined subtitle, null if it is not defined.
     */
    public static String getSubtitleCustom() {
        return sSubtitleCustom;
    }

    /**
     * Sets the number of audio tracks. It will be the length of audio array.
     * 
     * @param count total number of audio tracks.
     */
    public static void setAudioArray(int count) {
        sAudioArray = new String[count];
    }

    /**
     * Sets an audio track in audio array index position.
     * 
     * @param index Index into audio array.
     * @param audio Audio string.
     * @throws ArrayIndexOutOfBoundsException If index is greater than audio array length.
     */
    public static void setAudio(int index, String audio)
            throws ArrayIndexOutOfBoundsException {

        if (index > sAudioArray.length) {
            throw new ArrayIndexOutOfBoundsException("Audio index " + index + " does not exist");
        }
        sAudioArray[index] = audio;
    }

    /**
     * Sets index for the current audio selected.
     * 
     * @param index into audio array.
     */
    public static void setAudioSelected(int index) {
        sAudioIndexSelected = index;
    }

    /**
     * Returns audio array length.
     */
    public static int getAudioArrayLength() {
        if (sAudioArray != null)
            return sAudioArray.length;
        else
            return 0;
    }

    /**
     * Returns the audio string into sAudioArray[index].
     * 
     * @param index into audio array.
     * @throws ArrayIndexOutOfBoundsException If index is greater than audio array length.
     */
    public static String getAudioArrayEntry(int index) {

        if (index > sAudioArray.length) {
            throw new ArrayIndexOutOfBoundsException("Audio index " + index + " does not exist");
        }
        return sAudioArray[index];
    }

    /**
     * Reuturns current selected audio array index.
     */
    public static int getAudioSelected() {
        return sAudioIndexSelected;
    }

    /**
     * Specifies if hardware acceleration should be used or not.
     */
    private static boolean sHardwareAccelerated = false;

    /**
     * Returns true if hardware acceleration is activated, false otherwise.
     */
    public static boolean isHardwareAccelerated() {
        return sHardwareAccelerated;
    }

    /**
     * Sets value for hardware acceleration.
     * 
     * @param val true if hardware acceleration should be used.
     */
    public static void setHardwareAccelerated(boolean val) {
        sHardwareAccelerated = val;
    }

    /**
     * Specifies if display playback information.
     */
    private static boolean sDisplayPlaybackInformation = false;

    /**
     * Returns true if playback information should be displayed, false otherwise.
     */
    public static boolean getDisplayPlaybackInformation() {
        return sDisplayPlaybackInformation;
    }

    /**
     * Sets value for display playback information property.
     * 
     * @param val true if playback information should be displayed.
     */
    public static void setDisplayPlaybackInformation(boolean val) {
        sDisplayPlaybackInformation = val;
    }

    /**
     * Bits Per Second information field.
     */
    private static String sPlaybackInformationBPS = null;

    /**
     * Returns BPS information
     */
    public static String getPlaybackInformationBPS() {
        return sPlaybackInformationBPS;
    }

    /**
     * Sets BPS value.
     */
    public static void setPlaybackInformationBPS(String val) {
        sPlaybackInformationBPS = val;
    }

    /**
     * Resolution information field.
     */
    private static String sPlaybackInformationResolution = null;

    /**
     * Returns video resolution information.
     */
    public static String getPlaybackInformationResolution() {
        return sPlaybackInformationResolution;
    }

    /**
     * Sets video resolution information to val
     */
    public static void setPlaybackInformationResolution(String val) {
        sPlaybackInformationResolution = val;
    }

}
