package com.visualon.AppUI;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.visualon.AppPlayerCommonFeatures.APPCommonPlayerAssetSelection;
import com.visualon.AppPlayerCommonFeatures.APPCommonPlayerAssetSelection.AssetStatus;
import com.visualon.AppPlayerCommonFeatures.APPCommonPlayerAssetSelection.AssetType;
import com.visualon.AppPlayerCommonFeatures.CommonFunc;
import com.visualon.AppPlayerCommonFeatures.voLog;
import com.visualon.OSMPPlayer.VOCommonPlayerAssetSelection.VOOSMPAssetProperty;
import com.visualon.OSMPPlayer.VOOSMPType;

import java.util.ArrayList;

public class AssetDialog extends Dialog implements OnClickListener {
    private static final String TAG = "@@@OSMP+AssetPlayer"; // Tag for VOLog messages
    private RadioGroup m_rgAssetVideo = null;
    private RadioGroup m_rgAssetAudio = null;
    private RadioGroup m_rgAssetSubtitle = null;
    private ArrayList<String> m_lstVideo = null;
    private int m_nSelectedVideoIndex = -1;
    private int m_nSelectedAudioIndex = 0;
    private int m_nSelectedSubtitleIndex = 0;

    private Context m_Context;
    private APPCommonPlayerAssetSelection m_asset = null;

    /* (non-Javadoc)
     * @see android.app.Dialog#onBackPressed()
     */
    @Override
    public void onBackPressed() {

        hide();
        if (m_asset != null)
            m_asset.clearSelection();
        super.onBackPressed();

    }

    @Override
    public void cancel() {
        super.cancel();

        voLog.d(TAG, "cancel is called");
        if (m_asset != null)
            m_asset.clearSelection();
    }

    /* (non-Javadoc)
     * @see android.app.Dialog#onStart()
     */
    @Override
    protected void onStart() {

        super.onStart();
    }

    /* (non-Javadoc)
     * @see android.app.Dialog#onStop()
     */
    @Override
    protected void onStop() {

        super.onStop();
    }

    public AssetDialog(Context context) {
        super(context);

        m_Context = context;
    }

