package com.ooyala.android.nielsensample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.nielsen.app.sdk.AppSdk;
import com.nielsen.app.sdk.IAppNotifier;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.PlayerDomain;
import com.ooyala.android.nielsensdk.NielsenAnalytics;
import com.ooyala.android.ui.OoyalaPlayerLayoutController;
import com.ooyala.android.util.DebugMode;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Observable;
import java.util.Observer;

public class NielsenSampleAppActivity extends Activity implements Observer, IAppNotifier {
  public final static String OPT_OUT_URL_EXTRAS_KEY = "opt_out_url";
  public final static String OPT_OUT_RESULT_KEY = "opt_out_result";
  public final static int OPTOUT_REQUEST_CODE = 100;

  private final static String TAG = NielsenSampleAppActivity.class.getSimpleName();
  private final static String PCODE = "42Zms6h4wdcI1R1uFzepD-KZ0kkk";
  private final static String DOMAIN = "http://www.ooyala.com";
  private final static String NIELSEN_SFCODE = "UAT-CERT";
  private final static String NIELSEN_APPID = "T70BC66D4-C904-4DA1-AB9D-BB658F70E9A7";

  private Spinner embedSpinner;
  private HashMap<String, String> embedMap;
  private ArrayAdapter<String> embedAdapter;
  private NielsenAnalytics nielsenAnalytics;
  private String optOutUrl;
  private OoyalaPlayer player;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    DebugMode.setMode( DebugMode.Mode.LogAndAbort );

    setContentView(R.layout.main);
    embedSpinner = (Spinner) findViewById(R.id.embedSpinner);
    embedMap = new LinkedHashMap<String, String>();
    embedMap.put("ID3-Demo", "84aDVmcTqN3FrdLXClZgJq-GfFEDhS1a");
    embedMap.put("ID3-TravelEast", "Y5aHlyczqJaJ2Mh6BNWLXfpcmxOaKzcx");
    embedMap.put("ID3-TravelLive", "w3MXlyczp03XOkXoGecg4L8xLIyOiPnR");
    embedMap.put("ID3-FoodEast1", "12YnlyczrWcZvPbIJJTV7TmeVi3tgGPa");
    embedMap.put("ID3-FoodEast2", "B1YXlyczpFZhH6GgBSrrO6VWI6aiMKw0");
    embedMap.put("CMS-Demo", "M3bmM3czp1j9horxoTLGaJtgLmW57u4F");
    embedMap.put("CMS-NoAds", "FzYjJzczo3_M3OjkeIta-IIFcPGSGxci");
    embedMap.put("CMS-WithAds", "x3YjJzczqREV-5RDiemsrdqki1FYu2NT");

    embedAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
    embedSpinner.setAdapter(embedAdapter);
    for (String key : embedMap.keySet()) {
      embedAdapter.add(key);
    }
    embedAdapter.notifyDataSetChanged();

    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    OoyalaPlayerLayoutController playerLayoutController = new OoyalaPlayerLayoutController(playerLayout,
        PCODE, new PlayerDomain(DOMAIN));
    player = playerLayoutController.getPlayer();

    nielsenAnalytics = new NielsenAnalytics( this, player, this, NIELSEN_APPID, "0.1", "NielsenTestApp", NIELSEN_SFCODE, player.getID3TagNotifier(), getCustomConfig(), getCustomMetadata() );

    player.addObserver(this);

    final Button optInOutButton = (Button)findViewById( R.id.optInOutButton );
    optInOutButton.setOnClickListener( new OnClickListener() {
      @Override
      public void onClick( View v ) {
        showOptInOutUI();
      }
    } );

    final Button setButton = (Button) findViewById(R.id.setButton);
    setButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        final String embed = embedMap.get(embedSpinner.getSelectedItem());
        if (player.setEmbedCode(embed)) {
          TextView urlText = (TextView) findViewById(R.id.urlText);
          urlText.setText("");
          OoyalaPlayer.enableCustomHLSPlayer = true;
          player.play();
        } else {
          Log.d(TAG, "Something Went Wrong!");
        }
      }
    });

  }

  private String getOptOutUrl() {
    if( nielsenAnalytics != null && nielsenAnalytics.isValid() ) {
      return nielsenAnalytics.getNielsenAppSdk().userOptOutURLString();
    }
    else {
      return null;
    }
  }

  private void showOptInOutUI() {
    final String url = getOptOutUrl();
    if( url == null || url.trim().length() == 0 ) {
      showRestartRequiredMessage();
    }
    else {
      Intent i = new Intent(this, OptOutActivity.class);
      final Bundle pars = new Bundle();
      if (i != null && pars != null) {
        pars.putString(OPT_OUT_URL_EXTRAS_KEY, url);
        i.putExtras(pars);
        startActivityForResult( i, OPTOUT_REQUEST_CODE );
      }
    }
  }

  private void showRestartRequiredMessage() {
    AlertDialog.Builder builder = new AlertDialog.Builder( this );
    builder.setTitle( "No Opt-Out URL" );
    builder.setMessage( "If networking was disabled, please enable networking & restart this app." );
    builder.setNeutralButton( "OK", new DialogInterface.OnClickListener() {
      @Override
      public void onClick( DialogInterface dialog, int which ) {
        dialog.dismiss();
      }
    } );
    builder.show();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult( requestCode, resultCode, data );
    if( resultCode == RESULT_OK ) {
      if( requestCode == OPTOUT_REQUEST_CODE ) {
        final String uoo = data.getStringExtra( OPT_OUT_RESULT_KEY );
        Log.d( TAG, "onActivityResult: uoo = " + uoo );
        nielsenAnalytics.getNielsenAppSdk().userOptOut( uoo );
      }
    }
  }

  public void onAppSdkEvent(long timestamp, int code, String description) {
    switch( code ) {
      case AppSdk.EVENT_INITIATE:
        Log.d( TAG, "EVENT_INITIATE" );
        break;
      case AppSdk.EVENT_STARTUP:
        Log.d( TAG, "EVENT_STARTUP" );
        break;
      case AppSdk.EVENT_SHUTDOWN:
        Log.d( TAG, "EVENT_SHUTDOWN" );
        break;
      case AppSdk.EVENT_FATAL:
        Log.d( TAG, "EVENT_FATAL" );
        break;
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (player != null && player.getState() != OoyalaPlayer.State.SUSPENDED) {
      player.suspend();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (player != null && player.getState() == OoyalaPlayer.State.SUSPENDED) {
      player.resume();
    }
  }

  @Override
  public void update(Observable observable, Object data) {
    Log.d( TAG, "update: " + data );
  }

  private JSONObject getCustomConfig() {
    final JSONObject json = new JSONObject();
    try {
      json.put( "tv", "false" );
      json.put( "nol_devDebug", "true" ); // do NOT do this for production apps!
    }
    catch( JSONException e ) {
      Log.e( TAG, "getCustomConfig()", e );
    }
    return json;
  }

  private JSONObject getCustomMetadata() {
    return null;
  }
}
