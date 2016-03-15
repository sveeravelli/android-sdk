package com.ooyala.android.ads.vast;

import android.test.AndroidTestCase;

/**
 * Created by zchen on 3/15/16.
 */
public class OffsetTest extends AndroidTestCase {
  public void testOffsetPercentage() {
    String s = "-1%";
    Offset offset = Offset.parseOffset(s);
    assertEquals(Offset.Type.Percentage, offset.getType());
    assertEquals(0.0, offset.getPercentage());

    s = "50%";
    offset = Offset.parseOffset(s);
    assertEquals(Offset.Type.Percentage, offset.getType());
    assertEquals(0.5, offset.getPercentage());

    s = "12.6%";
    offset = Offset.parseOffset(s);
    assertEquals(Offset.Type.Percentage.Percentage, offset.getType());
    assertEquals(0.126, offset.getPercentage());

    s = "100%";
    offset = Offset.parseOffset(s);
    assertEquals(Offset.Type.Percentage, offset.getType());
    assertEquals(1.0, offset.getPercentage());

    s = "126%";
    offset = Offset.parseOffset(s);
    assertEquals(Offset.Type.Percentage, offset.getType());
    assertEquals(1.0, offset.getPercentage());
  }

  public void testOffsetPercentageFail() {
    String s = "ABCD%";
    Offset offset = Offset.parseOffset(s);
    assertNull(offset);
  }

  public void testOffsetSeconds() {
    String s = "03:30:00";
    Offset offset = Offset.parseOffset(s);
    assertEquals(Offset.Type.Seconds, offset.getType());
    assertEquals(3*3600.0 + 30* 60, offset.getSeconds());

    s = "5.000";
    offset = Offset.parseOffset(s);
    assertEquals(Offset.Type.Seconds, offset.getType());
    assertEquals(5.0, offset.getSeconds());

    s = "00:00:08.520";
    offset = Offset.parseOffset(s);
    assertEquals(Offset.Type.Seconds, offset.getType());
    assertEquals(8.52, offset.getSeconds());
  }

  public void testOffsetSecondsFail() {
    String s = "03:abc:00";
    Offset offset = Offset.parseOffset(s);
    assertNull(offset);

    s = "03::00";
    offset = Offset.parseOffset(s);
    assertNull(offset);

    s = "-1:00:00";
    offset = Offset.parseOffset(s);
    assertNull(offset);
  }

  public void testOffsetStart() {
    String s = "start";
    Offset offset = Offset.parseOffset(s);
    assertEquals(Offset.Type.Seconds, offset.getType());
    assertEquals(0, (int)offset.getSeconds());
  }

  public void testOffsetEnd() {
    String s = "end";
    Offset offset = Offset.parseOffset(s);
    assertEquals(Offset.Type.Seconds, offset.getType());
    assertEquals(Integer.MAX_VALUE, (int)offset.getSeconds());
  }

  public void testOffsetPosition() {
    String s = "#1";
    Offset offset = Offset.parseOffset(s);
    assertEquals(Offset.Type.Position, offset.getType());
    assertEquals(1, offset.getPosition());

    s = "#12345";
    offset = Offset.parseOffset(s);
    assertEquals(Offset.Type.Position, offset.getType());
    assertEquals(12345, offset.getPosition());
  }

  public void testOffsetPositionFail() {
    String s = "##3";
    Offset offset = Offset.parseOffset(s);
    assertNull(offset);

    s = "#1.35";
    offset = Offset.parseOffset(s);
    assertNull(offset);
  }
}
