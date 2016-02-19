package com.ooyala.android.configuration;

import android.test.AndroidTestCase;

/**
 * Created by zchen on 2/18/16.
 */
public class ExoConfigurationTest extends AndroidTestCase {
  private final long LOW_THRESHOLD = 1000;
  private final long HIGH_THRESHOLD = 500 * 1000;
  private final long HIGH_WATERMARK = 30 * 1000;
  private final long LOW_WATERMARK = 3 * 1000;
  private final float LOW_BUFFER = 0.15f;
  private final float HIGH_BUFFER = 0.85f;

  public ExoConfigurationTest() {
    super();
  }

  @Override
  protected void setUp() {

  }

  @Override
  protected void tearDown() {

  }

  public void testDefaultConfig() {
    ExoConfiguration config = ExoConfiguration.getDefaultExoConfiguration();
    assertEquals(config.getLowerBitrateThreshold(), 0);
    assertEquals(config.getUpperBitrateThreshold(), Integer.MAX_VALUE);
    assertEquals(config.getHighWatermarkMs(), 30000);
    assertEquals(config.getLowWatermarkMs(), 15000);
    assertEquals(config.getHighBufferLoad(), 0.8f);
    assertEquals(config.getLowBufferLoad(), 0.2f);
  }

  public void testValidConfig() {
    ExoConfiguration config =
        new ExoConfiguration.Builder().setUpperBitrateThreshold(HIGH_THRESHOLD).setLowerBitrateThreshold(LOW_THRESHOLD).setHighBufferLoad(HIGH_BUFFER).setLowBufferLoad(LOW_BUFFER).setHighWatermarkMs(HIGH_WATERMARK).setLowWatermarkMs(LOW_WATERMARK).build();
    assertEquals(config.getLowerBitrateThreshold(), LOW_THRESHOLD);
    assertEquals(config.getUpperBitrateThreshold(), HIGH_THRESHOLD);
    assertEquals(config.getHighWatermarkMs(), HIGH_WATERMARK);
    assertEquals(config.getLowWatermarkMs(), LOW_WATERMARK);
    assertEquals(config.getHighBufferLoad(), HIGH_BUFFER);
    assertEquals(config.getLowBufferLoad(), LOW_BUFFER);
  }

  public void testInvalidBitrate() {
    ExoConfiguration config =
        new ExoConfiguration.Builder().setUpperBitrateThreshold(LOW_THRESHOLD).setLowerBitrateThreshold(HIGH_THRESHOLD).build();
    assertNull(config);
  }

  public void testInvalidWatermark() {
    ExoConfiguration config =
        new ExoConfiguration.Builder().setHighWatermarkMs(LOW_WATERMARK).setLowWatermarkMs(HIGH_WATERMARK).build();
    assertNull(config);
  }

  public void testInvalidBufferLoad() {
    ExoConfiguration config =
        new ExoConfiguration.Builder().setHighBufferLoad(LOW_BUFFER).setLowBufferLoad(HIGH_BUFFER).build();
    assertNull(config);
  }

  public void testInvalidLowBufferLoad() {
    ExoConfiguration config =
        new ExoConfiguration.Builder().setLowBufferLoad(-0.5f).build();
    assertNull(config);
  }

  public void testInvalidHighBufferLoad() {
    ExoConfiguration config =
        new ExoConfiguration.Builder().setHighBufferLoad(2.0f).build();
    assertNull(config);
  }
}
