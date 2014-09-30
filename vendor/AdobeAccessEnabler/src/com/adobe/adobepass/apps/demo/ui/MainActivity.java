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

package com.adobe.adobepass.apps.demo.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.adobe.adobepass.accessenabler.api.AccessEnabler;
import com.adobe.adobepass.accessenabler.api.AccessEnablerException;
import com.adobe.adobepass.accessenabler.models.Event;
import com.adobe.adobepass.accessenabler.models.MetadataKey;
import com.adobe.adobepass.accessenabler.models.MetadataStatus;
import com.adobe.adobepass.accessenabler.models.Mvpd;
import com.adobe.adobepass.accessenabler.utils.Log;
import com.adobe.adobepass.accessenabler.utils.SerializableNameValuePair;
import com.adobe.adobepass.accessenabler.utils.Utils;
import com.adobe.adobepass.apps.demo.AccessEnablerDelegate;
import com.adobe.adobepass.apps.demo.AdobePassDemoApp;
import com.adobe.adobepass.apps.demo.R;
import com.adobe.adobepass.apps.demo.ui.storageviewer.StorageViewerActivity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.adobe.adobepass.apps.demo.AdobePassDemoApp.*;

public class MainActivity extends AbstractActivity {
    private static final String LOG_TAG = "MainActivity";

    private static final String REQUESTOR_ID_HARDCODED = "AdobeBEAST";
    private static final String RESOURCE_ID_HARDCODED = "<rss version=\"2.0\" xmlns:media=\"http://search.yahoo.com/mrss/\"><channel><title><![CDATA[1]]></title><item><title><![CDATA[U]]></title><guid><![CDATA[e]]></guid><media:rating scheme=\"urn:v-chip\">G</media:rating></item></channel></rss>";
    //private static final String RESOURCE_ID_HARDCODED = "<rss version=\"2.0\" xmlns:media=\"http://search.yahoo.com/mrss/\"><channel><title>nbcsports</title><item><title>NBC Sports PGA Event</title><guid>123456789</guid><media:rating scheme=\"urn:vchip\">TV-PG</media:rating></item></channel></rss>";
    private String SP_URL_HARDCODED;

    private static final int MVPD_PICKER_ACTIVITY = 1;
    private static final int MVPD_LOGIN_ACTIVITY = 2;
    private static final int MVPD_LOGOUT_ACTIVITY = 3;
    private static final int PREFERENCES_ACTIVITY = 4;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            int opCode = bundle.getInt("op_code");

