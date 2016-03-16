package com.ooyala.android.ads.vast;

import com.ooyala.android.util.DebugMode;

/**
 * Created by zchen on 3/11/16.
 */
public class Offset {
  private static int MAX_OFFSET = Integer.MAX_VALUE / 1024;
  private static final String TAG = Offset.class.getName();
  public enum Type{
    Seconds,
    Percentage,
    Position
  };

  private final Type type;
  private final double value;

  private Offset(Type type, double value) {
    this.type = type;
    this.value = value;
  }

  public Type getType() {
      return type;
    }

  public double getPercentage() {
    return type == Type.Percentage ? value : -1.0;
  }

  public double getSeconds() {
    return type == Type.Seconds ? value : -1.0;
  }

  public int getPosition() {
    return type == Type.Position ? (int)value : -1;
  }


  public static Offset parseOffset(String offsetString) {
    if (offsetString == null) {
      return null;
    }

    if (offsetString.equals("start")) {
      return new Offset(Type.Seconds, 0);
    }

    if (offsetString.equals("end")) {
      return new Offset(Type.Seconds, MAX_OFFSET);
    }

    int percentageIndex = offsetString.indexOf('%');
    double value = -1;
    if (percentageIndex > 0) {
      // Parse percentage string
      try {
        value = Double.parseDouble(offsetString.substring(0, percentageIndex)) / 100.0;
        if (value > 1) {
          value = 1;
        } else if (value < 0) {
          value = 0;
        }
        return new Offset(Type.Percentage, value);
      } catch (NumberFormatException e) {
        DebugMode.logE(TAG, "Invalid time offset:" + offsetString);
        return null;
      }
    } else {
      value = VASTUtils.secondsFromTimeString(offsetString, -1);
      if (value >= 0) {
        return new Offset(Type.Seconds, value);
      } else if (offsetString.charAt(0) == '#') {
        try {
          int position = Integer.parseInt(offsetString.substring(1));
          return new Offset(Type.Position, position);
        } catch (NumberFormatException ex) {
          return null;
        }
      }
    }
    return null;
  }
}
