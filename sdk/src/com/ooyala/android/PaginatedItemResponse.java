package com.ooyala.android;

public class PaginatedItemResponse {
  public int firstIndex = -1;
  public int count = 0;

  public PaginatedItemResponse(int firstIndex, int count) {
    this.firstIndex = firstIndex;
    this.count = count;
  }
}
