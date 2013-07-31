package com.ooyala.android.devicemangementsampleapp;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.drm.DrmErrorEvent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.ooyala.android.EmbedTokenGenerator;
import com.ooyala.android.EmbedTokenGeneratorCallback;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OoyalaPlayerLayoutController;
import com.ooyala.android.sampleapp.R;

public class DeviceManagementSampleAppActivity extends Activity implements EmbedTokenGenerator, Observer{
	
	final String EMBED  = "fill me in";
	final String PCODE  = "fill me in";
	final String APIKEY = "fill me in";
	final String SECRET = "fill me in";
	final String DOMAIN = "www.ooyala.com";

	private OoyalaPlayer player;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
		OoyalaPlayerLayoutController playerLayoutController = new OoyalaPlayerLayoutController(playerLayout,
				APIKEY, SECRET, PCODE, DOMAIN, this);
		player = playerLayoutController.getPlayer();
		player.addObserver(this);
		if (player.setEmbedCode(EMBED)) {
			player.play();
		} else {
			Log.d(this.getClass().getName(), "Something Went Wrong!");
		}
	}

	@Override
	public void getTokenForEmbedCodes(List<String> embedCodes,
			EmbedTokenGeneratorCallback callback) {
		//add embed token/OPT in the setEmbedToken() example below
		// http://player.ooyala.com/sas/embed_token/pcode/embed_code?account_id=account&api_key=apikey&expires=expires&signature=signature
		callback.setEmbedToken("fill me in");

	}
	// make http requests async
	public void promptNickname(){
		String lastResultUrl = "http://player.ooyala.com/sas/api/v1/device_management/auth_token/" + player.getAuthToken() + "/last_result";
		try {
			final DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(lastResultUrl);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int responseCode = httpResponse.getStatusLine().getStatusCode();
			if (responseCode == 200) {
				HttpEntity httpEntity = httpResponse.getEntity();
				String output = EntityUtils.toString(httpEntity);
				JSONObject jsonObject = new JSONObject(output);
				String result = jsonObject.getString("result");
				final String publicDeviceId = jsonObject.getString("public_device_id");
				if (result.equals("new device registered")) {
					AlertDialog.Builder alert = new AlertDialog.Builder(this);
					final EditText input = new EditText(this); 
					final String nicknameUrl = "http://player.ooyala.com/sas/api/v1/device_management/auth_token/" + player.getAuthToken() + "/devices/" + publicDeviceId;                 
					alert.setTitle("Device Registration");  
					alert.setMessage("Enter Device Nickname: ");                	   			        
					alert.setView(input);
					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
						public void onClick(DialogInterface dialog, int whichButton) {  
							final String value = "{\"nickname\":\""+ input.getText().toString() +"\"}";
							HttpPut httpPut = new HttpPut(nicknameUrl);
							try {
								httpPut.setEntity(new StringEntity(value));
								httpClient.execute(httpPut);
							} catch (ClientProtocolException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}		   								   			                	
							return;                  
						}  
					});  
					alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							return;   
						}
					});
					alert.show();           

				}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		OoyalaPlayer player = (OoyalaPlayer) arg0;
		String notification = arg1.toString();
		Log.d("DEVICE MANAGEMENT", "Notification Recieved: " + arg1 + " - state: " + player.getState());
		//android 3+
		if (notification.equals(OoyalaPlayer.PLAY_STARTED_NOTIFICATION)) {
			promptNickname();
		}
		if (notification.equals(OoyalaPlayer.ERROR_NOTIFICATION)) {
			// http://developer.android.com/reference/android/drm/DrmErrorEvent.html
			if (player.getError().getMessage().equals(DrmErrorEvent.TYPE_PROCESS_DRM_INFO_FAILED +"")) {
				String lastResultUrl = "http://player.ooyala.com/sas/api/v1/device_management/auth_token/" + player.getAuthToken() + "/last_result";
				try {
				// make http requests async
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(lastResultUrl);
				HttpResponse httpResponse = httpClient.execute(httpGet);
				HttpEntity httpEntity = httpResponse.getEntity();
				String output = EntityUtils.toString(httpEntity);
				JSONObject jsonObject = new JSONObject(output);
				String result = jsonObject.getString("result");
				if (result.equals("device limit reached")) {
					// device management error, device limit reached
				} else {
					// regular widevine error
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			} else if (player.getError().getMessage().equals(DrmErrorEvent.TYPE_PROCESS_DRM_INFO_FAILED +"")){
				// regular widevine error
			}
			// else if {} ... check the DRMErrorEvent doc for other Widevine errors
		} 
	}
}
