package com.ooyala.android;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

import com.ooyala.android.OoyalaException.OoyalaErrorCode;

/**
 * Monitor, maintain authorization for an asset.
 */
class AuthHeartbeat {
  private Timer _timer = new Timer("AuthHeartbeat");
  private PlayerAPIClient _apiClient;
  private Handler _handler = new Handler();
  private OnAuthHeartbeatErrorListener _authHeartbeatErrorListener;

  public AuthHeartbeat(PlayerAPIClient client) {
    _apiClient = client;
  }

  public void start() {
    stop();
    _timer = new Timer("AuthHeartbeat");
    _timer.schedule(new AuthHeartbeatTimerTask(), 0); //send initial ping right now.
    _timer.scheduleAtFixedRate(new AuthHeartbeatTimerTask(), 0, _apiClient.getHeartbeatInterval() * 1000);
  }

  public void stop() {
    if (_timer != null) {
      _timer.cancel();
    }
    _timer = null;
  }

  class AuthHeartbeatTimerTask extends TimerTask {

    @Override
    public void run() {
      tryHeartbeat(3);
    }

    private void tryHeartbeat(int attempt) {
      OoyalaException exception = null;
      try {
        if (!_apiClient.authorizeHeartbeat()) {
          exception = new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_HEARTBEAT_FAILED, "Unauthorized");
        }
      } catch (OoyalaException e) {
        exception = e;
      }
      if (exception != null) {
        if (attempt > 0) {
          tryHeartbeat(attempt - 1);
        } else {
          sendError(exception);
        }
      }
    }

    private void sendError(final OoyalaException e) {
      _handler.post(new Runnable() {
        @Override
        public void run() {
          final OnAuthHeartbeatErrorListener listener = _authHeartbeatErrorListener;
          if( listener != null ) {
            listener.onAuthHeartbeatError(e);
          }
        }
      });
    }
  }

  public interface OnAuthHeartbeatErrorListener {
    public void onAuthHeartbeatError(OoyalaException e);
  }

  public void setAuthHeartbeatErrorListener(OnAuthHeartbeatErrorListener authHeartBeatErrorListener) {
    _authHeartbeatErrorListener = authHeartBeatErrorListener;
  }
}