    public AssetDialog(Context context, int theme) {
        super(context, theme);
        m_Context = context;
        setContentView(R.layout.player_asset);
        findViewById(R.id.btnCancel).setOnClickListener(this);
        findViewById(R.id.btnCommit).setOnClickListener(this);
        m_rgAssetVideo = (RadioGroup) findViewById(R.id.rgVideo);
        m_rgAssetAudio = (RadioGroup) findViewById(R.id.rgAudio);
        m_rgAssetSubtitle = (RadioGroup) findViewById(R.id.rgSubtitle);
        
        /*Set tag value for internal auto test.*/
        m_rgAssetVideo.setTag("VIDEO");
        m_rgAssetAudio.setTag("AUDIO");
        m_rgAssetSubtitle.setTag("SUBT");
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnCancel:
                hide();
                if (m_asset != null)
                    m_asset.clearSelection();
                break;
            case R.id.btnCommit:
                hide();
                if (m_asset != null) {
                    VOOSMPType.VO_OSMP_RETURN_CODE returnCode = m_asset.commitSelection();
                    voLog.d(TAG, "CommitSelection: " + returnCode);
                }

                break;
        }
    }

    public void updateAssetInfo(APPCommonPlayerAssetSelection asset) {

        voLog.d(TAG, "+++ updateAssetInfo +++");

        if (asset == null)
            return;
        m_asset = asset;

        m_lstVideo = new ArrayList<String>();
        getVideoDescription(m_lstVideo);
        m_lstVideo.add(0, m_Context.getResources().getString(R.string.Player_BpsQuality_Auto));
        fillAssetRadioGroup(m_rgAssetVideo, m_lstVideo);
        setVideoAssetClickListener(m_lstVideo);

        ArrayList<String> lstAudio = new ArrayList<String>();
        getAudioDescription(lstAudio);
        m_lstAudioCount = lstAudio.size();
        fillAssetRadioGroup(m_rgAssetAudio, lstAudio);
        setAudioAssetClickListener(lstAudio);

        ArrayList<String> lstSubtitle = new ArrayList<String>();
        getSubtitleDescription(lstSubtitle);
        m_lstSubtitleCount = lstSubtitle.size();
        fillAssetRadioGroup(m_rgAssetSubtitle, lstSubtitle);
        setSubtitleAssetClickListener(lstSubtitle);

        initAssetInfo();
        updateButtonAvailability(AssetType.Asset_Video);

        voLog.d(TAG, "--- updateAssetInfo ---");
    }

    public void updateAssetSelection(APPCommonPlayerAssetSelection asset) {
        if (asset == null)
            return;
        m_asset = asset;
    }

    private void updateButtonAvailability(AssetType baseAssetType) {

        voLog.d(TAG, "+++ updateButtonAvailability +++: by videoIndex - " + m_nSelectedVideoIndex);

        if (baseAssetType == AssetType.Asset_Video) {
            checkButtonAvailable(m_lstAudioCount, m_rgAssetAudio, AssetType.Asset_Audio);
            checkButtonAvailable(m_lstSubtitleCount, m_rgAssetSubtitle, AssetType.Asset_Subtitle);
        }

        voLog.d(TAG, "--- updateButtonAvailability ---");
    }

    private int m_lstAudioCount = 0;
    private int m_lstSubtitleCount = 0;

    private void initAssetInfo() {

        voLog.d(TAG, "+++ initAssetInfo +++ " + m_nSelectedVideoIndex + ", " + m_nSelectedAudioIndex + ", " + m_nSelectedSubtitleIndex);

        if (m_rgAssetVideo == null || m_rgAssetAudio == null || m_rgAssetSubtitle == null)
            return;

        if (m_nSelectedVideoIndex >= -1 && m_rgAssetVideo.getChildCount() > (m_nSelectedVideoIndex + 1)) {
            m_rgAssetVideo.check(m_rgAssetVideo.getChildAt(m_nSelectedVideoIndex + 1).getId());
            RadioButton button = (RadioButton) m_rgAssetVideo.getChildAt(m_nSelectedVideoIndex + 1);
        }

        if (m_nSelectedAudioIndex >= 0 && m_rgAssetAudio.getChildCount() > m_nSelectedAudioIndex) {
            m_rgAssetAudio.check(m_rgAssetAudio.getChildAt(m_nSelectedAudioIndex).getId());
            RadioButton button = (RadioButton) m_rgAssetAudio.getChildAt(m_nSelectedAudioIndex);
        }

        if (m_nSelectedSubtitleIndex >= 0 && m_rgAssetSubtitle.getChildCount() > m_nSelectedSubtitleIndex) {
            m_rgAssetSubtitle.check(m_rgAssetSubtitle.getChildAt(m_nSelectedSubtitleIndex).getId());
            RadioButton button = (RadioButton) m_rgAssetSubtitle.getChildAt(m_nSelectedSubtitleIndex);
        }

        voLog.d(TAG, "--- initAssetInfo --- ");
    }

    private void getVideoDescription(ArrayList<String> lstString) {

        voLog.d(TAG, "+++ getVideoDescription +++");

        if (lstString == null || m_asset == null)
            return;

        int nAssetCount = m_asset.getAssetCount(AssetType.Asset_Video);
        if (nAssetCount == 0)
            return;

        int nDefaultIndex = 0;
        for (int nAssetIndex = 0; nAssetIndex < nAssetCount; nAssetIndex++) {
            VOOSMPAssetProperty propImpl = m_asset.getAssetProperty(AssetType.Asset_Video, nAssetIndex);
            String strDescription;
            int nPropertyCount = propImpl.getPropertyCount();
            if (nPropertyCount == 0) {
                strDescription = "V" + Integer.toString(nDefaultIndex++);
            } else {
                final int KEY_DESCRIPTION_INDEX = 2;
                strDescription = (String) propImpl.getValue(KEY_DESCRIPTION_INDEX);
            }
            if (strDescription.length() > 4) {
                String str = strDescription.substring(0, (strDescription.length() - 4));
                voLog.d(TAG, "getVideoDescription:str = " + str);
                if (str.equals("0")) {
                    lstString.add("0");
                } else {
                    lstString.add(CommonFunc.bitrateToString(Integer.valueOf(str).intValue()));
                }
            }
        }
        m_nSelectedVideoIndex = m_asset.getAssetIndex(AssetType.Asset_Video, AssetStatus.Asset_Selected);
        voLog.d(TAG, "--- getVideoDescription --- default: " + m_nSelectedVideoIndex);
    }

    private void getAudioDescription(ArrayList<String> lstString) {

        if (lstString == null || m_asset == null)
            return;

        int nAssetCount = m_asset.getAssetCount(AssetType.Asset_Audio);
        voLog.d(TAG, "+++ getVideoDescription +++: " + nAssetCount);
        if (nAssetCount == 0)
            return;

        int nDefaultIndex = 0;
        for (int nAssetIndex = 0; nAssetIndex < nAssetCount; nAssetIndex++) {
            VOOSMPAssetProperty propImpl = m_asset.getAssetProperty(AssetType.Asset_Audio, nAssetIndex);
            String strDescription;
            int nPropertyCount = propImpl.getPropertyCount();
            if (nPropertyCount == 0) {
                strDescription = "A" + Integer.toString(nDefaultIndex++);
            } else {
                final int KEY_DESCRIPTION_INDEX = 1;
                strDescription = (String) propImpl.getValue(KEY_DESCRIPTION_INDEX);
            }
            lstString.add(strDescription);
        }
        m_nSelectedAudioIndex = m_asset.getAssetIndex(AssetType.Asset_Audio, AssetStatus.Asset_Playing);
        voLog.d(TAG, "--- getVideoDescription --- default: " + m_nSelectedAudioIndex);
    }

    private void getSubtitleDescription(ArrayList<String> lstString) {

        if (lstString == null || m_asset == null)
            return;

        int nAssetCount = m_asset.getAssetCount(AssetType.Asset_Subtitle);
        voLog.d(TAG, "+++ getSubtitleDescription +++: " + nAssetCount);
        if (nAssetCount == 0)
            return;

        int nDefaultIndex = 0;
        for (int nAssetIndex = 0; nAssetIndex < nAssetCount; nAssetIndex++) {
            VOOSMPAssetProperty propImpl = m_asset.getAssetProperty(AssetType.Asset_Subtitle, nAssetIndex);
            String strDescription;
            int nPropertyCount = propImpl.getPropertyCount();
            if (nPropertyCount == 0) {
                strDescription = "Subt" + Integer.toString(nDefaultIndex++);
            } else {
                final int KEY_DESCRIPTION_INDEX = 1;
                strDescription = (String) propImpl.getValue(KEY_DESCRIPTION_INDEX);
            }
            lstString.add(strDescription);
        }
        m_nSelectedSubtitleIndex = m_asset.getAssetIndex(AssetType.Asset_Subtitle, AssetStatus.Asset_Playing);
        voLog.d(TAG, "--- getSubtitleDescription ---: " + m_nSelectedSubtitleIndex);
    }

    private void fillAssetRadioGroup(RadioGroup group, ArrayList<String> list) {
        if (group == null)
            return;
        group.removeAllViews();
        for (int i = 0; i < list.size(); i++) {
            RadioButton radio = new RadioButton(m_Context);
            radio.setText(list.get(i));
            group.addView(radio);
        }
    }

    private void checkButtonAvailable(int buttonCount, RadioGroup radioGroup, AssetType assetType) {

        if(radioGroup != null) {
            radioGroup.clearCheck();
        }

        for (int i = 0; i < buttonCount; i++) {

            RadioButton button = (RadioButton) radioGroup.getChildAt(i);
            int checkingIndex = i;
            if (assetType == AssetType.Asset_Video) {
                checkingIndex = i - 1;
            }

            if (checkingIndex < 0) {

                continue;
            }

            if (m_asset.isTrackAvailable(assetType, checkingIndex)) {

                String selected = "[ ]";
                button.setEnabled(true);
                if(assetType == AssetType.Asset_Audio) {
                    if (checkingIndex == m_nSelectedAudioIndex) {
                        button.setChecked(true);
                        selected = "[*]";
                    }
                } else if(assetType == AssetType.Asset_Subtitle) {
                    if(checkingIndex == m_nSelectedSubtitleIndex) {
                        button.setChecked(true);
                        selected = "[*]";
                    }
                }

                voLog.d(TAG, "O> " + checkingIndex + " - " + button.getText() + " , " + selected);

            } else {

                voLog.d(TAG, "X> " + checkingIndex + " - " + button.getText());
                button.setEnabled(false);
                if(button.isChecked()) {
                    button.setChecked(false);
                }
            }
        }
    }

    private void setVideoAssetClickListener(ArrayList<String> lstString) {
        if (m_rgAssetVideo == null || lstString.size() == 0)
            return;
        for (int i = 0; i < lstString.size(); i++) {
            RadioButton button = (RadioButton) m_rgAssetVideo.getChildAt(i);
            button.setTag(i);
            button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    m_nSelectedVideoIndex = (Integer) v.getTag() - 1;
                    VOOSMPType.VO_OSMP_RETURN_CODE returnCode = m_asset.selectAsset(AssetType.Asset_Video, m_nSelectedVideoIndex);
                    voLog.d(TAG, "selectAsset VideoTrack: " + m_nSelectedVideoIndex + " --> " + returnCode);
                    updateButtonAvailability(AssetType.Asset_Video);
                }
            });
        }
    }

    private void setAudioAssetClickListener(ArrayList<String> lstString) {
        if (lstString.size() <= 0)
            return;

        for (int i = 0; i < lstString.size(); i++) {
            RadioButton button = (RadioButton) m_rgAssetAudio.getChildAt(i);
            button.setTag(i);
            button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    m_nSelectedAudioIndex = (Integer) v.getTag();
                    m_asset.selectAsset(AssetType.Asset_Audio, m_nSelectedAudioIndex);
                }
            });
        }
    }

    private void setSubtitleAssetClickListener(ArrayList<String> lstString) {
        if (lstString.size() <= 0)
            return;
        for (int i = 0; i < lstString.size(); i++) {
            RadioButton button = (RadioButton) m_rgAssetSubtitle.getChildAt(i);
            button.setTag(i);
            button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    m_nSelectedSubtitleIndex = (Integer) v.getTag();
                    m_asset.selectAsset(AssetType.Asset_Subtitle, m_nSelectedSubtitleIndex);
                }
            });
        }
    }

    public ArrayList<String> getVideoArrayList() {
        return m_lstVideo;
    }
}