            messageHandlers[opCode].handle(bundle);
        }
    };

    private WebView logoutWebView;

    private ProgressDialog spWorkSpinWheel;

    private EditText editRequestorId;
    private EditText editResourceId;
    private EditText editUserMetadata;

    private LinearLayout authnViewGroup;
    private LinearLayout authzViewGroup;
    private LinearLayout userMetadataViewGroup;

    private AccessEnabler accessEnabler;
    private AccessEnablerDelegate delegate = new AccessEnablerDelegate(handler);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "Creating main activity.");

        // inflate the layout for this activity
        setContentView(R.layout.main);

        // obtain handles to UI elements
        editRequestorId = (EditText) findViewById(R.id.edit_requestor_id);
        Button btnSetRequestor = (Button) findViewById(R.id.btn_set_requestor_id);
        Button btnStorageViewer = (Button) findViewById(R.id.btn_storage_viewer);
        Button btnPreferences = (Button) findViewById(R.id.btn_show_preferences);
        Button btnLogout = (Button) findViewById(R.id.btn_logout);

        Button btnCheckAuthN = (Button) findViewById(R.id.btn_check_authn);
        Button btnGetAuthN = (Button) findViewById(R.id.btn_get_authn);
        Button btnTempPassReset = (Button) findViewById(R.id.btn_temp_pass_reset);

        editResourceId = (EditText) findViewById(R.id.edit_resource_id);
        Button btnCheckAuthZ = (Button) findViewById(R.id.btn_check_authz);
        Button btnGetAuthZ = (Button) findViewById(R.id.btn_get_authz);
        Button btnCheckPreAuthZ = (Button) findViewById(R.id.btn_check_preauthz);

        editUserMetadata = (EditText) findViewById(R.id.edit_user_metadata);
        Button btnGetUserMetadata = (Button) findViewById(R.id.btn_get_user_metadata);

        authnViewGroup = (LinearLayout) findViewById(R.id.view_group_authn);
        authzViewGroup = (LinearLayout) findViewById(R.id.view_group_authz);
        userMetadataViewGroup = (LinearLayout) findViewById(R.id.view_group_user_metadata);

        // install event listeners
        btnSetRequestor.setOnClickListener(btnSetRequestorIdOnClickListener);
        btnStorageViewer.setOnClickListener(btnStorageViewerOnClickListener);
        btnPreferences.setOnClickListener(btnPreferencesOnClickListener);
        btnLogout.setOnClickListener(btnLogoutOnClickListener);
        btnCheckAuthN.setOnClickListener(btnCheckAuthNOnClickListener);
        btnGetAuthN.setOnClickListener(btnGetAuthNOnClickListener);
        btnTempPassReset.setOnClickListener(btnTempPassResetListener);
        btnCheckPreAuthZ.setOnClickListener(btnCheckPreAuthZListener);
        btnCheckAuthZ.setOnClickListener(btnCheckAuthZOnClickListener);
        btnGetAuthZ.setOnClickListener(btnGetAuthZOnClickListener);
        btnGetUserMetadata.setOnClickListener(btnGetUserMetadataClickListener);

        // configure the AccessEnabler library
        accessEnabler = AdobePassDemoApp.getAccessEnablerInstance();
        if (accessEnabler != null) {
            // set the delegate for the AccessEnabler
            accessEnabler.setDelegate(delegate);
        } else {
            trace(LOG_TAG, "Failed to configure the AccessEnabler library. ");
            finish();
        }

        editRequestorId.setText(REQUESTOR_ID_HARDCODED);
        editResourceId.setText(RESOURCE_ID_HARDCODED);

        // update the title bar with the client version
        setTitle(getResources().getString(R.string.app_name) + " (v" + accessEnabler.getVersion() + ")");

        // set the default SP URL
        SharedPreferences settings = getSharedPreferences(ADOBE_PASS_PREFERENCES, 0);
        SP_URL_HARDCODED = settings.getString(ENVIRONMENT_URL, STAGING_URL);

        // Warning: this method should be invoked for testing/development purpose only.
        // The production app SHOULD use only HTTPS (this is the default value).
        accessEnabler.useHttps(settings.getBoolean(USE_HTTPS, true));
    }

    private final OnClickListener btnStorageViewerOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, StorageViewerActivity.class);
            startActivity(intent);
        }
    };

    private final OnClickListener btnSetRequestorIdOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            String requestorId = editRequestorId.getText().toString();

            if (!"".equals(requestorId)) {
                // request configuration data
                ArrayList<String> spUrls = new ArrayList<String>();
                spUrls.add(SP_URL_HARDCODED);

                try {
                    String signedRequestorId = AdobePassDemoApp.getSignatureGenerator().generateSignature(requestorId);
                    Log.d(LOG_TAG, "Signed requestor ID: " + signedRequestorId);
                    accessEnabler.setRequestor(requestorId, signedRequestorId, spUrls);

                    // show the spin-wheel
                    spWorkSpinWheel = ProgressDialog.show(MainActivity.this, "", "Talking to backend server...", true);
                } catch (AccessEnablerException e) {
                    trace(LOG_TAG, "Failed to digitally sign the requestor id.");
                }
            } else{
                trace(LOG_TAG, "Enter a valid requestor id.");
            }
        }
    };

    private final OnClickListener btnPreferencesOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
            startActivityForResult(intent, PREFERENCES_ACTIVITY);
        }
    };

    private final OnClickListener btnLogoutOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            // do the logout
            accessEnabler.logout();
        }
    };

    private final OnClickListener btnCheckAuthNOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            accessEnabler.checkAuthentication();
        }
    };

    private final OnClickListener btnGetAuthNOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            accessEnabler.getAuthentication();
        }
    };

    private final OnClickListener btnTempPassResetListener = new OnClickListener() {
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, TempPassResetActivity.class);
            String requestorId = ((EditText) findViewById(R.id.edit_requestor_id)).getText().toString();
            intent.putExtra("requestorId", requestorId);
            startActivity(intent);
        }
    };

    private final OnClickListener btnCheckPreAuthZListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            String[] resourcesArray = editResourceId.getText().toString().split("\\(\\^\\)");
            ArrayList<String> resourcesList = new ArrayList<String>(Arrays.asList(resourcesArray));
            accessEnabler.checkPreauthorizedResources(resourcesList);
        }
    };

    private final OnClickListener btnCheckAuthZOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            accessEnabler.checkAuthorization(editResourceId.getText().toString());
        }
    };

    private final OnClickListener btnGetAuthZOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            accessEnabler.getAuthorization(editResourceId.getText().toString());
        }
    };

    private final OnClickListener btnGetUserMetadataClickListener = new OnClickListener() {
        public void onClick(View view) {
            String metadataName = editUserMetadata.getText().toString();
            if (metadataName != null && metadataName.length() > 0) {
                MetadataKey key = new MetadataKey(AccessEnabler.METADATA_KEY_USER_META);
                key.addArgument(new SerializableNameValuePair(AccessEnabler.METADATA_ARG_USER_META, metadataName));

                accessEnabler.getMetadata(key);
            } else {
                trace(LOG_TAG, "Enter a valid metadata id.");
            }
        }
    };

    public interface MessageHandler {
        void handle(Bundle bundle);
    }

    private WebView createLogoutWebView() {
        // setup the logout WebView (hidden)
        WebView webView = new WebView(MainActivity.this);
        // enable JavaScript support
        WebSettings browserSettings = webView.getSettings();
        browserSettings.setJavaScriptEnabled(true);
        browserSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        // install listeners for various page load events
        webView.setWebViewClient(webViewClient);

        return webView;
    }

    private final WebViewClient webViewClient = new WebViewClient() {

        public boolean shouldOverrideUrlLoading(WebView view, String url){
            Log.d(LOG_TAG, "Loading URL: " + url);

            // if we detect a redirect to our application URL, this is an indication
            // that the logout workflow was completed successfully
            if (url.equals(URLDecoder.decode(AccessEnabler.ADOBEPASS_REDIRECT_URL))) {
                // dismiss the spin-wheel
                spWorkSpinWheel.dismiss();

                // destroy the logout WebView
                logoutWebView.destroy();

                alertDialog("Logout", "SUCCESS") ;
            }

            return false;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.d(LOG_TAG, "Ignoring SSL certificate error.");
            handler.proceed();
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.d(LOG_TAG, description);
            Log.d(LOG_TAG, failingUrl);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(LOG_TAG, "Page started: " + url);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(LOG_TAG, "Page loaded: " + url);
            super.onPageFinished(view, url);
        }
    };

    private MessageHandler[] messageHandlers = new MessageHandler[] {
            new MessageHandler() { public void handle(Bundle bundle) { handleSetRequestor(bundle); } },             //  0 SET_REQUESTOR_COMPLETE
            new MessageHandler() { public void handle(Bundle bundle) { handleSetAuthnStatus(bundle); } },           //  1 SET_AUTHN_STATUS
            new MessageHandler() { public void handle(Bundle bundle) { handleSetToken(bundle); } },                 //  2 SET_TOKEN
            new MessageHandler() { public void handle(Bundle bundle) { handleSetTokenRequestFailed(bundle); } },    //  3 TOKEN_REQUEST_FAILED
            new MessageHandler() { public void handle(Bundle bundle) { handleSelectedProvider(bundle); } },         //  4 SELECTED_PROVIDER
            new MessageHandler() { public void handle(Bundle bundle) { handleDisplayProviderDialog(bundle); } },    //  5 DISPLAY_PROVIDER_DIALOG
            new MessageHandler() { public void handle(Bundle bundle) { handleNavigateToUrl(bundle); } },            //  6 NAVIGATE_TO_URL
            new MessageHandler() { public void handle(Bundle bundle) { handleSendTrackingData(bundle); } },         //  7 SEND_TRACKING_DATA
            new MessageHandler() { public void handle(Bundle bundle) { handleSetMetadataStatus(bundle); } },        //  8 SET_METADATA_STATUS
            new MessageHandler() { public void handle(Bundle bundle) { handlePreauthorizedResources(bundle); } },   //  9 PREAUTHORIZED_RESOURCES
    };

    private void handleMessage(Bundle bundle) {
        String message = bundle.getString("message");
        Log.d(LOG_TAG, message);
    }

    private void handleSetRequestor(Bundle bundle) {
        // extract the status of the setRequestor() API call
        int status = bundle.getInt("status");

        switch (status) {
            case (AccessEnabler.ACCESS_ENABLER_STATUS_SUCCESS): {
                // set requestor operation was successful - enable the authN/Z controls
                authnViewGroup.setVisibility(View.VISIBLE);
                authzViewGroup.setVisibility(View.VISIBLE);
                userMetadataViewGroup.setVisibility(View.VISIBLE);

                alertDialog("Config phase", "SUCCESS");
            } break;
            case (AccessEnabler.ACCESS_ENABLER_STATUS_ERROR): {
                // set requestor operation failed - disable the authN/Z controls
                authnViewGroup.setVisibility(View.GONE);
                authzViewGroup.setVisibility(View.GONE);
                userMetadataViewGroup.setVisibility(View.GONE);

                alertDialog("Config phase", "FAILED");
            } break;
            default: {
                throw new RuntimeException("setRequestor(): Unknown status code.");
            }
        }

        // dismiss the progress dialog
        spWorkSpinWheel.dismiss();
    }

    private void handleSetAuthnStatus(Bundle bundle) {
        // extract the status code
        int status = bundle.getInt("status");
        String errCode = bundle.getString("err_code");

        switch (status) {
            case (AccessEnabler.ACCESS_ENABLER_STATUS_SUCCESS): {
                alertDialog("Authentication", "SUCCESS");
            } break;
            case (AccessEnabler.ACCESS_ENABLER_STATUS_ERROR): {
                alertDialog("Authentication", "FAILED:\n" + errCode);
            } break;
            default: {
                throw new RuntimeException("setAuthnStatus(): Unknown status code.");
            }
        }
    }

    private void handleSetToken(Bundle bundle) {
        // extract the token and resource ID
        String resourceId = bundle.getString("resource_id");
        String token = bundle.getString("token");

        String error;
        if (token == null || token.trim().length() == 0) {
            error = "empty token";
        } else {
            try {
                error = new MediaTokenValidatorTask().execute(resourceId, token).get();
            } catch (Exception e) {
                Log.d(LOG_TAG, e.getMessage());
                error = "token validation process interrupted";
            }
        }

        if (error == null)
            alertDialog("Authorization", "SUCCESS\n\nValidated media token\n\nResource: " + resourceId);
        else
            alertDialog("Authorization", "FAILED\n\nFailed media token validation\n\nResource: " + resourceId + "\nError: " + error);

        Log.d (LOG_TAG, resourceId);
        Log.d(LOG_TAG, "Token: " + token);
    }

    private class MediaTokenValidatorTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String resource = params[0];
                String token = params[1];

                // build request
                HttpClient httpclient = new DefaultHttpClient();
                // release-prod IP for sp.auth.adobe.com (for testing only - bypass spoofing)
                HttpPost httppost = new HttpPost("http://66.235.135.6/tvs/v1/validate");
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("mediaToken", Utils.base64Encode(token.getBytes())));
                nameValuePairs.add(new BasicNameValuePair("resource", Utils.base64Encode(resource.getBytes())));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // execute request
                HttpResponse response = httpclient.execute(httppost);

                // 200 status => token valid
                int code = response.getStatusLine().getStatusCode();
                if (code == HttpStatus.SC_OK)
                    return null;

                // retrieve error message
                InputStream is = response.getEntity().getContent();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, length);
                }
                return new String(baos.toByteArray());
            } catch (Exception e) {
                Log.d(LOG_TAG, e.getMessage());
                return "problem interacting with validator service";
            }
        }
    }

    private void handleSetTokenRequestFailed(Bundle bundle) {
        // extract the error details and resource ID
        String resourceId = bundle.getString("resource_id");
        String errorCode = bundle.getString("err_code");
        String errorDescription = bundle.getString("err_description");

        alertDialog("Authorization", "FAILED\n\nFor resource: " + resourceId +
                "\n\nERROR: " + errorCode +
                "\n\nERROR DETAILS: " + errorDescription);
    }

    private void handleSelectedProvider(Bundle bundle) {
        // extract the MVPD ID
        String mvpdId = bundle.getString("mvpd_id");

        alertDialog("Selected MVPD", (mvpdId == null) ? "None" : mvpdId);
    }

    private void handleDisplayProviderDialog(Bundle bundle) {
        handleMessage(bundle);

        // start the activity that handles the MVPD selection process
        Intent intent = new Intent(MainActivity.this, MvpdPickerActivity.class);
        intent.putExtra("mvpd_bundled_data", bundle);
        startActivityForResult(intent, MVPD_PICKER_ACTIVITY);
    }

    private void handleNavigateToUrl(Bundle bundle) {
        handleMessage(bundle);

        String targetUrl = bundle.getString("url");

        if (targetUrl.indexOf(AccessEnabler.SP_URL_PATH_GET_AUTHENTICATION) > 0) {
            // start the activity that handles the MVPD login process
            Intent intent = new Intent(MainActivity.this, MvpdLoginActivity.class);
            intent.putExtra("url", bundle.getString("url"));
            startActivityForResult(intent, MVPD_LOGIN_ACTIVITY);
        } else if (targetUrl.indexOf(AccessEnabler.SP_URL_PATH_LOGOUT) > 0) {
            // show the spin-wheel
            spWorkSpinWheel = ProgressDialog.show(MainActivity.this, "", "Talking to backend server...", true);

            // instantiate the logout WebView
            logoutWebView = createLogoutWebView();

            // go to the logout URL
            logoutWebView.loadUrl(targetUrl);
        }
    }

    private void handleSendTrackingData(Bundle bundle) {
        // extract the event type and the event data
        int eventType = bundle.getInt("event_type");
        ArrayList<String> data = bundle.getStringArrayList("event_data");

        String message = "";
        String eventName;
        int index = 0;

        switch (eventType) {
            case (Event.EVENT_MVPD_SELECTION): {
                eventName = "mvpd selection";

                message += "MVPD ID: " + data.get(index) + "\n\n"; index ++;
            } break;

            case (Event.EVENT_AUTHN_DETECTION): {
                eventName = "authentication detection";

                message += "SUCCESSFUL: " + data.get(index) + "\n\n"; index ++;
                message += "MVPD ID: " + data.get(index) + "\n\n"; index ++;
                message += "GUID: " + data.get(index) + "\n\n"; index ++;
                message += "CACHED: " + data.get(index) + "\n\n"; index ++;

            } break;

            case (Event.EVENT_AUTHZ_DETECTION): {
                eventName = "authorization detection";
                message += "SUCCESSFUL: " + data.get(index) + "\n\n"; index ++;
                message += "MVPD ID: " + data.get(index) + "\n\n"; index ++;
                message += "GUID: " + data.get(index) + "\n\n"; index ++;
                message += "CACHED: " + data.get(index) + "\n\n"; index ++;
                message += "ERROR: " + data.get(index) + "\n\n"; index ++;
                message += "ERROR DETAILS: " + data.get(index) + "\n\n"; index ++;
            } break;

            default: {
                throw new RuntimeException("setTrackingData(): Unknown event type.");
            }
        }

        message += "DEVICE TYPE: " + data.get(index) + "\n\n"; index ++;
        message += "CLIENT TYPE: " + data.get(index) + "\n\n"; index ++;
        message += "OS: " + data.get(index) + "\n\n";

        alertDialog("Tracking event", "EVENT: " + eventName + "\n\n" + message);
    }

    private void handleSetMetadataStatus(Bundle bundle) {
        // extract the key and the result
        MetadataKey key = (MetadataKey) bundle.getSerializable("key");
        MetadataStatus result = (MetadataStatus) bundle.getSerializable("result");

        switch (key.getKey()) {
            case AccessEnabler.METADATA_KEY_TTL_AUTHN: {
                String ttl = "None";
                if (result != null && result.getSimpleResult() != null) {
                    ttl = result.getSimpleResult();
                }
                alertDialog("AuthN token TTL", ttl);
            } break;

            case AccessEnabler.METADATA_KEY_TTL_AUTHZ: {
                String resourceId = key.getArgument(AccessEnabler.METADATA_ARG_RESOURCE_ID);
                String ttl = "None";
                if (result != null && result.getSimpleResult() != null) {
                    ttl = result.getSimpleResult();
                }
                alertDialog("AuthZ token TTL", "For resource: " + resourceId + "\n\n" + ttl);
            } break;

            case AccessEnabler.METADATA_KEY_DEVICE_ID: {
                String deviceId = "None";
                if (result != null && result.getSimpleResult() != null) {
                    deviceId = result.getSimpleResult();
                }
                alertDialog("Device ID", "".equals(deviceId) ? "None" : deviceId);
            } break;

            case AccessEnabler.METADATA_KEY_USER_META: {
                String metadataName = key.getArgument(AccessEnabler.METADATA_ARG_USER_META);
                Object metadataValue = null;
                String metadataValueDecrypted = null;
                boolean isEncrypted = false;

                if (result != null && result.getUserMetadataResult() != null) {
                    isEncrypted = result.isEncrypted();
                    metadataValue = result.getUserMetadataResult();

                    if (isEncrypted) {
                        try {
                            metadataValueDecrypted =
                                    AdobePassDemoApp.getSignatureGenerator().decryptCiphertext((String) metadataValue);
                        } catch (AccessEnablerException e) {
                            Log.e(LOG_TAG, e.toString());
                            metadataValueDecrypted = null;
                        }
                    }
                }

                String message = "Key: " + (metadataName != null ? metadataName : "None") +
                        "\nEncrypted: " + isEncrypted +
                        "\nValue: " + (metadataValue != null ? metadataValue : "None");
                if (isEncrypted) {
                    message = message + "\nValue decrypted: " +
                            (metadataValueDecrypted != null ? metadataValueDecrypted : "None");
                }

                Log.d(LOG_TAG, "getUserMetadata:\n" + message);
                ((EditText) findViewById(R.id.edit_user_metadata_status)).setText(message);

                alertDialog("User Metadata", message);
            }
            break;

            default: {
                throw new RuntimeException("setRequestor(): Unknown status code.");
            }
        }
    }

    private void handlePreauthorizedResources(Bundle bundle) {
        // extract the pre-authz resource list
        ArrayList<String> resources = bundle.getStringArrayList("resources");

        String message = (resources.size() == 0) ? "None" : Utils.joinStrings(resources, "\n");
        alertDialog("Preauthorized resources", message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case (MVPD_PICKER_ACTIVITY): {
                switch (resultCode) {
                    case (RESULT_OK): {
                        Mvpd mvpd = (Mvpd) data.getSerializableExtra("mvpd");
                        trace(LOG_TAG, "Selected: " + mvpd.getDisplayName());

                        // user has selected an MVPD: call setSelectedProvider()
                        accessEnabler.setSelectedProvider(mvpd.getId());
                    } break;
                    case (RESULT_CANCELED): {
                        trace(LOG_TAG, "Selection canceled.");

                        // abort the authN flow.
                        accessEnabler.setSelectedProvider(null);
                    } break;
                    default: {
                        trace(LOG_TAG, "Cannot handle activity result.");
                    }
                }
            } break;
            case (MVPD_LOGIN_ACTIVITY): {
                switch (resultCode) {
                    case (RESULT_OK): {
                        // retrieve the authentication token
                        accessEnabler.getAuthenticationToken();
                    } break;
                    case (RESULT_CANCELED): {
                        trace(LOG_TAG, "Login canceled.");

                        // abort the authN flow.
                        accessEnabler.setSelectedProvider(null);
                    } break;
                    default: {
                        trace(LOG_TAG, "Cannot handle activity result.");
                    }
                }
            } break;
            case (MVPD_LOGOUT_ACTIVITY): {
                switch (resultCode) {
                    case (RESULT_OK): {
                        trace(LOG_TAG, "Logout successful.");
                    } break;
                    case (RESULT_CANCELED): {
                        trace(LOG_TAG, "Logout canceled.");
                    } break;
                    default: {
                        trace(LOG_TAG, "Cannot handle activity result.");
                    }
                }
            } break;
            case (PREFERENCES_ACTIVITY): {
                SP_URL_HARDCODED = (String) data.getSerializableExtra("url");

                // Warning: this method should be invoked for testing/development purpose only.
                // The production app SHOULD use only HTTPS (this is the default value).
                accessEnabler.useHttps((Boolean) data.getSerializableExtra("use_https"));
            } break;
            default: {
                trace(LOG_TAG, "Unknown activity.");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.application_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MetadataKey key;

        // Handle item selection
        switch (item.getItemId()) {
            case (R.id.authn_exp): {
                key = new MetadataKey(AccessEnabler.METADATA_KEY_TTL_AUTHN);
            } break;
            case (R.id.authz_exp): {
                key = new MetadataKey(AccessEnabler.METADATA_KEY_TTL_AUTHZ);
                key.addArgument(new SerializableNameValuePair(
                        AccessEnabler.METADATA_ARG_RESOURCE_ID, editResourceId.getText().toString()));
            } break;
            case (R.id.device_id): {
                key = new MetadataKey(AccessEnabler.METADATA_KEY_DEVICE_ID);
            } break;
            case (R.id.current_mvpd): {
                accessEnabler.getSelectedProvider();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }

        accessEnabler.getMetadata(key);
        return true;
    }
}
