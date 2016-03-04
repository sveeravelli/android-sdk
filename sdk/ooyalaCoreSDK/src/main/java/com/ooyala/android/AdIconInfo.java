package com.ooyala.android;

/**
 * Created by zchen on 3/3/16.
 */
public class AdIconInfo {
  private final int index;
  private final int width;
  private final int height;
  private final int xPosition;
  private final int yPosition;
  private final double offset;
  private final double duration;
  private final String resourceUrl;

  public AdIconInfo(int index, int width, int height, int x, int y, double offset, double duration, String resourceUrl) {
    this.index = index;
    this.width = width;
    this.height = height;
    this.xPosition = x;
    this.yPosition = y;
    this.offset = offset;
    this.duration = duration;
    this.resourceUrl = resourceUrl;
  }

  /**
   * @return the index
   */
  public int getIndex() {
    return index;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getxPosition() {
    return xPosition;
  }

  public int getyPosition() {
    return yPosition;
  }

  public double getOffset() {
    return offset;
  }

  public double getDuration() {
    return duration;
  }

  public String getResourceUrl() {
    return resourceUrl;
  }
}
