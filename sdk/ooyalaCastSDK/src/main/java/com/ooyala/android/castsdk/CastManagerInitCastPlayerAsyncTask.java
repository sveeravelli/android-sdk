package com.ooyala.android.castsdk;

import android.os.AsyncTask;

import com.ooyala.android.CastModeOptions;
import com.ooyala.android.EmbedTokenGenerator;
import com.ooyala.android.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * An async task that can handle the blocking embed token generation needed to init the player
 */
public class CastManagerInitCastPlayerAsyncTask extends AsyncTask<Void, Integer, String> {
  CastManager manager;
  CastModeOptions options;

  public CastManagerInitCastPlayerAsyncTask(CastManager manager, CastModeOptions options) {
    super();
    this.manager = manager;
    this.options = options;
  }

  @Override
  protected String doInBackground(Void...params) {
    List<String> embedCodes = new ArrayList<>();
    embedCodes.add(this.options.getEmbedCode());
    return Utils.blockingGetEmbedTokenForEmbedCodes(this.options.getGenerator(), embedCodes);
  }

  @Override
  protected void onPostExecute(String token) {
    super.onPostExecute(token);
    if (!isCancelled()) {
      manager.initCastPlayer(this.options, token);
    }
  }

}
