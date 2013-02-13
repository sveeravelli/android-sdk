package com.ooyala.android;
import java.util.Timer;
import java.util.TimerTask;

import com.ooyala.android.OoyalaException.OoyalaErrorCode;

import android.os.Handler;

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
      try {
        if (!_apiClient.authorizeHeartbeat()) {
          sendError(new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_HEARTBEAT_FAILED, "Unauthorized"));
        }
      } catch (OoyalaException e) {
        sendError(e);
      }
    }

    private void sendError(final OoyalaException e) {
      _handler.post(new Runnable() {
        @Override
        public void run() {
          _authHeartbeatErrorListener.onAuthHeartbeatError(e);
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
