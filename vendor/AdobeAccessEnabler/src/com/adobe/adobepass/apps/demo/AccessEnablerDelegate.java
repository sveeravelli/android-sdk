/*************************************************************************
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2013 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the
 * terms of the Adobe license agreement accompanying it.  If you have received this file from a
 * source other than Adobe, then your use, modification, or distribution of it requires the prior
 * written permission of Adobe.
 *
 * For the avoidance of doubt, this file is Documentation under the Agreement.
 ************************************************************************/

package com.adobe.adobepass.apps.demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.adobe.adobepass.accessenabler.api.IAccessEnablerDelegate;
import com.adobe.adobepass.accessenabler.models.Event;
import com.adobe.adobepass.accessenabler.models.MetadataKey;
import com.adobe.adobepass.accessenabler.models.MetadataStatus;
import com.adobe.adobepass.accessenabler.models.Mvpd;
import com.adobe.adobepass.accessenabler.utils.Log;
import com.adobe.adobepass.accessenabler.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

public class AccessEnablerDelegate implements IAccessEnablerDelegate {
    private static final String LOG_TAG = "AccessEnablerDelegate";

    private Handler handler;

    public final int SET_REQUESTOR_COMPLETE = 0;
    public final int SET_AUTHN_STATUS = 1;
    public final int SET_TOKEN = 2;
    public final int TOKEN_REQUEST_FAILED = 3;
    public final int SELECTED_PROVIDER = 4;
    public final int DISPLAY_PROVIDER_DIALOG = 5;
    public final int NAVIGATE_TO_URL = 6;
    public final int SEND_TRACKING_DATA = 7;
    public final int SET_METADATA_STATUS = 8;
    public final int PREAUTHORIZED_RESOURCES = 9;

    public AccessEnablerDelegate(Handler handler) {
        this.handler = handler;
    }

    private Bundle createMessagePayload(int opCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putInt("op_code", opCode);
        if (message != null) {
            bundle.putString("message", message);
        }

        return bundle;
    }

    public void setRequestorComplete(int status) {
        String message = "setRequestorComplete(" + status + ")";
        Log.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(SET_REQUESTOR_COMPLETE, message);
        bundle.putInt("status", status);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public void setAuthenticationStatus(int status, String errorCode) {
        String message = "setAuthenticationStatus(" + status + ", " + errorCode + ")";
        Log.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(SET_AUTHN_STATUS, message);
        bundle.putInt("status", status);
        bundle.putString("err_code", errorCode) ;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public void setToken(String token, String resourceId) {
        String message = "setToken(" + token + ", " + resourceId + ")";
        Log.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(SET_TOKEN, message);
        bundle.putString("resource_id", resourceId) ;
        bundle.putString("token", token) ;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public void tokenRequestFailed(String resourceId, String errorCode, String errorDescription) {
        String message = "tokenRequestFailed(" + resourceId + ", " + errorCode + ", " + errorDescription + ")";
        Log.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(TOKEN_REQUEST_FAILED, message);
        bundle.putString("resource_id", resourceId) ;
        bundle.putString("err_code", errorCode) ;
        bundle.putString("err_description", errorDescription) ;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public void selectedProvider(Mvpd mvpd) {
        String message;
        if (mvpd != null) {
            message = "selectedProvider(" + mvpd.getId() + ")";
        } else {
            message = "selectedProvider(null)";
        }
        Log.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(SELECTED_PROVIDER, message);
        bundle.putString("mvpd_id", (mvpd == null) ? null : mvpd.getId());
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public void displayProviderDialog(ArrayList<Mvpd> mvpds) {
        String message = "displayProviderDialog(" + mvpds.size() + " mvpds)";
        Log.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(DISPLAY_PROVIDER_DIALOG, message);

        // serialize the MVPD objects
        ArrayList<String> serializedData = new ArrayList<String>();
        for (Mvpd mvpd : mvpds) {
            try {
                serializedData.add(mvpd.serialize());
            } catch (IOException e) {
                Log.e(LOG_TAG, e.toString());
            }
        }

        bundle.putStringArrayList("mvpd_data", serializedData);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    @Override
    public void preauthorizedResources(ArrayList<String> resources) {
        String message = "preauthorizedResources(" + Utils.joinStrings(resources, ", ") + ")";
        Log.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(PREAUTHORIZED_RESOURCES, message);
        bundle.putStringArrayList("resources", resources);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public void navigateToUrl(String url) {
        String message = "navigateToUrl(" + url + ")";
        Log.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(NAVIGATE_TO_URL, message);
        bundle.putString("url", url);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public void sendTrackingData(Event event, ArrayList<String> data) {
        String message = "sendTrackingData(" + Utils.joinStrings(data, "|") + ", " + event.getType() + ")";
        Log.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(SEND_TRACKING_DATA, message);
        bundle.putInt("event_type", event.getType());
        bundle.putStringArrayList("event_data", data);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public void setMetadataStatus(MetadataKey key, MetadataStatus result) {
        String message = "setMetadataStatus(" + key.getKey() + ", " + result + ")";
        Log.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(SET_METADATA_STATUS, message);
        bundle.putSerializable("key", key);
        bundle.putSerializable("result", result);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
}
