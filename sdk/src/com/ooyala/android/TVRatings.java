package com.ooyala.android;

public class TVRatings {
  public final String rating;
  public final String labels;
  public TVRatings( String rating, String labels ) {
    this.rating = rating == null ? rating : rating.replaceFirst( "TV-", "" );
    this.labels = labels == null ? labels : labels.replace( ",", "" );
  }
}
