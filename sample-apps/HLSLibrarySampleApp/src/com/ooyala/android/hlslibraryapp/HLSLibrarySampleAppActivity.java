package com.ooyala.android.hlslibraryapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OoyalaPlayerLayoutController;
import com.ooyala.android.OptimizedOoyalaPlayerLayoutController;
import com.ooyala.android.hlslibraryapp.R;
import com.visualon.OSMPUtils.voLog;

public class HLSLibrarySampleAppActivity extends Activity {
  /** Called when the activity is first created. */
  OoyalaPlayer player = null;
  String _lastUrl = "";
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    OptimizedOoyalaPlayerLayoutController playerLayoutController = new OptimizedOoyalaPlayerLayoutController(playerLayout,
        "R2d3I6s06RyB712DN0_2GsQS-R-Y.nCFrd", "UpmPCeDJspTKqLHO5IyZSRbsSiC7AM_rAqGztDRN",
        "B3MDExOuTldXc1CiXbzAauYN7Iui", "www.ooyala.com");
    player = playerLayoutController.getPlayer();

    Spinner spinPlayer = (Spinner) findViewById(R.id.spinPlayer);
    Spinner spinUrl = (Spinner) findViewById(R.id.spinUrl);
    Button btnGo = (Button) findViewById(R.id.btnGo);

    _listUrl.add(getString(R.string.strAddLink));
    _listUrl.add("http://player.ooyala.com/player/iphone/liMDE4NjqFlpZJYb6bYRtMgxkItkaGgq.m3u8?geo_country=US");
    _listUrl.add("https://dl.dropbox.com/u/98081242/hls_vod_with_ads.m3u8");
    _listUrl.add("http://player.ooyala.com/player/iphone/I1cmRoNjpD5gJC7ZwNPQO7ZO7M1oahn5.m3u8");
    _listUrl.add("http://csm-e.cds1.yospace.com/csm/restart/live/24537085?yo.p=3&yo.l=true&ext=.m3u8");
    _listUrl.add("http://dev-freewheel.espn.go.com/ad/g/1?_dv=2&nw=87146&csid=watchespn:ios:phone:espn1&caid=15321273&vdur=100&vprn=846897&pvrn=134525&afid=241335&sfid=241335&flag=+sltp+exvt-slcb&prof=87146:watchespn_ios_hls&resp=m3u8;_fw_lpu=http://brs-l3-espn1-hls.espn.go.com/espn1/p/adhoc-s/adhoc-s.m3u8");
    _listUrl.add("http://brs-l3-espn1-hls.espn.go.com/espn1/p/adhoc-s/adhoc-s.m3u8");

    _listUrl.add("http://ooyalaonlinehlslive667.ngcdn01.telstra.net/hls/c304/index-ASG100.m3u8?IS=0&ET=1384213559&CIP=1.2.3.4&KO=2&KN=1&US=e9bafc104e737b26115700fa51be679c");
    _listUrl.add("http://ooyalaonlinehlslive667.ngcdn01.telstra.net/hls/c304/index-ASG101.m3u8?IS=0&ET=1384213540&CIP=1.2.3.4&KO=2&KN=1&US=5c153e2237c56423f49f76c08d341e06");
    _listUrl.add("http://ooyalaonlinehlslive667.ngcdn01.telstra.net/hls/c304/index-ASG102.m3u8?IS=0&ET=1384213529&CIP=1.2.3.4&KO=2&KN=1&US=76faf450e8fd1d383c500558d712bee3");
    _listUrl.add("http://ooyalaonlinehlslive667.ngcdn01.telstra.net/hls/c304/index-ASG103.m3u8?IS=0&ET=1384213189&CIP=1.2.3.4&KO=2&KN=1&US=b4713747d5b1acfa360698babf4380ad");
    _listUrl.add("https://devimages.apple.com.edgekey.net/resources/http-streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8");
    _listUrl.add("http://www.cpcweb.com/webcasts/hls/cpcdemo.m3u8");

    ReadUrlInfo();
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_spinner_item, _listUrl);
    spinUrl.setAdapter(adapter);

    final ArrayList<String> arrPlayers = new ArrayList<String>();
    arrPlayers.add(OoyalaPlayer.PLAYER_VISUALON);
    //arrPlayers.add(OoyalaPlayer.PLAYER_NEXPLAYER);
    arrPlayers.add(OoyalaPlayer.PLAYER_ANDROID);
    ArrayAdapter<String> playerAdapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_spinner_item, arrPlayers);
    spinPlayer.setAdapter(playerAdapter);


    OnClickListener listener = new OnClickListener() {

      @Override
      public void onClick(View v) {
        Spinner spinPlayer = (Spinner) findViewById(R.id.spinPlayer);
        Spinner spinUrl = (Spinner) findViewById(R.id.spinUrl);

        if(spinUrl.getSelectedItem().toString() == getString(R.string.strAddLink)) {
          _lastUrl = null;

          AlertDialog.Builder alert = new AlertDialog.Builder(HLSLibrarySampleAppActivity.this);

          alert.setTitle("Add your own Link");
          alert.setMessage("URL shorteners don't work well.  \nDon't forget 'http://'.\n If it doesn't work, put full urls in url.txt on your sd card");

          // Set an EditText view to get user input
          final EditText input = new EditText(HLSLibrarySampleAppActivity.this);
          alert.setView(input);

          alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {

            Spinner spinPlayer = (Spinner) findViewById(R.id.spinPlayer);
            String url = input.getText().toString();

            player.changeHardCodedUrl(url);
            player.playerType(spinPlayer.getSelectedItem().toString());
            player.setEmbedCode("w5bDRsNzpULkgdCdCqG_4jXPQgTR4P3S"); //"AxYnZrNToCSMFZItXb2oERT8tEFhxro" VOD
            player.play();
            return;
            }
          });

          alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              // Canceled.
            }
          });

          alert.show();
          return;
        }

        player.changeHardCodedUrl(spinUrl.getSelectedItem().toString());
        player.playerType(spinPlayer.getSelectedItem().toString());
        player.setEmbedCode("w5bDRsNzpULkgdCdCqG_4jXPQgTR4P3S"); //"AxYnZrNToCSMFZItXb2oERT8tEFhxro-" - VOD
        player.play();
        return;
      }
    };

    btnGo.setOnClickListener(listener);
  }


  ArrayList<String> _listUrl = new ArrayList<String>();
  /* Retrieve list of media sources */
  public void ReadUrlInfoToList(String configureFile)
  {
    String sUrl,line = "";
    sUrl = configureFile;//"/sdcard/url.txt";
    File UrlFile = new File(sUrl);
    if (!UrlFile.exists())
    {
      Toast.makeText(this, "Could not find "+sUrl+" file!", Toast.LENGTH_LONG).show();
      return ;
    }
    FileReader fileread;
    try
    {
      fileread = new FileReader(UrlFile);
      BufferedReader bfr = new BufferedReader(fileread);
      try
      {
        while (line != null)
        {
          line = bfr.readLine();
          if (line !=null)
          {
            _listUrl.add(line);
          }
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
  }

  /* Retrieve list of media sources */
  public void ReadUrlInfo()
  {
    ReadUrlInfoToList("/sdcard/url.txt");
  }

  @Override
  protected void onPause() {
    player.suspend();
    super.onPause();
  }

  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    player.resume();
    super.onResume();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);

      System.out.println("IN onConfigurationChanged()");
  }


}