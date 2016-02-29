package com.ooyala.android.player;

import com.ooyala.android.OoyalaNotification;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.util.DebugMode;

import java.util.HashMap;
import java.util.Map;

import static com.ooyala.android.OoyalaPlayer.STATE_CHANGED_NOTIFICATION_NAME;
import static com.ooyala.android.OoyalaPlayer.State;

public class PlayerInterfaceUtil {

  private static final String TAG = PlayerInterfaceUtil.class.getSimpleName();

  public static final OoyalaNotification buildSetStateNotification(OoyalaPlayer.State oldState, OoyalaPlayer.State newState) {
    DebugMode.logD(TAG, "player set state " + newState + ", old state was " + oldState);
    final Map<String,State> data = new HashMap<String,OoyalaPlayer.State>();
    data.put(OoyalaNotification.OLD_STATE_KEY, oldState);
    data.put(OoyalaNotification.NEW_STATE_KEY, newState);
    return new OoyalaNotification(
        STATE_CHANGED_NOTIFICATION_NAME,
        data
    );
  }

}
